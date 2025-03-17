# scripts/load_patents_to_db.py
import os
import requests
import re
import time

def upload_patents_to_api(patents_dir, api_url):
    """특허 파일들을 API를 통해 업로드"""
    success_count = 0
    failure_count = 0
    
    # 모든 특허 파일 처리
    for filename in os.listdir(patents_dir):
        if filename.endswith('.txt'):
            file_path = os.path.join(patents_dir, filename)
            
            # 파일명에서 특허 번호 추출
            patent_number_match = re.search(r'patent_(\d+)', filename)
            patent_number = patent_number_match.group(1) if patent_number_match else "Unknown"
            
            # 파일 내용 읽기
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
            
            # 발명 명칭 추출
            title_match = re.search(r'발명의명칭\s*:\s*([^\n]+)', content)
            title = title_match.group(1).strip() if title_match else f"특허 {patent_number}"
            
            # 출원인 추출 (선택사항)
            applicant_match = re.search(r'출원인\s*:\s*([^\n]+)', content)
            applicant = applicant_match.group(1).strip() if applicant_match else ""
            
            # API 요청 데이터 - 추가 메타데이터 포함
            patent_data = {
                "title": title,
                "content": content,
                "application_number": patent_number,  # 출원번호 추가
                "metadata": {
                    "applicant": applicant,
                    "filename": filename
                }
            }
            
            # API에 전송
            try:
                response = requests.post(api_url, json=patent_data)
                
                if response.status_code == 200 or response.status_code == 201:
                    print(f"✅ 특허 '{patent_number}' 업로드 성공: {title}")
                    success_count += 1
                else:
                    print(f"❌ 특허 '{patent_number}' 업로드 실패: {response.text}")
                    failure_count += 1
                
                # 서버 부하 방지를 위한 짧은 대기
                time.sleep(0.1)
                
            except Exception as e:
                print(f"❌ 특허 '{patent_number}' 업로드 중 오류: {str(e)}")
                failure_count += 1
    
    print(f"\n작업 완료: {success_count}개 성공, {failure_count}개 실패")

if __name__ == "__main__":
    # 특허 파일 디렉토리
    patents_dir = "path/to/extracted/patents"
    # FastAPI 엔드포인트
    api_url = "http://127.0.0.1:8000/api/v1/patents/"
    
    upload_patents_to_api(patents_dir, api_url)
