# app/services/word2vec_service.py
import numpy as np
import pickle
from gensim.models import Word2Vec
from app.core.config import settings

class Word2VecEnhancer:
    def __init__(self):
        self.model = None
    
    def train(self, tokenized_docs):
        """Word2Vec 모델 학습"""
        self.model = Word2Vec(
            sentences=tokenized_docs,
            vector_size=settings.WORD2VEC_SIZE,
            window=settings.WINDOW,
            min_count=settings.MIN_COUNT,
            workers=4
        )
        return self.model
    
    def get_document_vector(self, tokens):
        """문서의 Word2Vec 임베딩 벡터 계산"""
        if self.model is None:
            raise ValueError("Word2Vec 모델이 학습되지 않았습니다.")
        
        # 모델에 있는 단어만 필터링
        valid_tokens = [token for token in tokens if token in self.model.wv]
        
        if not valid_tokens:
            # 유효한 토큰이 없으면 0 벡터 반환
            return np.zeros((1, self.model.vector_size))
        
        # 단어 벡터의 평균 계산
        vectors = [self.model.wv[token] for token in valid_tokens]
        document_vector = np.mean(vectors, axis=0)
        
        # 2D 텐서 형태로 반환 (배치 차원 추가)
        return document_vector.reshape(1, -1)
    
    def enhance_tfidf(self, tfidf_matrix, vocabulary):
        """TF-IDF 벡터를 Word2Vec으로 향상"""
        if self.model is None:
            raise ValueError("Word2Vec 모델이 학습되지 않았습니다.")
        
        # 구현 내용
        # ...
        
        return tfidf_matrix
    
    def save(self, filename):
        """모델 저장"""
        if self.model:
            self.model.save(filename)
    
    def load(self, filename):
        """모델 로드"""
        try:
            self.model = Word2Vec.load(filename)
            return True
        except Exception as e:
            print(f"Word2Vec 모델 로드 오류: {e}")
            return False
