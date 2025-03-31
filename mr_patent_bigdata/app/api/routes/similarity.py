from fastapi import APIRouter, HTTPException, BackgroundTasks
from typing import Dict, Any, List
from datetime import datetime, timezone
import numpy as np
from sklearn.metrics.pairwise import cosine_similarity
import json  # JSON 직렬화를 위해 추가

from app.core.database import database
from app.services.vectorizer import get_tfidf_vector, get_kobert_vector
from app.services.kipris import get_patent_public_info, download_patent_pdf

router = APIRouter(prefix="/api", tags=["similarity"])

def get_current_timestamp():
    """현재 시간을 ISO 8601 형식으로 변환 (UTC)"""
    return datetime.now(timezone.utc).isoformat().replace('+00:00', 'Z')

@router.post("/draft/{patent_draft_id}/similarity-check", response_model=Dict[str, Any])
async def run_similarity_check(patent_draft_id: int, background_tasks: BackgroundTasks):
    """특허 초안의 적합도 검사, 유사도 분석, 상세 비교를 모두 수행 (비동기)"""
    # 1. 특허 초안 존재 확인
    draft_query = """
    SELECT * FROM patent_draft 
    WHERE patent_draft_id = :draft_id
    """
    draft = await database.fetch_one(
        query=draft_query,
        values={"draft_id": patent_draft_id}
    )
    
    if not draft:
        raise HTTPException(
            status_code=404,
            detail={
                "code": "DRAFT_NOT_FOUND",
                "message": "해당 ID의 특허 초안을 찾을 수 없습니다.",
                "timestamp": get_current_timestamp()
            }
        )
    
    # 2. similarity 엔티티 생성 (분석 상태 표시)
    now = datetime.now(timezone.utc)
    similarity_query = """
    INSERT INTO similarity (
        patent_draft_id, 
        similarity_created_at, 
        similarity_updated_at
    ) VALUES (
        :draft_id,
        :created_at,
        :updated_at
    )
    """
    
    similarity_id = await database.execute(
        query=similarity_query,
        values={
            "draft_id": patent_draft_id,
            "created_at": now,
            "updated_at": now
        }
    )
    
    # 3. 백그라운드에서 분석 작업 실행
    background_tasks.add_task(
        process_full_similarity_analysis, 
        patent_draft_id,
        similarity_id
    )
    
    # 4. 즉시 응답 반환 (작업 시작됨)
    return {
        "data": {
            "similarity_id": similarity_id,
            "status": "ANALYZING"
        },
        "timestamp": get_current_timestamp()
    }

@router.get("/patent/fitness/{patent_draft_id}", response_model=Dict[str, Any])
async def get_fitness_result(patent_draft_id: int):
    """특허 초안의 적합도 검사 결과 조회"""
    # 적합도 결과 조회
    fitness_query = """
    SELECT * FROM fitness 
    WHERE patent_draft_id = :draft_id
    ORDER BY fitness_created_at DESC 
    LIMIT 1
    """
    
    fitness = await database.fetch_one(
        query=fitness_query,
        values={"draft_id": patent_draft_id}
    )
    
    if not fitness:
        raise HTTPException(
            status_code=404,
            detail={
                "code": "FITNESS_NOT_FOUND",
                "message": "적합도 검사 결과가 없습니다.",
                "timestamp": get_current_timestamp()
            }
        )
    
    return {
        "data": {
            "fitness_id": fitness["fitness_id"],
            "fitness_good_content": fitness["fitness_good_content"],
            "fitness_is_corrected": fitness["fitness_is_corrected"]
        },
        "timestamp": get_current_timestamp()
    }

