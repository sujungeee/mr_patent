# app/services/similarity_service.py
import pickle
import numpy as np
import os
from typing import List, Dict, Any
from sklearn.metrics.pairwise import cosine_similarity

from app.core.config import settings
from app.services.tfidf_service import TfidfProcessor
from app.services.word2vec_service import Word2VecEnhancer
from app.services.kobert_service import KoBertProcessor

class SimilarityService:
    def __init__(self):
        self.tfidf_processor = TfidfProcessor()
        self.word2vec_enhancer = Word2VecEnhancer() if settings.USE_WORD2VEC else None
        self.kobert_processor = KoBertProcessor()
        self.load_models()
        
    def load_models(self):
        """저장된 모델 로드"""
        try:
            # 모델 디렉토리 확인
            os.makedirs(settings.MODEL_PATH, exist_ok=True)
            
            # TF-IDF 모델 로드 시도
            tfidf_path = f"{settings.MODEL_PATH}/tfidf_model.pkl"
            if os.path.exists(tfidf_path):
                self.tfidf_processor.load(tfidf_path)
                print("TF-IDF 모델 로드 완료")
            else:
                print("TF-IDF 모델이 아직 학습되지 않았습니다.")
            
            # Word2Vec 모델 로드 시도
            if settings.USE_WORD2VEC:
                word2vec_path = f"{settings.MODEL_PATH}/word2vec_model.pkl"
                if os.path.exists(word2vec_path):
                    self.word2vec_enhancer.load(word2vec_path)
                    print("Word2Vec 모델 로드 완료")
                else:
                    print("Word2Vec 모델이 아직 학습되지 않았습니다.")
        except Exception as e:
            print(f"모델 로드 중 오류 발생: {e}")
    
    def process_patent(self, content: str) -> Dict[str, Any]:
        """특허 명세서 처리"""
        try:
            # 텍스트 전처리
            processed_text, tokens = self.tfidf_processor.preprocess(content)
            
            # TF-IDF 벡터 생성
            tfidf_vector = self.tfidf_processor.transform(processed_text)
            if tfidf_vector is not None:
                # 벡터가 1차원인 경우 2차원으로 변환
                if len(tfidf_vector.shape) == 1:
                    tfidf_vector = tfidf_vector.reshape(1, -1)
            
            # Word2Vec 벡터 생성 (옵션)
            word2vec_vector = None
            if self.word2vec_enhancer and self.word2vec_enhancer.model:
                try:
                    word2vec_vector = self.word2vec_enhancer.get_document_vector(tokens)
                except Exception as e:
                    print(f"Word2Vec 벡터 생성 오류: {str(e)}")
            
            # KoBERT 벡터 생성 (예외 처리 추가)
            try:
                kobert_vector = self.kobert_processor.get_embedding(content)
            except Exception as e:
                print(f"KoBERT 벡터 생성 실패: {str(e)}")
                kobert_vector = np.zeros((1, 768))  # 기본 벡터
            
            return {
                "tfidf_vector": tfidf_vector,
                "word2vec_vector": word2vec_vector,
                "kobert_vector": kobert_vector,
                "tokens": tokens
            }
        except Exception as e:
            print(f"특허 처리 오류: {str(e)}")
            # 오류 시 기본 벡터 반환
            default_vector_size = 100  # 기본 설정
            try:
                if hasattr(self.tfidf_processor, "vectorizer"):
                    default_vector_size = len(self.tfidf_processor.get_feature_names())
            except:
                pass
                
            return {
                "tfidf_vector": np.zeros((1, default_vector_size)),
                "word2vec_vector": np.zeros((1, 100)),
                "kobert_vector": np.zeros((1, 768)),
                "tokens": []
            }

    
    def calculate_similarity(self, vector1, vector2):
        """벡터 간 코사인 유사도 계산"""
        try:
            # 벡터 차원 확인 및 조정
            if len(vector1.shape) > 2 or len(vector2.shape) > 2:
                # 차원이 2보다 큰 경우 2차원으로 변환
                vector1 = vector1.reshape(-1, vector1.shape[-1])
                vector2 = vector2.reshape(-1, vector2.shape[-1])
            
            # 1차원 벡터인 경우 2차원으로 변환
            if len(vector1.shape) == 1:
                vector1 = vector1.reshape(1, -1)
            if len(vector2.shape) == 1:
                vector2 = vector2.reshape(1, -1)
            
            # 코사인 유사도 계산
            similarity = cosine_similarity(vector1, vector2)[0][0]
            return float(similarity)
        except Exception as e:
            print(f"유사도 계산 오류: {str(e)}")
            return 0.0

