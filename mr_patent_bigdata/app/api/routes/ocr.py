from fastapi import APIRouter, UploadFile, File, BackgroundTasks, HTTPException
from typing import Dict, Any
import os
import io
from google.cloud import vision
from pdf2image import convert_from_path
from app.core.logging import logger
from dotenv import load_dotenv

router = APIRouter(prefix="/api", tags=["ocr"])

# 임시 파일 저장 경로
TEMP_DIR = "/temp_pdf"
os.makedirs(TEMP_DIR, exist_ok=True)

# .env 파일 로드
load_dotenv()

# 환경 변수에서 경로 가져오기
credentials_path = os.getenv("GOOGLE_CREDENTIALS_PATH")
os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = credentials_path

@router.post("/pdf/extract-text", response_model=Dict[str, Any])
async def extract_text_from_pdf(
    file: UploadFile = File(...),
    background_tasks: BackgroundTasks = None
):
    """특허 PDF에서 텍스트 추출"""
    # 임시 파일로 저장
    temp_file_path = f"{TEMP_DIR}/{file.filename}"
    
    with open(temp_file_path, "wb") as f:
        content = await file.read()
        f.write(content)
    
    try:
        # Google Cloud Vision API로 텍스트 추출
        text = extract_text(temp_file_path)
        
        # 임시 파일 삭제
        if os.path.exists(temp_file_path):
            os.remove(temp_file_path)
        
        return {
            "status": "success",
            "text": text
        }
    except Exception as e:
        logger.error(f"PDF 텍스트 추출 중 오류: {str(e)}")
        raise HTTPException(status_code=500, detail=f"PDF 처리 중 오류: {str(e)}")

def extract_text(pdf_path: str) -> str:
    """PDF에서 텍스트 추출 (Google Cloud Vision API 사용)"""
    try:
        # Vision API 클라이언트 초기화
        client = vision.ImageAnnotatorClient()
        
        # Poppler 경로 지정
        poppler_path = r"C:\SSAFY\poppler-24.08.0\Library\bin"
        
        # PDF를 이미지로 변환 (여전히 poppler 필요)
        pages = convert_from_path(pdf_path, 300, poppler_path=poppler_path)
        text_result = ""
        
        # 각 페이지를 Vision API로 처리
        for i, page in enumerate(pages):
            # 이미지를 메모리에 저장
            img_byte_arr = io.BytesIO()
            page.save(img_byte_arr, format='PNG')
            content = img_byte_arr.getvalue()
            
            # Vision API 요청
            image = vision.Image(content=content)
            
            # 문서 텍스트 감지 요청 (한국어+영어 힌트 포함)
            response = client.document_text_detection(
                image=image,
                image_context={"language_hints": ["ko", "en"]}
            )
            
            # 페이지별 결과 저장
            text = response.full_text_annotation.text
            text_result += f"\n--- 페이지 {i+1} ---\n{text}"
            
        return text_result
    except Exception as e:
        logger.error(f"Vision API 처리 중 오류: {str(e)}")
        raise Exception(f"OCR 처리 중 오류: {str(e)}")