@router.get("/patent/similarity/{patent_draft_id}", response_model=Dict[str, Any])
async def get_similarity_result(patent_draft_id: int):
    """특허 유사도 분석 결과 조회"""
    # 최신 유사도 분석 결과 조회
    similarity_query = """
    SELECT * FROM similarity 
    WHERE patent_draft_id = :draft_id
    ORDER BY similarity_created_at DESC 
    LIMIT 1
    """
    
    similarity = await database.fetch_one(
        query=similarity_query,
        values={"draft_id": patent_draft_id}
    )
    
    if not similarity:
        raise HTTPException(
            status_code=404,
            detail={
                "code": "SIMILARITY_NOT_FOUND", 
                "message": "유사도 분석 결과가 없습니다.",
                "timestamp": get_current_timestamp()
            }
        )
    
    # 유사한 특허 목록 조회
    similar_patents_query = """
    SELECT sp.*, p.patent_title, p.patent_application_number
    FROM similarity_patent sp
    JOIN patent p ON sp.patent_id = p.patent_id
    WHERE sp.similarity_id = :similarity_id
    ORDER BY sp.similarity_patent_score DESC
    """
    
    similar_patents = await database.fetch_all(
        query=similar_patents_query,
        values={"similarity_id": similarity["similarity_id"]}
    )
    
    # 결과 포맷팅
    result_patents = []
    for patent in similar_patents:
        result_patents.append({
            "similarity_patent_id": patent["similarity_patent_id"],
            "patent_id": patent["patent_id"],
            "patent_title": patent["patent_title"],
            "similarity_patent_score": float(patent["similarity_patent_score"])
        })
    
    return {
        "data": {
            "similar_patents": result_patents
        },
        "timestamp": get_current_timestamp()
    }

# 백그라운드 처리 함수
async def process_full_similarity_analysis(patent_draft_id: int, similarity_id: int):
    """적합도 검사, 유사도 분석, 상세 비교를 순차적으로 모두 수행"""
    try:
        # 1단계: 적합도 검사 수행
        await perform_fitness_check(patent_draft_id)
        
        # 2단계: 유사도 분석 수행
        similar_patents = await perform_similarity_analysis(patent_draft_id, similarity_id)
        
        # 3단계: 상위 3개 특허에 대한 공고전문 가져오기와 상세 비교
        top_patents = similar_patents[:3]  # 상위 3개만 선택
        for patent in top_patents:
            # 공고전문 정보 가져오기 (KIPRIS API 호출)
            patent_public_id = await fetch_patent_public(patent["patent_id"], patent["patent_application_number"])
            
            if patent_public_id:
                # 상세 비교 수행
                await perform_detailed_comparison(
                    patent_draft_id, 
                    patent["similarity_patent_id"], 
                    patent_public_id
                )
    except Exception as e:
        # 오류 발생 시 로깅
        print(f"유사도 분석 중 오류 발생: {str(e)}")

async def perform_fitness_check(patent_draft_id: int):
    """특허 초안의 적합도 검사 수행"""
    # 특허 초안 조회
    draft_query = """
    SELECT * FROM patent_draft 
    WHERE patent_draft_id = :draft_id
    """
    draft = await database.fetch_one(
        query=draft_query,
        values={"draft_id": patent_draft_id}
    )
    
    if not draft:
        raise Exception("특허 초안을 찾을 수 없습니다.")
    
    # 각 항목별 적합도 검사
    fitness_results = {}
    now = datetime.now(timezone.utc)
    
    # 제목 적합도
    title = draft["patent_draft_title"]
    title_fitness = check_title_fitness(title)
    fitness_results["patent_draft_title"] = title_fitness["pass"]
    
    # 기술 분야 적합도
    tech_field = draft["patent_draft_technical_field"]
    tech_field_fitness = check_field_fitness(tech_field, "technical_field")
    fitness_results["patent_draft_technical_field"] = tech_field_fitness["pass"]
    
    # 배경기술 적합도
    background = draft["patent_draft_background"]
    background_fitness = check_field_fitness(background, "background")
    fitness_results["patent_draft_background"] = background_fitness["pass"]
    
    # 해결과제 적합도
    problem = draft["patent_draft_problem"]
    problem_fitness = check_field_fitness(problem, "problem")
    fitness_results["patent_draft_problem"] = problem_fitness["pass"]
    
    # 해결수단 적합도
    solution = draft["patent_draft_solution"]
    solution_fitness = check_field_fitness(solution, "solution")
    fitness_results["patent_draft_solution"] = solution_fitness["pass"]
    
    # 발명효과 적합도
    effect = draft["patent_draft_effect"]
    effect_fitness = check_field_fitness(effect, "effect")
    fitness_results["patent_draft_effect"] = effect_fitness["pass"]
    
    # 청구항 적합도
    claim = draft["patent_draft_claim"]
    claim_fitness = check_field_fitness(claim, "claim")
    fitness_results["patent_draft_claim"] = claim_fitness["pass"]
    
    # 종합 적합도 판정
    is_corrected = all(fitness_results.values())
    
    # DB에 적합도 결과 저장
    fitness_query = """
    INSERT INTO fitness (
        patent_draft_id,
        fitness_good_content,
        fitness_is_corrected,
        fitness_created_at,
        fitness_updated_at
    ) VALUES (
        :draft_id,
        :good_content,
        :is_corrected,
        :created_at,
        :updated_at
    )
    """
    
    await database.execute(
        query=fitness_query,
        values={
            "draft_id": patent_draft_id,
            "good_content": json.dumps(fitness_results),  # 딕셔너리를 JSON으로 직렬화
            "is_corrected": 1 if is_corrected else 0,
            "created_at": now,
            "updated_at": now
        }
    )
    
    return fitness_results

