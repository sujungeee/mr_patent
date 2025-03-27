import os
import uuid
import re
from fastapi import APIRouter, BackgroundTasks, HTTPException, status

from app.core.logging import logger
from app.core.database import database, patent, task_status
from app.schemas.patent import ProcessRequest, ProcessResponse
from app.services.extractor import extract_patent_data, prepare_corpus
from app.services.vectorizer import fit_tfidf_vectorizer, generate_vectors
from striprtf.striprtf import rtf_to_text

router = APIRouter(prefix="/api/patents", tags=["patents"])

async def process_patent_files(rtf_directory: str, task_id: str):
    """특허 데이터 처리 및 저장"""
    try:
        # 작업 상태 초기화
        rtf_files = [f for f in os.listdir(rtf_directory) if f.endswith(".rtf")]
        total_files = len(rtf_files)
        
        await database.execute(
            task_status.update().where(task_status.c.task_id == task_id).values(
                total_files=total_files,
                processed_files=0,
                progress=0
            )
        )
        
        # 파일명 패턴
        filename_pattern = r"([a-zA-Z]\d+)_(\d+)_(\d+)\.rtf"
        
        all_patents = []
        processed_files = 0
        
        # 1단계: 모든 파일에서 특허 데이터 추출
        for filename in rtf_files:
            ipc_match = re.match(filename_pattern, filename)
            ipc_code = ipc_match.group(1) if ipc_match else None
            
            file_path = os.path.join(rtf_directory, filename)
            try:
                with open(file_path, 'r', encoding='utf-8') as file:
                    rtf_content = file.read()
                rtf_text = rtf_to_text(rtf_content)
                logger.info(f"RTF 변환 결과 (처음 500자): {rtf_text[:500]}")
                
                # 특허 데이터 추출
                patents = extract_patent_data(rtf_text)
                
                # IPC 코드 추가
                for patent_data in patents:
                    patent_data['ipc_from_filename'] = ipc_code
                
                all_patents.extend(patents)
                
                processed_files += 1
                progress = int((processed_files / total_files) * 50)
                
                # 작업 상태 업데이트
                await database.execute(
                    task_status.update().where(task_status.c.task_id == task_id).values(
                        processed_files=processed_files,
                        progress=progress,
                        status="EXTRACTING"
                    )
                )
                
                logger.info(f"파일 {filename} 처리 완료: {len(patents)}개 특허 추출")
                
            except Exception as e:
                logger.error(f"Error processing {filename}: {e}")
        
        # 2단계: TF-IDF 벡터라이저 학습
        logger.info("TF-IDF 벡터라이저 학습 시작")
        corpus = prepare_corpus(all_patents)
        fit_tfidf_vectorizer(corpus)
        
        # 작업 상태 업데이트
        await database.execute(
            task_status.update().where(task_status.c.task_id == task_id).values(
                progress=60,
                status="VECTORIZING"
            )
        )
        
        # 3단계: 벡터화 및 저장
        total_patents = len(all_patents)
        logger.info(f"총 {total_patents}개 특허 벡터화 및 저장 시작")
        
        for i, patent_data in enumerate(all_patents):
            # 벡터 생성
            tfidf_vector, kobert_vector = generate_vectors(patent_data)
            
            # 데이터베이스에 저장
            query = patent.insert().values(
                patent_title=patent_data.get("title"),
                patent_application_number=patent_data.get("application_number"),
                patent_ipc=patent_data.get("ipc_classification"),
                patent_summary=patent_data.get("summary"),
                patent_claim=patent_data.get("claims"),
                patent_tfidf_vector=tfidf_vector.tobytes(),
                patent_kobert_vector=kobert_vector.tobytes()
            )
            
            await database.execute(query)
            
            # 작업 상태 업데이트
            if i % 100 == 0 or i == total_patents - 1:
                progress = 60 + int((i / total_patents) * 40)
                await database.execute(
                    task_status.update().where(task_status.c.task_id == task_id).values(
                        progress=progress,
                        status="VECTORIZING"
                    )
                )
                logger.info(f"진행률: {progress}%, {i+1}/{total_patents} 처리 완료")
        
        # 작업 완료
        await database.execute(
            task_status.update().where(task_status.c.task_id == task_id).values(
                progress=100,
                status="COMPLETED"
            )
        )
        
        logger.info(f"특허 데이터 처리 완료: {total_patents}개의 특허 저장")
        
    except Exception as e:
        logger.error(f"데이터 처리 중 오류 발생: {e}")
        await database.execute(
            task_status.update().where(task_status.c.task_id == task_id).values(
                status="FAILED"
            )
        )
        raise

@router.post("/extract-and-vectorize", response_model=ProcessResponse)
async def extract_and_vectorize(request: ProcessRequest, background_tasks: BackgroundTasks):
    """특허 RTF 파일에서 데이터 추출 및 벡터화 수행"""
    if not os.path.exists(request.rtf_directory):
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"디렉토리를 찾을 수 없습니다: {request.rtf_directory}"
        )
    
    # 폴더 내 RTF 파일 존재 여부 확인
    rtf_files = [f for f in os.listdir(request.rtf_directory) if f.endswith(".rtf")]
    if not rtf_files:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="지정한 디렉토리에 RTF 파일이 없습니다."
        )
    
    # 작업 ID 생성
    task_id = f"task_{uuid.uuid4().hex[:10]}"
    
    # 작업 상태 저장
    await database.execute(
        task_status.insert().values(
            task_id=task_id,
            status="STARTED",
            progress=0,
            total_files=0,
            processed_files=0
        )
    )
    
    # 백그라운드 작업 시작
    background_tasks.add_task(process_patent_files, request.rtf_directory, task_id)
    
    return {
        "status": True,
        "message": "특허 데이터 추출 및 벡터화 작업이 백그라운드에서 시작되었습니다.",
        "task_id": task_id,
        "directory": request.rtf_directory
    }
