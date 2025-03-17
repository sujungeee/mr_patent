# app/services/kobert_service.py
import numpy as np
import torch
import re
from transformers import AutoTokenizer, AutoModel  # BertTokenizer 대신 AutoTokenizer 사용
from app.core.config import settings

class KoBertProcessor:
    def __init__(self):
        self.tokenizer = None
        self.model = None
        self.initialize()
    
    def initialize(self):
        """KoBERT 모델 및 토크나이저 초기화"""
        try:
            # 로컬 변수 대신 인스턴스 변수로 할당 (self. 추가)
            self.tokenizer = AutoTokenizer.from_pretrained('skt/kobert-base-v1')
            self.model = AutoModel.from_pretrained('skt/kobert-base-v1')
            print("KoBERT 모델 로드 완료")
            
            # 모델이 성공적으로 로드된 경우에만 eval() 호출
            if self.model is not None:
                self.model.eval()  # 평가 모드로 설정
        except Exception as e:
            print(f"KoBERT 모델 로드 오류: {e}")
            self.model = None
            self.tokenizer = None
    
    def get_embedding(self, text):
        """문서의 KoBERT 임베딩 가져오기"""
        try:
            # 길이 제한 (최대 100,000자)
            truncated_text = text[:100000]
            
            # 토큰화 시 최대 길이 설정
            inputs = self.tokenizer(
                truncated_text,
                return_tensors='pt',
                truncation=True,
                max_length=512  # BERT 최대 길이
            )
            
            # token_type_ids 문제 해결 (안전한 방식으로 재생성)
            if 'token_type_ids' in inputs:
                # 모든 값이 0인 새로운 텐서로 완전히 대체
                token_type_shape = inputs['token_type_ids'].shape
                inputs['token_type_ids'] = torch.zeros(token_type_shape, dtype=torch.long)
            
            # 모델 추론 시 별도의 try-except 블록으로 분리
            try:
                with torch.no_grad():
                    outputs = self.model(**inputs)
                    # [CLS] 토큰 벡터 추출
                    embeddings = outputs[0][:, 0, :].numpy()
                    return embeddings
            except Exception as e:
                print(f"KoBERT 모델 추론 중 오류: {str(e)}")
                return np.zeros((1, 768))  # 기본 임베딩 반환
                
        except Exception as e:
            print(f"KoBERT 임베딩 생성 오류: {str(e)}")
            return np.zeros((1, 768))

    
    def get_embedding_for_long_text(self, text):
        """청킹 및 평균화를 통한 긴 문서 처리"""
        # 문장으로 텍스트 분할
        sentences = re.split(r'(?<=[.!?])\s+', text)
        
        # 각 문장 처리
        embeddings = []
        for sentence in sentences:
            if len(sentence.strip()) > 10:  # 실질적인 문장만 처리
                inputs = self.tokenizer(
                    sentence, 
                    return_tensors="pt", 
                    max_length=settings.MAX_SEQUENCE_LENGTH, 
                    truncation=True, 
                    padding='max_length'
                )
                
                with torch.no_grad():
                    outputs = self.model(**inputs)
                
                embedding = outputs.last_hidden_state[:, 0, :].numpy()
                embeddings.append(embedding)
        
        # 임베딩 평균화
        if embeddings:
            return np.mean(embeddings, axis=0)
        else:
            # 매우 짧은 문서의 대체 방안
            return self.get_embedding(text[:settings.MAX_SEQUENCE_LENGTH])
