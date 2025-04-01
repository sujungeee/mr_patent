from fastapi import APIRouter, UploadFile, File, BackgroundTasks, HTTPException
from typing import Dict, Any
import os
import pytesseract
from pdf2image import convert_from_path
from app.core.logging import logger

router = APIRouter(prefix="/api", tags=["ocr"])

# 임시 파일 저장 경로
TEMP_DIR = "/temp_pdf"
os.makedirs(TEMP_DIR, exist_ok=True)

# Tesseract 실행 파일 경로 명시적 지정
pytesseract.pytesseract.tesseract_cmd = r'C:\Program Files\Tesseract-OCR\tesseract.exe'

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
        # PDF에서 텍스트 추출
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
    """PDF에서 텍스트 추출 (OCR)"""
    try:
        # Poppler 경로 지정
        poppler_path = r"C:\SSAFY\poppler-24.08.0\Library\bin"
        
        # PDF를 이미지로 변환 (Poppler 경로 지정)
        pages = convert_from_path(pdf_path, 300, poppler_path=poppler_path)
        text_result = ""
        
        for i, page in enumerate(pages):
            # 이미지에서 텍스트 추출 (한국어+영어)
            text = pytesseract.image_to_string(page, lang='kor+eng')
            text_result += f"\n--- 페이지 {i+1} ---\n{text}"
            
        return text_result
    except Exception as e:
        logger.error(f"OCR 처리 중 오류: {str(e)}")
        raise Exception(f"OCR 처리 중 오류: {str(e)}")
