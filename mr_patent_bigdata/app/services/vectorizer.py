import os
import pickle
import numpy as np
import torch
from sklearn.feature_extraction.text import TfidfVectorizer
from typing import List, Dict, Any, Tuple
from kobert_tokenizer import KoBERTTokenizer
from transformers import BertModel

from app.core.logging import logger

# 벡터라이저 저장 경로
VECTORIZER_PATH = "models/tfidf_vectorizer.pkl"
os.makedirs(os.path.dirname(VECTORIZER_PATH), exist_ok=True)

# TF-IDF 벡터라이저와 KoBERT 모델 초기화
tfidf_vectorizer = TfidfVectorizer(max_features=1000)

# KoBERT 모델 초기화
tokenizer = KoBERTTokenizer.from_pretrained('skt/kobert-base-v1')
model = BertModel.from_pretrained('skt/kobert-base-v1', return_dict=False)

def train_and_save_vectorizer(corpus, filename=VECTORIZER_PATH):
    """특허 말뭉치로 TF-IDF 벡터라이저 학습 및 저장"""
    global tfidf_vectorizer
    
    # 벡터라이저 학습
    tfidf_vectorizer.fit(corpus)
    
    # 학습된 벡터라이저 저장
    with open(filename, "wb") as f:
        pickle.dump(tfidf_vectorizer, f)
    
    print(f"TF-IDF 벡터라이저 학습 완료 (어휘 크기: {len(tfidf_vectorizer.vocabulary_)})")
    return tfidf_vectorizer

def load_vectorizer(filename=VECTORIZER_PATH):
    """저장된 TF-IDF 벡터라이저 로드"""
    global tfidf_vectorizer
    
    try:
        with open(filename, "rb") as f:
            tfidf_vectorizer = pickle.load(f)
        print(f"TF-IDF 벡터라이저 로드 완료 (어휘 크기: {len(tfidf_vectorizer.vocabulary_)})")
    except FileNotFoundError:
        print("저장된 벡터라이저가 없습니다. 먼저 학습을 진행하세요.")
    
    return tfidf_vectorizer

def fit_tfidf_vectorizer(corpus: List[str]) -> None:
    """TF-IDF 벡터라이저 학습"""
    global tfidf_vectorizer
    tfidf_vectorizer = TfidfVectorizer(max_features=1000)
    tfidf_vectorizer.fit(corpus)
    logger.info("TF-IDF 벡터라이저 학습 완료")
    
    # 학습된 벡터라이저 저장
    save_vectorizer(tfidf_vectorizer)

def save_vectorizer(vectorizer, filename=VECTORIZER_PATH):
    """학습된 TF-IDF 벡터라이저 저장"""
    with open(filename, "wb") as f:
        pickle.dump(vectorizer, f)
    logger.info(f"TF-IDF 벡터라이저 저장 완료 (경로: {filename})")

def load_vectorizer(filename=VECTORIZER_PATH):
    """저장된 TF-IDF 벡터라이저 로드"""
    global tfidf_vectorizer
    
    try:
        with open(filename, "rb") as f:
            tfidf_vectorizer = pickle.load(f)
        logger.info(f"TF-IDF 벡터라이저 로드 완료 (어휘 크기: {len(tfidf_vectorizer.vocabulary_)})")
        return tfidf_vectorizer
    except FileNotFoundError:
        logger.warning("저장된 벡터라이저가 없습니다. 기본 벡터라이저를 사용합니다.")
        return tfidf_vectorizer

def get_tfidf_vector(text: str) -> np.ndarray:
    """텍스트의 TF-IDF 벡터 계산"""
    global tfidf_vectorizer
    
    if not text:
        return np.zeros(1000)
    
    try:
        # 벡터라이저 사용하여 변환
        return tfidf_vectorizer.transform([text]).toarray()[0]
    except Exception as e:
        logger.error(f"TF-IDF 벡터 생성 중 오류: {e}")
        # 저장된 벡터라이저 로드 시도
        try:
            load_vectorizer()
            return tfidf_vectorizer.transform([text]).toarray()[0]
        except:
            # 빈 벡터 반환
            logger.warning("벡터라이저 로드 실패. 영벡터를 반환합니다.")
            return np.zeros(1000)

def get_kobert_vector(text: str, max_length: int = 512) -> np.ndarray:
    """텍스트의 KoBERT 벡터 계산"""
    if not text:
        return np.zeros(768)  # KoBERT 임베딩 차원
    
    try:
        # 텍스트가 너무 길면 잘라내기
        if len(text) > max_length * 4:
            text = text[:max_length * 4]
        
        inputs = tokenizer(text, return_tensors='pt', truncation=True, max_length=max_length, padding='max_length')
        with torch.no_grad():
            # return_dict=True를 추가하여 속성으로 접근 가능한 객체 반환
            outputs = model(**inputs, return_dict=True)
        
        return outputs.last_hidden_state[:, 0, :].numpy().flatten()
    except Exception as e:
        logger.error(f"KoBERT 벡터 생성 중 오류: {e}")
        return np.zeros(768)  # 오류 발생 시 영벡터 반환

def generate_vectors(patent_data: Dict[str, Any]) -> Tuple[np.ndarray, np.ndarray]:
    """특허 데이터의 벡터 생성"""
    title = patent_data.get("title", "")
    summary = patent_data.get("summary", "")
    claims = patent_data.get("claims", "")
    
    combined_text = f"{title} {summary} {claims}"
    
    tfidf_vector = get_tfidf_vector(combined_text)
    kobert_vector = get_kobert_vector(combined_text)
    
    return tfidf_vector, kobert_vector

def generate_field_vectors(field_text: str) -> Tuple[np.ndarray, np.ndarray]:
    """특정 필드의 벡터 생성"""
    tfidf_vector = get_tfidf_vector(field_text)
    kobert_vector = get_kobert_vector(field_text)
    
    return tfidf_vector, kobert_vector
