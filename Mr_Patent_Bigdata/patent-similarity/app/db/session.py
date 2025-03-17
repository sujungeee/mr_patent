from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker

# SQLite 데이터베이스 설정 (간단한 테스트용)
SQLALCHEMY_DATABASE_URL = "sqlite:///./patent_database.db"

# SQLite는 기본적으로 단일 스레드만 지원하므로, 멀티스레드 접근을 허용하는 설정 추가
engine = create_engine(
    SQLALCHEMY_DATABASE_URL, connect_args={"check_same_thread": False}
)

# 세션 팩토리 생성
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# 모델 클래스 선언을 위한 베이스 클래스
Base = declarative_base()

# 의존성 주입을 위한 함수 - FastAPI 요청 핸들러에서 사용됨
def get_db():
    db = SessionLocal()
    try:
        yield db  # 요청 처리 동안 세션 제공
    finally:
        db.close()  # 요청 처리 완료 후 세션 닫기
