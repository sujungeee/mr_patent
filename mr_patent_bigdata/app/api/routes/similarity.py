from fastapi import APIRouter, HTTPException, BackgroundTasks
from typing import Dict, Any, List
from datetime import datetime, timezone
import numpy as np
from sklearn.metrics.pairwise import cosine_similarity
import json
import re

from app.core.database import database
from app.services.vectorizer import get_tfidf_vector, get_bert_vector
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

async def perform_similarity_analysis(patent_draft_id: int, similarity_id: int):
    """특허 초안과 유사한 특허 분석 - 하이브리드 접근법 (TF-IDF로 필터링 후 BERT로 정밀 분석)"""
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
    
    # 초안에서 TF-IDF 벡터 추출
    draft_title_tfidf = np.frombuffer(draft["patent_draft_title_tfidf_vector"])
    draft_summary_tfidf = np.frombuffer(draft["patent_draft_summary_tfidf_vector"])
    draft_claim_tfidf = np.frombuffer(draft["patent_draft_claim_tfidf_vector"])
    
    # 1단계: TF-IDF 기반 초기 유사도 계산 (모든 특허에 대해)
    limit = 1000
    offset = 0
    all_tfidf_candidates = []
    now = datetime.now(timezone.utc)
    
    while True:
        # 특허 배치 조회 (TF-IDF 벡터만 가져옴 - 메모리 절약)
        patents_query = """
        SELECT patent_id, patent_title, patent_summary, patent_claim,
               patent_application_number, 
               patent_title_tfidf_vector,
               patent_summary_tfidf_vector,
               patent_claim_tfidf_vector
        FROM patent
        LIMIT :limit OFFSET :offset
        """
        
        patents = await database.fetch_all(
            query=patents_query,
            values={"limit": limit, "offset": offset}
        )
        
        if not patents:
            break
            
        # 각 특허와 TF-IDF 유사도 계산
        for patent in patents:
            try:
                # TF-IDF 벡터 추출
                zero_tfidf = np.zeros(1000)
                
                patent_title_tfidf = np.frombuffer(patent["patent_title_tfidf_vector"]) if patent["patent_title_tfidf_vector"] else zero_tfidf
                patent_summary_tfidf = np.frombuffer(patent["patent_summary_tfidf_vector"]) if patent["patent_summary_tfidf_vector"] else zero_tfidf
                patent_claim_tfidf = np.frombuffer(patent["patent_claim_tfidf_vector"]) if patent["patent_claim_tfidf_vector"] else zero_tfidf
                
                # TF-IDF 기반 유사도 계산
                title_tfidf_similarity = float(cosine_similarity([draft_title_tfidf], [patent_title_tfidf])[0][0])
                summary_tfidf_similarity = float(cosine_similarity([draft_summary_tfidf], [patent_summary_tfidf])[0][0])
                claim_tfidf_similarity = float(cosine_similarity([draft_claim_tfidf], [patent_claim_tfidf])[0][0])
                
                # 필드별 가중치 적용한 전체 유사도
                overall_tfidf_similarity = (0.3 * title_tfidf_similarity + 0.3 * summary_tfidf_similarity + 0.4 * claim_tfidf_similarity)
                
                all_tfidf_candidates.append({
                    "patent_id": patent["patent_id"],
                    "patent_title": patent["patent_title"],
                    "patent_summary": patent["patent_summary"],
                    "patent_claim": patent["patent_claim"],
                    "patent_application_number": patent["patent_application_number"],
                    "tfidf_similarity": overall_tfidf_similarity
                })
            except Exception as e:
                print(f"특허 {patent['patent_id']} TF-IDF 유사도 계산 중 오류: {str(e)}")
                continue
        
        offset += limit
    
    # TF-IDF 유사도 기준 정렬
    all_tfidf_candidates.sort(key=lambda x: x["tfidf_similarity"], reverse=True)
    
    # 2단계: 상위 10개 후보에 대해서만 BERT 유사도 계산
    top_10_candidates = all_tfidf_candidates[:10]
    final_results = []
    
    for candidate in top_10_candidates:
        # 실시간 BERT 벡터 생성 및 유사도 계산
        # 텍스트 길이 제한 및 전처리
        draft_title = draft["patent_draft_title"][:500]
        draft_summary = draft["patent_draft_summary"][:500]
        draft_claim = draft["patent_draft_claim"][:500]
        
        patent_title = candidate["patent_title"][:500] if candidate["patent_title"] else ""
        patent_summary = candidate["patent_summary"][:500] if candidate["patent_summary"] else ""
        patent_claim = candidate["patent_claim"][:500] if candidate["patent_claim"] else ""
        
        # BERT 벡터 생성
        draft_title_bert = get_bert_vector(draft_title)
        draft_summary_bert = get_bert_vector(draft_summary)
        draft_claim_bert = get_bert_vector(draft_claim)
        
        patent_title_bert = get_bert_vector(patent_title)
        patent_summary_bert = get_bert_vector(patent_summary)
        patent_claim_bert = get_bert_vector(patent_claim)
        
        # BERT 유사도 계산
        title_bert_similarity = float(cosine_similarity([draft_title_bert], [patent_title_bert])[0][0])
        summary_bert_similarity = float(cosine_similarity([draft_summary_bert], [patent_summary_bert])[0][0])
        claim_bert_similarity = float(cosine_similarity([draft_claim_bert], [patent_claim_bert])[0][0])
        
        # 최종 유사도 계산 (TF-IDF 30%, BERT 70%)
        title_similarity = 0.3 * candidate["tfidf_similarity"] + 0.7 * title_bert_similarity
        summary_similarity = 0.3 * candidate["tfidf_similarity"] + 0.7 * summary_bert_similarity
        claim_similarity = 0.3 * candidate["tfidf_similarity"] + 0.7 * claim_bert_similarity
        
        # 필드별 가중치 적용한 전체 유사도
        overall_similarity = (0.3 * title_similarity + 0.3 * summary_similarity + 0.4 * claim_similarity)
        
        final_results.append({
            "patent_id": candidate["patent_id"],
            "patent_application_number": candidate["patent_application_number"],
            "title_similarity": title_similarity,
            "summary_similarity": summary_similarity,
            "claim_similarity": claim_similarity,
            "overall_similarity": overall_similarity
        })
    
    # 최종 유사도 기준 정렬
    final_results.sort(key=lambda x: x["overall_similarity"], reverse=True)
    
    # 결과 저장 (전체 10개)
    for patent in final_results:
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
    
    return final_results

def check_context_fitness(text: str, field_type: str) -> float:
    """BERT 벡터를 활용한 문맥 적합도 검사"""
    try:
        # 텍스트의 BERT 벡터 추출
        vector = get_bert_vector(text)
        
        # 특허 필드별 예상 패턴 벡터
        field_examples = {
            "technical_field": "본 발명은 기술 분야에 관한 것으로, 특히 기술의 응용과 관련된다",
            "background": "종래 기술에서는 다음과 같은 문제점이 있었다",
            "problem": "본 발명이 해결하고자 하는 과제는",
            "solution": "상기 과제를 해결하기 위한 본 발명의 구성은",
            "effect": "본 발명에 따르면 다음과 같은 효과가 있다",
            "claim": "청구항 1. 다음을 포함하는 장치:"
        }
        
        example_text = field_examples.get(field_type, "특허 문서 텍스트")
        example_vector = get_bert_vector(example_text)
        
        # 코사인 유사도 계산
        similarity = float(cosine_similarity([vector], [example_vector])[0][0])
        return similarity
    except Exception as e:
        print(f"문맥 적합도 검사 중 오류: {str(e)}")
        return 0.0  # 오류 시 0점 반환
