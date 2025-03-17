# app/schemas/patent.py
from pydantic import BaseModel
from typing import List, Optional

class PatentBase(BaseModel):
    title: str
    content: str

# app/schemas/patent.py 수정
class PatentCreate(PatentBase):
    application_number: Optional[str] = None
    metadata: Optional[dict[str, str]] = None

class PatentResponse(PatentBase):
    id: int
    
    class Config:
        # 'orm_mode'를 'from_attributes'로 변경
        from_attributes = True

class SimilarityResult(BaseModel):
    patent_id: int
    title: str
    similarity_score: float
    tfidf_score: float
    kobert_score: float
    word2vec_score: Optional[float] = None

class SimilarityResponse(BaseModel):
    results: List[SimilarityResult]

class PatentAnalyze(BaseModel):
    """특허 분석 요청 스키마"""
    content: str
    
class SimilarPatent(BaseModel):
    """유사 특허 응답 스키마"""
    id: int
    title: str
    similarity: float
