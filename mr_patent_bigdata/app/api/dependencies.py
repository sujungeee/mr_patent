from fastapi import Request, HTTPException, Depends
from fastapi.security import APIKeyHeader
import re

# API 키 헤더 정의
api_key_header = APIKeyHeader(name="Authorization", auto_error=False)

# 문서 경로 패턴
DOCS_ROUTES = ["/docs", "/redoc", "/openapi.json"]

async def verify_token(request: Request, api_key: str = Depends(api_key_header)):
    """토큰 검증 로직에서 문서 접근 경로는 예외 처리"""
    
    # 문서 접근 경로면 인증 없이 접근 허용
    if request.url.path in DOCS_ROUTES or request.url.path.endswith("/docs") or request.url.path.endswith("/redoc"):
        return True
        
    # 메인 루트 경로도 인증 없이 접근 허용
    if request.url.path == "/":
        return True
    
    # 기존 인증 로직
    if not api_key:
        raise HTTPException(
            status_code=401, 
            detail={"code": "TOKEN_NOT_FOUND", "message": "토큰이 존재하지 않습니다.", "status": 401}
        )
    
    # 토큰 검증 로직 - 기존 로직 유지
    # 여기에 토큰 검증 코드 추가...
    
    return True