async def perform_similarity_analysis(patent_draft_id: int, similarity_id: int):
    """특허 초안과 유사한 특허 분석"""
    # 특허 초안 조회
    draft_query = """
    SELECT * FROM patent_draft 
    WHERE patent_draft_id = :draft_id
    """
    draft = await database.fetch_one(
        query=draft_query,
        values={"draft_id": patent_draft_id}
    )
    
    if not draft:
        raise Exception("특허 초안을 찾을 수 없습니다.")
    
    # 초안에서 TF-IDF와 KoBERT 벡터 모두 추출
    draft_title_tfidf = np.frombuffer(draft["patent_draft_title_tfidf_vector"])
    draft_summary_tfidf = np.frombuffer(draft["patent_draft_summary_tfidf_vector"])
    draft_claim_tfidf = np.frombuffer(draft["patent_draft_claim_tfidf_vector"])
    
    # KoBERT 벡터 추출
    draft_title_kobert = np.frombuffer(draft["patent_draft_title_kobert_vector"])
    draft_summary_kobert = np.frombuffer(draft["patent_draft_summary_kobert_vector"])
    draft_claim_kobert = np.frombuffer(draft["patent_draft_claim_kobert_vector"])
    
    # 특허 검색 쿼리 (페이징 처리)
    limit = 1000
    offset = 0
    top_similar_patents = []
    now = datetime.now(timezone.utc)
    
    while True:
        # 특허 배치 조회
        patents_query = """
        SELECT patent_id, patent_title, patent_summary, patent_claim,
               patent_application_number, patent_tfidf_vector, patent_kobert_vector
        FROM patent
        LIMIT :limit OFFSET :offset
        """
        
        patents = await database.fetch_all(
            query=patents_query,
            values={"limit": limit, "offset": offset}
        )
        
        if not patents:
            break
            
        # 각 특허와 유사도 계산
        for patent in patents:
            # TF-IDF 벡터 변환
            patent_tfidf_vector = np.frombuffer(patent["patent_tfidf_vector"])
            # KoBERT 벡터 변환
            patent_kobert_vector = np.frombuffer(patent["patent_kobert_vector"])
            
            # TF-IDF 기반 유사도 계산
            title_tfidf_similarity = float(cosine_similarity([draft_title_tfidf], [patent_tfidf_vector])[0][0])
            summary_tfidf_similarity = float(cosine_similarity([draft_summary_tfidf], [patent_tfidf_vector])[0][0])
            claim_tfidf_similarity = float(cosine_similarity([draft_claim_tfidf], [patent_tfidf_vector])[0][0])
            
            # KoBERT 기반 유사도 계산
            title_kobert_similarity = float(cosine_similarity([draft_title_kobert], [patent_kobert_vector])[0][0])
            summary_kobert_similarity = float(cosine_similarity([draft_summary_kobert], [patent_kobert_vector])[0][0])
            claim_kobert_similarity = float(cosine_similarity([draft_claim_kobert], [patent_kobert_vector])[0][0])
            
            # 가중치 적용 (TF-IDF 50%, KoBERT 50%)
            title_similarity = 0.5 * title_tfidf_similarity + 0.5 * title_kobert_similarity
            summary_similarity = 0.5 * summary_tfidf_similarity + 0.5 * summary_kobert_similarity
            claim_similarity = 0.5 * claim_tfidf_similarity + 0.5 * claim_kobert_similarity
            
            # 필드별 가중치 적용한 전체 유사도
            overall_similarity = (0.3 * title_similarity + 0.3 * summary_similarity + 0.4 * claim_similarity)
            
            top_similar_patents.append({
                "patent_id": patent["patent_id"],
                "patent_application_number": patent["patent_application_number"],
                "title_similarity": title_similarity,
                "summary_similarity": summary_similarity,
                "claim_similarity": claim_similarity,
                "overall_similarity": overall_similarity
            })
        
        offset += limit
    
    # 유사도 기준 정렬
    top_similar_patents.sort(key=lambda x: x["overall_similarity"], reverse=True)
    
    # 결과 저장
    for patent in top_similar_patents[:10]:  # 상위 10개만 저장
        similarity_patent_query = """
        INSERT INTO similarity_patent (
            patent_id,
            similarity_id,
            similarity_patent_score,
            similarity_patent_claim,
            similarity_patent_summary,
            similarity_patent_title,
            similarity_patent_created_at,
            similarity_patent_updated_at
        ) VALUES (
            :patent_id,
            :similarity_id,
            :overall_score,
            :claim_score,
            :summary_score,
            :title_score,
            :created_at,
            :updated_at
        )
        """
        
        similarity_patent_id = await database.execute(
            query=similarity_patent_query,
            values={
                "patent_id": patent["patent_id"],
                "similarity_id": similarity_id,
                "overall_score": patent["overall_similarity"],
                "claim_score": patent["claim_similarity"],
                "summary_score": patent["summary_similarity"],
                "title_score": patent["title_similarity"],
                "created_at": now,
                "updated_at": now
            }
        )
        
        # ID 추가
        patent["similarity_patent_id"] = similarity_patent_id
    
    return top_similar_patents

