from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.core.config import settings
from app.core.database import database
from app.api.routes import patent, task, folders, drafts
from app.core.logging import logger

app = FastAPI(title=settings.PROJECT_NAME)

# CORS 설정 (안드로이드 앱에서 접근 허용)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 프로덕션에서는 실제 도메인으로 제한해야 함
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 라우터 등록
app.include_router(patent.router)
app.include_router(task.router)
app.include_router(folders.router)
app.include_router(drafts.router)

# 이벤트 핸들러
@app.on_event("startup")
async def startup():
    await database.connect()
    logger.info("데이터베이스 연결 성공")

@app.on_event("shutdown")
async def shutdown():
    await database.disconnect()
    logger.info("데이터베이스 연결 종료")

@app.get("/")
async def root():
    return {"message": "특허 관리 API가 실행 중입니다."}
