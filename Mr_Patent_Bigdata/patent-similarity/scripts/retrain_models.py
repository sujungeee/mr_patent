import os
import sys
import pickle
import numpy as np
import sqlite3
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

# 프로젝트 루트를 Python 경로에 추가
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from app.services.tfidf_service import TfidfProcessor
from app.services.word2vec_service import Word2VecEnhancer
from app.core.config import settings
from app.db.session import SessionLocal
from app.models.patent import Patent

def retrain_models_and_update_vectors():
    """모델 재학습 및 특허 데이터 벡터 업데이트"""
    # 모델 저장 디렉토리 생성
    os.makedirs(settings.MODEL_PATH, exist_ok=True)
    
    # 데이터베이스 연결
    db = SessionLocal()
    
    try:
        # 모든 특허 데이터 가져오기
        patents = db.query(Patent).all()
        
        if not patents:
            print("⚠️ 특허 데이터가 없습니다. 먼저 특허 데이터를 업로드하세요.")
            return
        
        print(f"🔄 {len(patents)}개의 특허 데이터로 모델 학습 중...")
        
        # 텍스트 데이터 준비
        contents = [patent.content for patent in patents]
        
        # TF-IDF 처리
        tfidf_processor = TfidfProcessor()
        processed_texts = []
        tokenized_docs = []
        
        for content in contents:
            processed_text, tokens = tfidf_processor.preprocess(content)
            processed_texts.append(processed_text)
            tokenized_docs.append(tokens)
        
        # TF-IDF 모델 학습 및 저장
        print("📊 TF-IDF 모델 학습 중...")
        tfidf_matrix = tfidf_processor.fit_transform(processed_texts)
        
        # TF-IDF 모델 저장
        tfidf_model_path = os.path.join(settings.MODEL_PATH, "tfidf_model.pkl")
        with open(tfidf_model_path, 'wb') as f:
            pickle.dump(tfidf_processor.vectorizer, f)
        print(f"✅ TF-IDF 모델 저장 완료: {tfidf_model_path}")
        
        # Word2Vec 모델 학습 및 저장 (설정에서 활성화된 경우)
        word2vec_enhancer = None
        if settings.USE_WORD2VEC:
            print("🔤 Word2Vec 모델 학습 중...")
            word2vec_enhancer = Word2VecEnhancer()
            word2vec_model = word2vec_enhancer.train(tokenized_docs)
            
            # Word2Vec 모델 저장
            word2vec_model_path = os.path.join(settings.MODEL_PATH, "word2vec_model.pkl")
            word2vec_model.save(word2vec_model_path)
            print(f"✅ Word2Vec 모델 저장 완료: {word2vec_model_path}")
        
        # 기존 특허 데이터 벡터 업데이트
        print("🔄 기존 특허 데이터 벡터 업데이트 중...")
        for i, patent in enumerate(patents):
            try:
                # 벡터 생성
                tfidf_vector = tfidf_processor.transform(processed_texts[i])
                
                # 벡터 업데이트
                patent.tfidf_vector = pickle.dumps(tfidf_vector)
                
                # Word2Vec 벡터 추가 (사용하는 경우)
                if word2vec_enhancer and settings.USE_WORD2VEC:
                    word2vec_vector = word2vec_enhancer.get_document_vector(tokenized_docs[i])
                    patent.word2vec_vector = pickle.dumps(word2vec_vector)
                
                # 100개마다 상태 출력
                if (i + 1) % 100 == 0 or i == len(patents) - 1:
                    print(f"⏳ 특허 벡터 업데이트 진행 중: {i+1}/{len(patents)}")
                    db.commit()  # 중간 커밋
            
            except Exception as e:
                print(f"❌ 특허 ID {patent.id} 벡터 업데이트 실패: {str(e)}")
        
        # 최종 커밋
        db.commit()
        print("🎉 모든 모델 학습 및 벡터 업데이트 완료!")
        
    except Exception as e:
        print(f"❌ 모델 학습 중 오류 발생: {str(e)}")
        db.rollback()
    finally:
        db.close()

if __name__ == "__main__":
    retrain_models_and_update_vectors()
