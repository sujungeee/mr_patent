# scripts/upload_patents.py
import os
import requests
import re
import time
import sys

def upload_patents_to_api(patents_dir, api_url, start_index=0, batch_size=500):
    """특허 파일들을 API를 통해 업로드"""
    success_count = 0
    failure_count = 0
    
    # 모든 특허 파일 리스트 가져오기
    patent_files = sorted([f for f in os.listdir(patents_dir) if f.endswith('.txt')])
    
    # 시작 인덱스와 배치 크기에 따라 처리할 파일 선택
    if start_index >= len(patent_files):
        print(f"오류: 시작 인덱스({start_index})가 파일 개수({len(patent_files)})를 초과합니다.")
        return
    
    end_index = min(start_index + batch_size, len(patent_files))
    files_to_process = patent_files[start_index:end_index]
    
    print(f"총 {len(patent_files)}개 중 {start_index+1}~{end_index}번째 파일 처리 중...")
    
    # 선택된 파일 처리
    for i, filename in enumerate(files_to_process, start=start_index+1):
        file_path = os.path.join(patents_dir, filename)
        
        # 파일 내용 읽기
        with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
            content = f.read()
        
        # 발명 명칭 추출 (없으면 파일명 사용)
        title_match = re.search(r'발명의명칭\s*:\s*([^\n]+)', content)
        title = title_match.group(1).strip() if title_match else os.path.splitext(filename)[0]
        
        # API 요청 데이터
        patent_data = {
            "title": title,
            "content": content
        }
        
        # API에 전송
        try:
            response = requests.post(api_url, json=patent_data)
            
            if response.status_code == 200 or response.status_code == 201:
                print(f"✅ [{i}/{end_index}] 특허 '{title[:30]}...' 업로드 성공")
                success_count += 1
            else:
                print(f"❌ [{i}/{end_index}] 특허 '{title[:30]}...' 업로드 실패: {response.status_code}")
                failure_count += 1
            
            # 서버 부하 방지를 위한 짧은 대기
            time.sleep(0.1)
            
        except Exception as e:
            print(f"❌ [{i}/{end_index}] 특허 '{title[:30]}...' 업로드 중 오류: {str(e)}")
            failure_count += 1
    
    print(f"\n작업 완료: {success_count}개 성공, {failure_count}개 실패")

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("사용법: python upload_patents.py <특허파일디렉토리> <시작인덱스> <배치크기(선택)>")
        print("예시: python upload_patents.py ./data/patents 0 100")
        sys.exit(1)
    
    # 명령행 인수 파싱
    patents_dir = sys.argv[1]
    start_index = int(sys.argv[2])
    batch_size = int(sys.argv[3]) if len(sys.argv) > 3 else 500
    
    # API 엔드포인트
    api_url = "http://127.0.0.1:8000/api/v1/patents/"
    
    upload_patents_to_api(patents_dir, api_url, start_index, batch_size)
