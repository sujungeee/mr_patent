import dask
import dask.bag as db
from dask.distributed import Client, LocalCluster
import numpy as np
import os
from datetime import datetime
import time

from app.core.logging import logger
from app.core.database import database, patent
from app.services.vectorizer import get_tfidf_vector, get_kobert_vector, load_vectorizer

# 전역 변수로 워커별 벡터라이저 로드 상태 추적
_VECTORIZER_LOADED = False

def ensure_vectorizer_loaded():
    """워커당 한 번만 벡터라이저 로드"""
    global _VECTORIZER_LOADED
    if not _VECTORIZER_LOADED:
        from app.services.vectorizer import load_vectorizer
        load_vectorizer()
        _VECTORIZER_LOADED = True
        logger.info(f"워커 {os.getpid()}: 벡터라이저 로드 완료")

def vectorize_patent(patent_data):
    """단일 특허 벡터화 (벡터라이저 로드 체크 포함)"""
    # 워커당 한 번만 벡터라이저 로드
    ensure_vectorizer_loaded()
    
    try:
        from app.services.vectorizer import get_tfidf_vector, get_kobert_vector
        
        # 텍스트 필드 추출
        title = patent_data.get("title", "")
        summary = patent_data.get("summary", "")
        claims = patent_data.get("claims", "")
        
        # 필드별 벡터 생성
        title_tfidf = get_tfidf_vector(title)
        title_kobert = get_kobert_vector(title)
        summary_tfidf = get_tfidf_vector(summary)
        summary_kobert = get_kobert_vector(summary)
        claim_tfidf = get_tfidf_vector(claims)
        claim_kobert = get_kobert_vector(claims)
        
        # 출원번호 정제
        app_number = patent_data.get("application_number", "")
        if "발명의명칭" in app_number:
            app_number = app_number.replace("발명의명칭", "")
        
        # IPC 코드 길이 제한
        ipc_code = patent_data.get("ipc_classification", "")
        if ipc_code and len(ipc_code) > 95:
            ipc_code = ipc_code[:95]
        
        return {
            "patent_title": patent_data.get("title"),
            "patent_application_number": app_number,
            "patent_ipc": ipc_code,
            "patent_summary": patent_data.get("summary"),
            "patent_claim": patent_data.get("claims"),
            "patent_title_tfidf_vector": title_tfidf.tobytes(),
            "patent_title_kobert_vector": title_kobert.tobytes(),
            "patent_summary_tfidf_vector": summary_tfidf.tobytes(),
            "patent_summary_kobert_vector": summary_kobert.tobytes(),
            "patent_claim_tfidf_vector": claim_tfidf.tobytes(),
            "patent_claim_kobert_vector": claim_kobert.tobytes(),
            "patent_created_at": datetime.utcnow(),
            "patent_updated_at": datetime.utcnow()
        }
    except Exception as e:
        logger.error(f"특허 벡터화 중 오류: {str(e)}")
        return None

async def process_patents_with_dask(all_patents, batch_size=100000):
    """Dask를 사용한 특허 벡터화 처리 (최적화 버전)"""
    # Dask 클러스터 설정
    cluster = LocalCluster(
        n_workers=12,              # 6에서 12으로 증가
        threads_per_worker=2,      # 적절함
        memory_limit="28GB",       # 20GB에서 25GB로 증가
        timeout=7200               # 타임아웃 2시간으로 증가
    )
    client = Client(cluster)
    logger.info(f"Dask 대시보드: {client.dashboard_link}")
    
    # 배치 계산
    total_patents = len(all_patents)
    num_batches = (total_patents + batch_size - 1) // batch_size
    
    logger.info(f"총 {total_patents}개 특허를 {num_batches}개 배치로 처리 (배치 크기: {batch_size})")
    
    total_processed = 0
    start_time = time.time()
    
    try:
        for batch_idx in range(num_batches):
            batch_start = time.time()
            start_idx = batch_idx * batch_size
            end_idx = min((batch_idx + 1) * batch_size, total_patents)
            batch_patents = all_patents[start_idx:end_idx]
            
            logger.info(f"배치 {batch_idx+1}/{num_batches} 처리 중 ({len(batch_patents)}개 특허)")
            
            # Dask Bag 생성 및 처리
            # 최적화된 파티션 설정
            patents_bag = db.from_sequence(batch_patents, npartitions=20)  # 워커 수의 2배
            vectorized_patents = patents_bag.map(vectorize_patent).filter(lambda x: x is not None)
            
            # 결과 수집
            results = await client.compute(vectorized_patents)
            
            # 데이터베이스에 배치 단위로 저장
            db_batch_size = 500
            for i in range(0, len(results), db_batch_size):
                db_batch = results[i:i+db_batch_size]
                
                # 배치 삽입 실행
                try:
                    query = patent.insert().values(db_batch)
                    await database.execute(query)
                    total_processed += len(db_batch)
                except Exception as e:
                    logger.error(f"배치 삽입 중 오류: {str(e)}")
                    # 개별 삽입으로 대체
                    for item in db_batch:
                        try:
                            query = patent.insert().values(item)
                            await database.execute(query)
                            total_processed += 1
                        except Exception as inner_e:
                            logger.error(f"개별 삽입 중 오류: {str(inner_e)}")
            
            # 성능 지표 계산 및 로깅
            batch_duration = time.time() - batch_start
            patents_per_second = len(batch_patents) / batch_duration if batch_duration > 0 else 0
            
            logger.info(f"배치 {batch_idx+1}/{num_batches} 완료: {len(batch_patents)}개 특허를 {batch_duration:.2f}초에 처리 ({patents_per_second:.2f} 특허/초)")
            
            # 예상 남은 시간 계산
            elapsed_time = time.time() - start_time
            avg_time_per_batch = elapsed_time / (batch_idx + 1)
            remaining_batches = num_batches - (batch_idx + 1)
            est_time_remaining = avg_time_per_batch * remaining_batches
            
            logger.info(f"진행 상황: {batch_idx+1}/{num_batches} 배치. 예상 남은 시간: {est_time_remaining/60:.2f}분")
    
    except Exception as e:
        logger.error(f"Dask 처리 중 오류: {str(e)}")
    finally:
        # 리소스 정리
        client.close()
        cluster.close()
    
    total_duration = time.time() - start_time
    logger.info(f"Dask 처리 완료: {total_processed}개 특허를 {total_duration/60:.2f}분에 처리")
    return total_processed
