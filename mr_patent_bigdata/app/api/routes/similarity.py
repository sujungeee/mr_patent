# app/api/routes/similarity.py

from fastapi import APIRouter, HTTPException, Query
from typing import Dict, Any, List
from datetime import datetime, timezone
import numpy as np
from sklearn.metrics.pairwise import cosine_similarity

from app.core.database import database
from app.services.vectorizer import get_tfidf_vector, get_kobert_vector

router = APIRouter(prefix="/api/similarity", tags=["similarity"])

def get_current_timestamp():
    """현재 시간을 ISO 8601 형식으로 변환 (UTC)"""
    return datetime.now(timezone.utc).isoformat().replace('+00:00', 'Z')

@router.post("/calculate/{user_patent_folder_id}", response_model=Dict[str, Any])
async def calculate_similarity(user_patent_folder_id: int, top_k: int = 3):
    """특허 초안과 유사한 특허 검색 및 저장"""
    # 1. 특허 초안 조회
    draft_query = """
    SELECT * FROM patent_draft 
    WHERE user_patent_folder_id = :folder_id
    """
    draft = await database.fetch_one(
        query=draft_query,
        values={"folder_id": user_patent_folder_id}
    )
    
    if not draft:
        raise HTTPException(
            status_code=404,
            detail={
                "code": "DRAFT_NOT_FOUND",
                "message": "해당 폴더의 특허 초안을 찾을 수 없습니다.",
                "timestamp": get_current_timestamp()
            }
        )
    
    # 2. 트랜잭션 시작
    async with database.transaction():
        # 3. 유사도 엔티티 생성
        now = datetime.now(timezone.utc)
        similarity_query = """
        INSERT INTO similarity (
            user_patent_folder_id, 
            similarity_created_at, 
            similarity_updated_at
        ) VALUES (
            :folder_id,
            :created_at,
            :updated_at
        )
        """
        
        similarity_id = await database.execute(
            query=similarity_query,
            values={
                "folder_id": user_patent_folder_id,
                "created_at": now,
                "updated_at": now
            }
        )
        
        # 4. 유사도 계산
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
        
        while True:
            # 특허 배치 조회
            patents_query = """
            SELECT patent_id, patent_title, patent_summary, patent_claim,
                   patent_tfidf_vector, patent_kobert_vector
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
                    "title_similarity": title_similarity,
                    "summary_similarity": summary_similarity,
                    "claim_similarity": claim_similarity,
                    "overall_similarity": overall_similarity
                })
            
            offset += limit
        
        # 유사도 기준 정렬
        top_similar_patents.sort(key=lambda x: x["overall_similarity"], reverse=True)
        top_k_patents = top_similar_patents[:top_k]
        
        # 5. 유사 특허 저장
        for idx, patent in enumerate(top_k_patents):
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
            
            await database.execute(
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
        
        # 6. 결과 반환
        result_patents = []
        for patent in top_k_patents:
            patent_detail = await database.fetch_one(
                query="SELECT * FROM patent WHERE patent_id = :id",
                values={"id": patent["patent_id"]}
            )
            
            result_patents.append({
                "patent_id": patent["patent_id"],
                "patent_title": patent_detail["patent_title"],
                "patent_application_number": patent_detail["patent_application_number"],
                "patent_ipc": patent_detail["patent_ipc"],
                "similarity_score": {
                    "overall": round(patent["overall_similarity"] * 100, 2),
                    "title": round(patent["title_similarity"] * 100, 2),
                    "summary": round(patent["summary_similarity"] * 100, 2),
                    "claim": round(patent["claim_similarity"] * 100, 2)
                }
            })
        
        return {
            "data": {
                "similarity_id": similarity_id,
                "similar_patents": result_patents
            },
            "timestamp": get_current_timestamp()
        }

# 적합도 검사 API 추가
@router.post("/fitness/{user_patent_folder_id}", response_model=Dict[str, Any])
async def check_patent_fitness(user_patent_folder_id: int):
    """특허 초안의 각 항목 적합도 검사"""
    # 특허 초안 조회
    draft_query = """
    SELECT * FROM patent_draft 
    WHERE user_patent_folder_id = :folder_id
    """
    draft = await database.fetch_one(
        query=draft_query,
        values={"folder_id": user_patent_folder_id}
    )
    
    if not draft:
        raise HTTPException(
            status_code=404,
            detail={
                "code": "DRAFT_NOT_FOUND",
                "message": "해당 폴더의 특허 초안을 찾을 수 없습니다.",
                "timestamp": get_current_timestamp()
            }
        )
    
    # 각 항목별 적합도 검사
    fitness_results = {}
    now = datetime.now(timezone.utc)
    
    # 제목 적합도
    title = draft["patent_draft_title"]
    title_fitness = check_title_fitness(title)
    fitness_results["title"] = title_fitness
    
    # 기술 분야 적합도
    tech_field = draft["patent_draft_technical_field"]
    tech_field_fitness = check_tech_field_fitness(tech_field)
    fitness_results["technical_field"] = tech_field_fitness
    
    # 배경기술 적합도
    background = draft["patent_draft_background"]
    background_fitness = check_background_fitness(background)
    fitness_results["background"] = background_fitness
    
    # 해결과제 적합도
    problem = draft["patent_draft_problem"]
    problem_fitness = check_problem_fitness(problem)
    fitness_results["problem"] = problem_fitness
    
    # 해결수단 적합도
    solution = draft["patent_draft_solution"]
    solution_fitness = check_solution_fitness(solution)
    fitness_results["solution"] = solution_fitness
    
    # 발명효과 적합도
    effect = draft["patent_draft_effect"]
    effect_fitness = check_effect_fitness(effect)
    fitness_results["effect"] = effect_fitness
    
    # 청구항 적합도
    claim = draft["patent_draft_claim"]
    claim_fitness = check_claim_fitness(claim)
    fitness_results["claim"] = claim_fitness
    
    # 종합 적합도 판정
    is_corrected = all([
        result["pass"] for result in fitness_results.values()
    ])
    
    # DB에 적합도 결과 저장
    fitness_query = """
    INSERT INTO fitness (
        user_patent_folder_id,
        fitness_good_content,
        fitness_is_corrected,
        fitness_created_at,
        fitness_updated_at
    ) VALUES (
        :folder_id,
        :good_content,
        :is_corrected,
        :created_at,
        :updated_at
    )
    """
    
    fitness_id = await database.execute(
        query=fitness_query,
        values={
            "folder_id": user_patent_folder_id,
            "good_content": fitness_results,
            "is_corrected": 1 if is_corrected else 0,
            "created_at": now,
            "updated_at": now
        }
    )
    
    return {
        "data": {
            "fitness_id": fitness_id,
            "results": fitness_results,
            "is_corrected": is_corrected
        },
        "timestamp": get_current_timestamp()
    }

# 적합도 검사 도우미 함수들
def check_title_fitness(title: str) -> Dict[str, Any]:
    """제목 적합도 검사"""
    # 구현 예시 (실제로는 더 복잡한 로직이 필요할 수 있음)
    if not title:
        return {"pass": False, "message": "제목이 비어있습니다."}
    
    if len(title) < 5:
        return {"pass": False, "message": "제목이 너무 짧습니다. 더 상세한 제목이 필요합니다."}
    
    if len(title) > 100:
        return {"pass": False, "message": "제목이 너무 깁니다. 더 간결한 제목이 필요합니다."}
    
    return {"pass": True, "message": "적합한 제목입니다."}

def check_tech_field_fitness(tech_field: str) -> Dict[str, Any]:
    """기술 분야 적합도 검사"""
    if not tech_field:
        return {"pass": False, "message": "기술 분야가 비어있습니다."}
    
    if len(tech_field) < 20:
        return {"pass": False, "message": "기술 분야 설명이 너무 짧습니다. 더 상세한 설명이 필요합니다."}
    
    if "본 발명은" not in tech_field:
        return {"pass": False, "message": "기술 분야에는 '본 발명은 ..에 관한 것이다'와 같은 형식이 필요합니다."}
    
    return {"pass": True, "message": "적합한 기술 분야 설명입니다."}

def check_background_fitness(background: str) -> Dict[str, Any]:
    """배경기술 적합도 검사"""
    if not background:
        return {"pass": False, "message": "배경기술이 비어있습니다."}
    
    if len(background) < 50:
        return {"pass": False, "message": "배경기술 설명이 너무 짧습니다. 기존 기술의 문제점을 충분히 설명해야 합니다."}
    
    return {"pass": True, "message": "적합한 배경기술 설명입니다."}

def check_problem_fitness(problem: str) -> Dict[str, Any]:
    """해결과제 적합도 검사"""
    if not problem:
        return {"pass": False, "message": "해결과제가 비어있습니다."}
    
    if len(problem) < 30:
        return {"pass": False, "message": "해결과제 설명이 너무 짧습니다. 해결하려는 문제를 명확히 설명해야 합니다."}
    
    return {"pass": True, "message": "적합한 해결과제 설명입니다."}

def check_solution_fitness(solution: str) -> Dict[str, Any]:
    """해결수단 적합도 검사"""
    if not solution:
        return {"pass": False, "message": "해결수단이 비어있습니다."}
    
    if len(solution) < 50:
        return {"pass": False, "message": "해결수단 설명이 너무 짧습니다. 발명의 핵심 구성을 상세히 설명해야 합니다."}
    
    return {"pass": True, "message": "적합한 해결수단 설명입니다."}

def check_effect_fitness(effect: str) -> Dict[str, Any]:
    """발명효과 적합도 검사"""
    if not effect:
        return {"pass": False, "message": "발명효과가 비어있습니다."}
    
    if len(effect) < 30:
        return {"pass": False, "message": "발명효과 설명이 너무 짧습니다. 발명의 장점과 효과를 충분히 설명해야 합니다."}
    
    return {"pass": True, "message": "적합한 발명효과 설명입니다."}

def check_claim_fitness(claim: str) -> Dict[str, Any]:
    """청구항 적합도 검사"""
    if not claim:
        return {"pass": False, "message": "청구항이 비어있습니다."}
    
    if len(claim) < 50:
        return {"pass": False, "message": "청구항이 너무 짧습니다. 발명의 보호범위를 명확히 정의해야 합니다."}
    
    if "청구항 1" not in claim and "[청구항 1]" not in claim:
        return {"pass": False, "message": "청구항 형식이 올바르지 않습니다. '청구항 1' 또는 '[청구항 1]'으로 시작해야 합니다."}
    
    return {"pass": True, "message": "적합한 청구항입니다."}
