import re
from typing import List, Dict, Any
from striprtf.striprtf import rtf_to_text
from app.core.logging import logger

def extract_patent_data(rtf_text: str) -> List[Dict[str, Any]]:
    """RTF 텍스트에서 특허 데이터 추출"""
    # 특허 섹션으로 분할
    pattern = r'-------------------------------------------------------------------\n출원번호\(출원일자\) : \t\d+\(\d{4}-\d{2}-\d{2}\)\n번호 : \d+\n-------------------------------------------------------------------'
    sections = re.split(pattern, rtf_text)
    
    # 헤더와 빈 섹션 제거
    sections = [s for s in sections if '출원번호\t:' in s]
    
    patents = []
    
    # 추출 패턴 정의
    patterns = {
        "application_number": r"출원번호\t: (\S+)",
        "title": r"발명의명칭\t: (.+?)(?=\n\n\(요약\))",
        "summary": r"\(요약\)\n(.+?)(?=\n\n\(청구항\))",
        "claims": r"\(청구항\)\n(.+?)(?=\nIPC분류)",
        "ipc_classification": r"IPC분류 \t: (.+)"
    }
    
    for section in sections:
        patent = {}
        for key, pattern in patterns.items():
            match = re.search(pattern, section, re.DOTALL)
            patent[key] = match.group(1).strip() if match else None
        
        if all(patent.get(k) for k in ["application_number", "title", "summary", "claims", "ipc_classification"]):
            patents.append(patent)
    
    return patents

def prepare_corpus(patents: List[Dict[str, Any]]) -> List[str]:
    """특허 데이터로부터 코퍼스 준비"""
    corpus = []
    for patent in patents:
        title = patent.get("title", "")
        summary = patent.get("summary", "")
        claims = patent.get("claims", "")
        combined_text = f"{title} {summary} {claims}"
        corpus.append(combined_text)
    return corpus