async def fetch_patent_public(patent_id: int, application_number: str):
    """KIPRIS API를 사용해 특허 공고전문 정보 가져오기"""
    # 이미 가져온 공고전문이 있는지 확인
    existing_query = """
    SELECT patent_public_id FROM patent_public
    WHERE patent_id = :patent_id
    """
    
    existing = await database.fetch_one(
        query=existing_query,
        values={"patent_id": patent_id}
    )
    
    if existing:
        return existing["patent_public_id"]
    
    # KIPRIS API 호출 (별도 서비스 구현 필요)
    try:
        now = datetime.now(timezone.utc)
        
        # API에서 특허 정보 가져오기 
        patent_info = await get_patent_public_info(application_number)
        
        if not patent_info or "publication_number" not in patent_info:
            return None
        
        # PDF 다운로드
        pdf_path, pdf_name = await download_patent_pdf(patent_info["publication_number"])
        
        # 공고전문 내용 추출 (OCR 등 필요 - 여기서는 간단한 예시로)
        content = f"특허 공고전문 내용 (출원번호: {application_number})"
        
        # DB에 저장
        insert_query = """
        INSERT INTO patent_public (
            patent_id,
            patent_public_number,
            patent_public_pdf_path,
            patent_public_pdf_name,
            patent_public_content,
            patent_public_api_response,
            patent_public_is_processed,
            patent_public_retrieved_at,
            patent_public_created_at,
            patent_public_updated_at
        ) VALUES (
            :patent_id,
            :public_number,
            :pdf_path,
            :pdf_name,
            :content,
            :api_response,
            :is_processed,
            :retrieved_at,
            :created_at,
            :updated_at
        )
        """
        
        patent_public_id = await database.execute(
            query=insert_query,
            values={
                "patent_id": patent_id,
                "public_number": patent_info["publication_number"],
                "pdf_path": pdf_path,
                "pdf_name": pdf_name,
                "content": content,
                "api_response": json.dumps(patent_info),  # 딕셔너리를 JSON으로 직렬화
                "is_processed": 1,
                "retrieved_at": now,
                "created_at": now,
                "updated_at": now
            }
        )
        
        return patent_public_id
    
    except Exception as e:
        print(f"공고전문 가져오기 중 오류: {str(e)}")
        return None

