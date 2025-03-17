# app/core/config.py
from pydantic_settings import BaseSettings, SettingsConfigDict
from pydantic import MySQLDsn  # 또는 필요한 다른 타입

class Settings(BaseSettings):
    APP_NAME: str = "특허 명세서 유사도 분석 API"
    MAX_SEQUENCE_LENGTH: int = 512
    WORD2VEC_SIZE: int = 200
    MIN_COUNT: int = 2
    WINDOW: int = 5
    USE_WORD2VEC: bool = True
    TFIDF_WEIGHT: float = 0.3
    WORD2VEC_WEIGHT: float = 0.3
    KOBERT_WEIGHT: float = 0.4
    MODEL_PATH: str = "models/saved"
    DATABASE_URL: str = "sqlite:///./patent_database.db"
    
    # 이전 Config 클래스 대신 model_config 사용
    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8"
    )

settings = Settings()
