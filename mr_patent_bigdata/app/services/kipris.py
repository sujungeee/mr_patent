# app/services/kipris.py
import os
import aiohttp
import json
from typing import Dict, Tuple, Any, Optional

# KIPRIS API 키 및 기본 URL
KIPRIS_API_KEY = os.environ.get("KIPRIS_API_KEY", "your_api_key")
KIPRIS_BASE_URL = "http://plus.kipris.or.kr/kipo-api/kipi/patInfoSearchService"

# PDF 저장 경로
PDF_DIR = "patent_pdfs"
os.makedirs(PDF_DIR, exist_ok=True)

async def get_patent_public_info(application_number: str) -> Optional[Dict[str, Any]]:
    """KIPRIS API를 사용해 특허 공고전문 정보 가져오기"""
    url = f"{KIPRIS_BASE_URL}/bibliographicInfo"
    params = {
        "applicationNumber": application_number,
        "ServiceKey": KIPRIS_API_KEY
    }
    
    try:
        async with aiohttp.ClientSession() as session:
            async with session.get(url, params=params) as response:
                if response.status == 200:
                    data = await response.json()
                    # API 응답 파싱 (실제 KIPRIS API 응답 구조에 맞게 수정 필요)
                    if "response" in data and "body" in data["response"]:
                        return {
                            "publication_number": data["response"]["body"].get("publicationNumber", ""),
                            "publication_date": data["response"]["body"].get("publicationDate", ""),
                            "application_number": application_number,
                            "title": data["response"]["body"].get("inventionTitle", ""),
                            "raw_response": data
                        }
                return None
    except Exception as e:
        print(f"KIPRIS API 호출 중 오류: {str(e)}")
        return None

async def download_patent_pdf(publication_number: str) -> Tuple[str, str]:
    """특허 공고전문 PDF 다운로드"""
    url = f"{KIPRIS_BASE_URL}/pat/biblioOpenAPI"
    params = {
        "publicationNumber": publication_number,
        "ServiceKey": KIPRIS_API_KEY
    }
    
    pdf_name = f"{publication_number}.pdf"
    pdf_path = os.path.join(PDF_DIR, pdf_name)
    
    try:
        async with aiohttp.ClientSession() as session:
            async with session.get(url, params=params) as response:
                if response.status == 200:
                    with open(pdf_path, 'wb') as f:
                        f.write(await response.read())
                    return pdf_path, pdf_name
                else:
                    # 오류 시 빈 파일 생성
                    with open(pdf_path, 'w') as f:
                        f.write("")
                    return pdf_path, pdf_name
    except Exception as e:
        print(f"PDF 다운로드 중 오류: {str(e)}")
        # 오류 시 빈 파일 생성
        with open(pdf_path, 'w') as f:
            f.write("")
        return pdf_path, pdf_name
