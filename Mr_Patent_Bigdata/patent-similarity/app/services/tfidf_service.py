# app/services/tfidf_service.py
import re
import pickle
import numpy as np
from konlpy.tag import Okt  # MeCab 대신 Okt 사용
from sklearn.feature_extraction.text import TfidfVectorizer

class TfidfProcessor:
    def __init__(self):
        self.vectorizer = None
        self.okt = Okt()  # MeCab 대신 Okt 사용
    
    def preprocess(self, text):
        """텍스트 전처리 및 토큰화"""
        # 텍스트 정리
        text = self._clean_text(text)
        
        # 토큰화
        tokens = self._tokenize(text)
        
        # TF-IDF 처리를 위한 문자열로 결합
        processed_text = ' '.join(tokens)
        
        return processed_text, tokens
    
    def _clean_text(self, text):
        """텍스트 정리 (특수문자 제거 등)"""
        # 특수 문자 및 숫자 제거
        text = re.sub(r'[^\w\s]', ' ', text)
        text = re.sub(r'\d+', ' ', text)
        # 추가 공백 제거
        text = re.sub(r'\s+', ' ', text).strip()
        return text
    
    def _tokenize(self, text):
        """Okt를 사용한 한국어 텍스트 토큰화"""
        # 명사 추출 (Okt는 기본적으로 명사만 추출)
        nouns = self.okt.nouns(text)
        
        # 의미있는 명사만 필터링 (2글자 이상)
        tokens = [noun for noun in nouns if len(noun) > 1]
        
        # 동사, 형용사도 필요한 경우 다음과 같이 추가할 수 있음
        # pos_tagged = self.okt.pos(text)
        # tokens.extend([word for word, pos in pos_tagged if pos.startswith('V') or pos.startswith('A')])
        
        return tokens
    
    def fit_transform(self, documents):
        """TF-IDF 벡터라이저 학습 및 변환"""
        self.vectorizer = TfidfVectorizer(min_df=2, max_df=0.95)
        matrix = self.vectorizer.fit_transform(documents)
        return matrix
    
    def transform(self, document):
        """단일 문서를 TF-IDF 벡터로 변환"""
        if self.vectorizer is None:
            raise ValueError("TF-IDF 벡터라이저가 아직 학습되지 않았습니다.")
        return self.vectorizer.transform([document])
    
    def get_vocabulary(self):
        """단어 사전 가져오기"""
        if self.vectorizer is None:
            raise ValueError("TF-IDF 벡터라이저가 아직 학습되지 않았습니다.")
        return self.vectorizer.vocabulary_
    
    def save(self, filename):
        """모델 저장"""
        with open(filename, 'wb') as f:
            pickle.dump(self.vectorizer, f)
    
    def load(self, filename):
        """모델 로드"""
        with open(filename, 'rb') as f:
            self.vectorizer = pickle.load(f)
    
    # app/services/tfidf_service.py에 추가
    def get_feature_names(self):
        """특성 이름(단어) 목록 반환"""
        if self.vectorizer is None:
            raise ValueError("TF-IDF 벡터라이저가 아직 학습되지 않았습니다.")
        
        # 최신 scikit-learn 버전 지원
        if hasattr(self.vectorizer, 'get_feature_names_out'):
            return self.vectorizer.get_feature_names_out()
        # 이전 scikit-learn 버전 지원
        else:
            return self.vectorizer.get_feature_names()

