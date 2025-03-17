# app/api/v1/endpoints/patent.py
from fastapi import APIRouter, Depends, HTTPException, UploadFile, File
from typing import List
from sqlalchemy.orm import Session
import pickle

from app.db.session import get_db
from app.models.patent import Patent
from app.schemas.patent import PatentCreate, PatentResponse, PatentAnalyze, SimilarPatent
from app.services.similarity_service import SimilarityService

router = APIRouter()
similarity_service = SimilarityService()

# app/api/v1/endpoints/patent.py
@router.post("/patents/", response_model=PatentResponse)
def create_patent(patent: PatentCreate, db: Session = Depends(get_db)):
    """특허 명세서 등록"""
    try:
        # 데이터베이스에 저장 (벡터 생성 없이)
        db_patent = Patent(
            title=patent.title,
            content=patent.content
            # 모델이 학습되지 않았으므로 벡터 저장 생략
        )
        
        db.add(db_patent)
        db.commit()
        db.refresh(db_patent)
        
        return db_patent
    except Exception as e:
        db.rollback()
        print(f"특허 저장 오류: {str(e)}")
        raise HTTPException(status_code=500, detail=f"특허 저장 중 오류 발생: {str(e)}")

@router.post("/patents/analyze/", response_model=List[SimilarPatent])
def analyze_patent(patent: PatentAnalyze, db: Session = Depends(get_db)):
    """특허 유사도 분석"""
    # 유사도 임계값 설정 (0.2 이상만 표시)
    SIMILARITY_THRESHOLD = 0.2
    
    # 사용자 특허 벡터 생성
    user_vectors = similarity_service.process_patent(patent.content)
    
    # 데이터베이스의 모든 특허 가져오기
    patents = db.query(Patent).all()
    
    # 유사도 계산 및 정렬
    similarities = []
    for p in patents:
        try:
            # NULL 체크 추가
            tfidf_vector = None
            kobert_vector = None
            
            if p.tfidf_vector is not None:
                tfidf_vector = pickle.loads(p.tfidf_vector)
            
            if p.kobert_vector is not None:
                kobert_vector = pickle.loads(p.kobert_vector)
            
            # 유사도 계산 (사용 가능한 벡터만 활용)
            similarity = 0.0
            count = 0
            
            # TF-IDF 벡터 유사도
            if tfidf_vector is not None and user_vectors["tfidf_vector"] is not None:
                tfidf_sim = similarity_service.calculate_similarity(
                    user_vectors["tfidf_vector"], tfidf_vector
                )
                similarity += tfidf_sim
                count += 1
            
            # KoBERT 벡터 유사도
            if kobert_vector is not None and user_vectors["kobert_vector"] is not None:
                kobert_sim = similarity_service.calculate_similarity(
                    user_vectors["kobert_vector"], kobert_vector
                )
                similarity += kobert_sim
                count += 1
            
            # 평균 유사도 계산
            if count > 0:
                similarity /= count
                
                # 임계값 이상인 경우만 결과에 추가
                if similarity >= SIMILARITY_THRESHOLD:
                    similarities.append({
                        "id": p.id,
                        "title": p.title,
                        "similarity": similarity
                    })
        except Exception as e:
            print(f"특허 {p.id} 처리 중 오류: {str(e)}")
            continue
    
    # 유사도 내림차순 정렬
    similarities.sort(key=lambda x: x["similarity"], reverse=True)
    
    # 상위 10개만 반환
    return similarities[:10]

