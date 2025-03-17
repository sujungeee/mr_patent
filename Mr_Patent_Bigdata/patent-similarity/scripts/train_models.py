import os
import sys
import pickle
import numpy as np

# 프로젝트 루트를 Python 경로에 추가
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from app.services.tfidf_service import TfidfProcessor
from app.services.word2vec_service import Word2VecEnhancer
from app.core.config import settings
from app.db.session import SessionLocal
from app.models.patent import Patent

def train_and_save_models():
    """데이터베이스에서 특허 데이터를 가져와 모델 학습 및 저장"""
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
        with open(os.path.join(settings.MODEL_PATH, "tfidf_model.pkl"), 'wb') as f:
            pickle.dump(tfidf_processor.vectorizer, f)
        print("✅ TF-IDF 모델 저장 완료")
        
        # Word2Vec 모델 학습 및 저장 (설정에서 활성화된 경우)
        if settings.USE_WORD2VEC:
            print("🔤 Word2Vec 모델 학습 중...")
            word2vec_enhancer = Word2VecEnhancer()
            word2vec_model = word2vec_enhancer.train(tokenized_docs)
            
            # Word2Vec 모델 저장
            word2vec_model.save(os.path.join(settings.MODEL_PATH, "word2vec_model.pkl"))
            print("✅ Word2Vec 모델 저장 완료")
        
        print("🎉 모든 모델 학습 및 저장 완료!")
        
    except Exception as e:
        print(f"❌ 모델 학습 중 오류 발생: {str(e)}")
    finally:
        db.close()

if __name__ == "__main__":
    train_and_save_models()