async def perform_detailed_comparison(patent_draft_id: int, similarity_patent_id: int, patent_public_id: int):
    """특허 초안과 공고전문의 상세 비교 수행"""
    try:
        now = datetime.now(timezone.utc)
        
        # 초안 정보 가져오기
        draft_query = """
        SELECT * FROM patent_draft 
        WHERE patent_draft_id = :draft_id
        """
        draft = await database.fetch_one(
            query=draft_query,
            values={"draft_id": patent_draft_id}
        )
        
        # 공고전문 정보 가져오기
        public_query = """
        SELECT * FROM patent_public 
        WHERE patent_public_id = :public_id
        """
        public = await database.fetch_one(
            query=public_query,
            values={"public_id": patent_public_id}
        )
        
        if not draft or not public:
            raise Exception("초안 또는 공고전문 정보를 찾을 수 없습니다.")
        
        # 상세 비교 (여기서는 간단한 예시)
        comparison_result = "초안과 공고전문의 상세 비교 결과"
        
        # 문맥 데이터 (유사한 부분 하이라이팅 등)
        context_data = {
            "title_comparison": {
                "similarity": 0.85,
                "highlights": [
                    {"original": "신발내부설치용에어쿠션", "similar": "신발내부설치용에어쿠션시트"}
                ]
            },
            "claim_comparison": {
                "similarity": 0.78,
                "highlights": [
                    {"original": "에어쿠션층", "similar": "공기층"}
                ]
            }
        }
        
        # 총점 계산
        total_score = 0.82  # 예시 점수
        
        # DB에 저장
        insert_query = """
        INSERT INTO detailed_comparison (
            patent_draft_id,
            patent_public_id,
            similarity_patent_id,
            detailed_comparison_result,
            detailed_comparison_context,
            detailed_comparison_total_score,
            detailed_comparison_created_at,
            detailed_comparison_updated_at
        ) VALUES (
            :draft_id,
            :public_id,
            :similarity_id,
            :result,
            :context,
            :score,
            :created_at,
            :updated_at
        )
        """
        
        await database.execute(
            query=insert_query,
            values={
                "draft_id": patent_draft_id,
                "public_id": patent_public_id,
                "similarity_id": similarity_patent_id,
                "result": comparison_result,
                "context": json.dumps(context_data),  # 딕셔너리를 JSON으로 직렬화
                "score": total_score,
                "created_at": now,
                "updated_at": now
            }
        )
        
    except Exception as e:
        print(f"상세 비교 중 오류: {str(e)}")

# 적합도 검사 도우미 함수들
def check_title_fitness(title: str) -> Dict[str, Any]:
    """제목 적합도 검사"""
    if not title:
        return {"pass": False, "message": "제목이 비어있습니다."}
    
    if len(title) < 5:
        return {"pass": False, "message": "제목이 너무 짧습니다. 더 상세한 제목이 필요합니다."}
    
    if len(title) > 100:
        return {"pass": False, "message": "제목이 너무 깁니다. 더 간결한 제목이 필요합니다."}
    
    return {"pass": True, "message": "적합한 제목입니다."}

def check_field_fitness(text: str, field_type: str) -> Dict[str, Any]:
    """필드 적합도 검사"""
    if not text:
        return {"pass": False, "message": f"{field_type} 내용이 비어있습니다."}
    
    min_length = {
        "technical_field": 10,
        "background": 10,
        "problem": 10,
        "solution": 10,
        "effect": 10,
        "claim": 10
    }.get(field_type, 10)
    
    if len(text) < min_length:
        return {"pass": False, "message": f"{field_type} 내용이 너무 짧습니다."}
    
    return {"pass": True, "message": f"적합한 {field_type} 내용입니다."}
