# app/scripts/train_vectorizer.py

import sys
import os
import asyncio
from databases import Database
import numpy as np

# 프로젝트 루트 디렉토리를 PYTHONPATH에 추가
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), '../..')))

from app.services.vectorizer import train_and_save_vectorizer
from app.core.config import settings

# 데이터베이스 연결
DATABASE_URL = settings.DATABASE_URL
database = Database(DATABASE_URL)

async def train_vectorizer_from_patents():
    """기존 특허 데이터를 사용하여 TF-IDF 벡터라이저 학습"""
    await database.connect()
    
    # 특허 데이터 조회
    query = """
    SELECT patent_title, patent_summary, patent_claim
    FROM patent
    """
    
    patents = await database.fetch_all(query=query)
    
    if not patents:
        print("학습할 특허 데이터가 없습니다.")
        return
    
    # 특허 텍스트 추출
    patent_texts = []
    for patent in patents:
        title = patent["patent_title"] or ""
        summary = patent["patent_summary"] or ""
        claim = patent["patent_claim"] or ""
        
        # 모든 텍스트 결합 (각 필드별 벡터라이저를 만들 수도 있음)
        combined_text = f"{title} {summary} {claim}"
        if combined_text.strip():
            patent_texts.append(combined_text)
    
    # 벡터라이저 학습 및 저장
    print(f"총 {len(patent_texts)}개 특허로 벡터라이저 학습 시작...")
    vectorizer = train_and_save_vectorizer(patent_texts)
    print("벡터라이저 학습 및 저장 완료!")
    
    await database.disconnect()

if __name__ == "__main__":
    asyncio.run(train_vectorizer_from_patents())
