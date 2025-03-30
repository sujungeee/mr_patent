# app/services/report_generator.py

import os
from datetime import datetime
from typing import Dict, Any
import asyncio
from fpdf import FPDF

# 리포트 저장 디렉토리
REPORTS_DIR = "reports"
os.makedirs(REPORTS_DIR, exist_ok=True)

async def generate_report_pdf(report_data: Dict[str, Any]) -> str:
    """특허 분석 결과를 PDF로 생성"""
    folder = report_data.get("folder", {})
    draft = report_data.get("draft", {})
    similar_patents = report_data.get("similar_patents", [])
    fitness = report_data.get("fitness", {})
    comparisons = report_data.get("comparisons", [])
    
    # PDF 생성
    pdf = FPDF()
    pdf.add_page()
    
    # 제목 설정
    pdf.set_font("Arial", "B", 16)
    pdf.cell(0, 10, "특허 유사도 분석 보고서", 0, 1, "C")
    pdf.ln(10)
    
    # 기본 정보
    pdf.set_font("Arial", "B", 12)
    pdf.cell(0, 10, "1. 특허 초안 정보", 0, 1)
    
    pdf.set_font("Arial", "", 11)
    pdf.cell(0, 8, f"제목: {draft.get('patent_draft_title', '')}", 0, 1)
    pdf.cell(0, 8, f"폴더: {folder.get('user_patent_folder_title', '')}", 0, 1)
    pdf.cell(0, 8, f"작성일: {draft.get('patent_draft_created_at', '')}", 0, 1)
    pdf.ln(5)
    
    # 적합도 결과
    pdf.set_font("Arial", "B", 12)
    pdf.cell(0, 10, "2. 적합도 검사 결과", 0, 1)
    
    pdf.set_font("Arial", "", 11)
    fitness_result = "합격" if fitness.get("fitness_is_corrected") else "불합격"
    pdf.cell(0, 8, f"적합도 결과: {fitness_result}", 0, 1)
    
    fitness_details = fitness.get("fitness_good_content", {})
    for field, is_valid in fitness_details.items():
        result = "적합" if is_valid else "부적합"
        pdf.cell(0, 8, f"{field}: {result}", 0, 1)
    pdf.ln(5)
    
    # 유사 특허 목록
    pdf.set_font("Arial", "B", 12)
    pdf.cell(0, 10, "3. 유사 특허 목록", 0, 1)
    
    pdf.set_font("Arial", "", 11)
    for i, patent in enumerate(similar_patents[:3], 1):
        pdf.cell(0, 8, f"{i}. {patent.get('patent_title', '')}", 0, 1)
        pdf.cell(0, 8, f"   출원번호: {patent.get('patent_application_number', '')}", 0, 1)
        pdf.cell(0, 8, f"   유사도: {float(patent.get('similarity_patent_score', 0)) * 100:.2f}%", 0, 1)
        pdf.ln(3)
    pdf.ln(5)
    
    # 상세 비교 결과
    pdf.set_font("Arial", "B", 12)
    pdf.cell(0, 10, "4. 상세 비교 결과", 0, 1)
    
    pdf.set_font("Arial", "", 11)
    for i, comp in enumerate(comparisons[:3], 1):
        pdf.cell(0, 8, f"비교 {i}:", 0, 1)
        pdf.cell(0, 8, f"총점: {float(comp.get('detailed_comparison_total_score', 0)) * 100:.2f}%", 0, 1)
        
        # 컨텍스트 데이터가 있는 경우 표시
        context_data = comp.get("detailed_comparison_context", {})
        if isinstance(context_data, dict) and "highlights" in context_data:
            for j, highlight in enumerate(context_data["highlights"][:3], 1):
                pdf.cell(0, 8, f"유사 부분 {j}:", 0, 1)
                pdf.multi_cell(0, 8, f"초안: {highlight.get('original', '')}")
                pdf.multi_cell(0, 8, f"특허: {highlight.get('similar', '')}")
                pdf.cell(0, 8, f"유사도: {float(highlight.get('similarity', 0)) * 100:.2f}%", 0, 1)
                pdf.ln(2)
        pdf.ln(3)
    
    # 파일 생성 날짜로 파일명 생성
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    folder_id = folder.get("user_patent_folder_id", "")
    filename = f"patent_report_{folder_id}_{timestamp}.pdf"
    file_path = os.path.join(REPORTS_DIR, filename)
    
    # PDF 저장
    pdf.output(file_path)
    
    return file_path
