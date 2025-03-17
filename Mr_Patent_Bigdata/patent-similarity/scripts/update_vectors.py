# scripts/update_vectors.py
import os
import sys
import pickle
import numpy as np

# 프로젝트 루트를 Python 경로에 추가
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from app.db.session import SessionLocal
from app.models.patent import Patent
from app.services.similarity_service import SimilarityService

def update_patent_vectors():
    """기존 특허의 벡터 업데이트"""
    db = SessionLocal()
    
    try:
        # 유사도 서비스 초기화
        similarity_service = SimilarityService()
        
        # 모든 특허 가져오기
        patents = db.query(Patent).all()
        
        print(f"{len(patents)}개의 특허 벡터 업데이트 시작...")
        
        for i, patent in enumerate(patents):
            try:
                # 특허 내용 처리
                vectors = similarity_service.process_patent(patent.content)
                
                # 벡터 저장
                patent.tfidf_vector = pickle.dumps(vectors["tfidf_vector"])
                
                if vectors["kobert_vector"] is not None:
                    patent.kobert_vector = pickle.dumps(vectors["kobert_vector"])
                
                # 100개마다 커밋
                if (i + 1) % 100 == 0 or i == len(patents) - 1:
                    db.commit()
                    print(f"진행 상황: {i+1}/{len(patents)} 완료")
                
            except Exception as e:
                print(f"특허 ID {patent.id} 처리 중 오류: {str(e)}")
                continue
        
        print("모든 특허 벡터 업데이트 완료!")
        
    except Exception as e:
        print(f"전체 처리 중 오류 발생: {str(e)}")
        db.rollback()
    finally:
        db.close()

if __name__ == "__main__":
    update_patent_vectors()
