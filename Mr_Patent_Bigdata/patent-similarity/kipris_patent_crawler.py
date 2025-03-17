# from selenium import webdriver
# from selenium.webdriver.chrome.service import Service
# from selenium.webdriver.common.by import By
# from selenium.webdriver.support.ui import WebDriverWait
# from selenium.webdriver.support import expected_conditions as EC
# from selenium.common.exceptions import TimeoutException, NoSuchElementException, StaleElementReferenceException
# from webdriver_manager.chrome import ChromeDriverManager
# import pyautogui  # 윈도우 다이얼로그 제어용
# import time
# import os
# from tqdm import tqdm
# import pandas as pd
# import traceback
# import re

# # 다운로드 경로 설정
# download_path = r"C:\Users\SSAFY\Downloads\a01_pdf"
# os.makedirs(download_path, exist_ok=True)

# # 크롬 옵션 설정
# options = webdriver.ChromeOptions()
# prefs = {
#     "download.default_directory": download_path,
#     "download.prompt_for_download": False,
#     "download.directory_upgrade": True,
#     "plugins.always_open_pdf_externally": True,
#     "safebrowsing.enabled": True
# }
# options.add_experimental_option("prefs", prefs)
# options.add_argument("--start-maximized")  # 브라우저 최대화로 요소 인식 개선
# options.add_argument("--disable-popup-blocking")  # 팝업 차단 해제

# # 웹드라이버 초기화
# driver = webdriver.Chrome(service=Service(ChromeDriverManager().install()), options=options)
# wait = WebDriverWait(driver, 20)  # 대기 시간 20초로 설정

# # 디버깅용 스크린샷 함수
# def capture_screenshot(prefix):
#     timestamp = time.strftime("%Y%m%d_%H%M%S")
#     filename = f"{download_path}/{prefix}_{timestamp}.png"
#     driver.save_screenshot(filename)
#     print(f"스크린샷 저장: {filename}")

# # 다운로드 진행 내역 저장용 데이터프레임
# download_records = pd.DataFrame(columns=["page", "patent_index", "application_number", "title", "status"])

# def search_patents():
#     try:
#         # KIPRIS 검색 페이지 접속
#         driver.get("https://www.kipris.or.kr/khome/search/searchResult.do")
#         time.sleep(3)  # 페이지 로드 대기 시간 추가
        
#         # 검색창 찾기 및 검색어 입력
#         search_input = wait.until(EC.presence_of_element_located((By.ID, "queryText")))
#         search_input.clear()
#         search_input.send_keys("IPC=[A01]")
        
#         # 검색 버튼 클릭
#         search_button = wait.until(EC.element_to_be_clickable((By.CSS_SELECTOR, ".btn-search")))
#         driver.execute_script("arguments[0].click();", search_button)
        
#         # 검색 결과 로딩 대기
#         wait.until(EC.presence_of_element_located((By.CSS_SELECTOR, ".result-item")))
        
#         # 총 페이지 수 가져오기
#         try:
#             result_total = wait.until(EC.presence_of_element_located((By.ID, "resultTotal")))
#             total_count_text = result_total.text
#             total_count_match = re.search(r'(\d{1,3}(?:,\d{3})*)', total_count_text)
            
#             if total_count_match:
#                 total_count = int(total_count_match.group(1).replace(',', ''))
#                 per_page = 30  # 기본값
#                 total_pages = (total_count + per_page - 1) // per_page
#             else:
#                 total_pages = 100  # 기본값
#         except:
#             print("페이지 정보를 찾을 수 없어 기본값 사용")
#             total_pages = 100  # 기본값
            
#         print(f"총 {total_pages}페이지의 검색 결과를 처리합니다.")
#         return total_pages
        
#     except Exception as e:
#         capture_screenshot("search_error")
#         print(f"검색 중 오류 발생: {e}")
#         traceback.print_exc()
#         return 0

# def process_patent_list(current_page):
#     try:
#         # 현재 페이지의 특허 목록 가져오기
#         patents = wait.until(EC.presence_of_all_elements_located((By.CSS_SELECTOR, ".result-item")))
#         print(f"페이지 {current_page}: {len(patents)}개의 특허 항목을 처리합니다.")
        
#         for idx, patent in enumerate(patents):
#             try:
#                 # 출원번호 추출
#                 try:
#                     app_number_elements = patent.find_elements(By.CSS_SELECTOR, "em.tit")
#                     for el in app_number_elements:
#                         if "출원번호" in el.text:
#                             app_number = el.find_element(By.XPATH, "./following-sibling::div").text.strip()
#                             # 괄호와 공백 제거
#                             app_number = re.sub(r'\([^)]*\)', '', app_number).strip()
#                             break
#                     else:
#                         # 다른 방법으로 시도
#                         app_number = patent.get_attribute("id").replace("divViewSel", "")
#                         if not app_number:
#                             app_number = f"Unknown_{current_page}_{idx}"
#                 except:
#                     app_number = f"Unknown_{current_page}_{idx}"
                
#                 # 제목 추출
#                 try:
#                     title_element = patent.find_element(By.CSS_SELECTOR, "h1.title button.link.under, h1.title .link.under")
#                     title = title_element.text
#                 except:
#                     title = "Unknown Title"
                
#                 print(f"처리 중: {app_number} - {title[:30]}...")
                
#                 # 특허 항목 클릭 - 2분할 화면 열기
#                 try:
#                     title_button = patent.find_element(By.CSS_SELECTOR, "h1.title button.link.under, h1.title .link.under")
#                     driver.execute_script("arguments[0].click();", title_button)
#                     print("특허 항목 클릭")
#                 except:
#                     print("특허 항목 클릭 실패")
#                     capture_screenshot(f"click_error_{app_number}")
#                     continue
                
#                 # 2분할 화면 패널 로드 대기
#                 try:
#                     detail_panel = wait.until(EC.presence_of_element_located((By.ID, "mainResultDetailArea")))
#                     time.sleep(2)  # 상세 내용 로딩 대기
#                     print("상세 패널 로드됨")
#                 except:
#                     print("상세 패널 로드 실패")
#                     capture_screenshot(f"detail_panel_error_{app_number}")
#                     continue
                
#                 # PDF 다운로드
#                 download_success = download_pdf_from_detail_panel()
                
#                 # 다운로드 기록 저장
#                 status = "success" if download_success else "failed"
#                 download_records.loc[len(download_records)] = [current_page, idx+1, app_number, title, status]
                
#                 # 상세 패널 닫기
#                 try:
#                     close_button = driver.find_element(By.CSS_SELECTOR, "#mainResultDetailArea .btn-close")
#                     driver.execute_script("arguments[0].click();", close_button)
#                     print("상세 패널 닫기 완료")
#                 except:
#                     try:
#                         # 자바스크립트로 닫기 시도
#                         driver.execute_script("searchDrag.toggleDetail(false);")
#                         print("자바스크립트로 상세 패널 닫기 완료")
#                     except:
#                         print("상세 패널 닫기 실패")
#                         capture_screenshot(f"close_panel_error_{app_number}")
                
#                 time.sleep(2)  # 패널 닫힘 대기
                
#             except Exception as e:
#                 print(f"특허 처리 중 오류 발생: {e}")
#                 traceback.print_exc()
#                 capture_screenshot(f"process_error_{current_page}_{idx}")
                
#                 # 다운로드 기록에 오류 추가
#                 download_records.loc[len(download_records)] = [
#                     current_page, 
#                     idx+1, 
#                     app_number if 'app_number' in locals() else f"Unknown_{current_page}_{idx}", 
#                     title if 'title' in locals() else "Unknown Title", 
#                     f"error: {str(e)}"
#                 ]
                
#                 # 상세 패널이 열려 있으면 닫기
#                 try:
#                     if driver.find_element(By.CSS_SELECTOR, "#mainResultDetailArea[style*='display: block']"):
#                         driver.execute_script("searchDrag.toggleDetail(false);")
#                 except:
#                     pass
            
#             # 서버 부하 방지를 위한 대기 시간
#             time.sleep(3)
        
#         # 기록 저장 (주기적으로 저장하여 데이터 유실 방지)
#         download_records.to_csv(os.path.join(download_path, "download_records.csv"), index=False)
        
#     except Exception as e:
#         print(f"페이지 처리 중 오류 발생: {e}")
#         traceback.print_exc()
#         capture_screenshot(f"page_error_{current_page}")

# def download_pdf_from_detail_panel():
#     try:
#         # 상세 패널에서 공개전문 또는 공고전문 버튼 찾기
#         pdf_button = None
#         pdf_button_selectors = [
#             "#tabContents a.btn-doc:contains('공개전문')",
#             "#tabContents a.btn-doc:contains('공고전문')",
#             "#viewTabMenu a:contains('공개전문')",
#             "#viewTabMenu a:contains('공고전문')",
#             "//a[contains(text(),'공개전문')]",
#             "//a[contains(text(),'공고전문')]",
#             ".btn-icon.btn-document",
#             ".btn-icon.btn-viewer"
#         ]
        
#         for selector in pdf_button_selectors:
#             try:
#                 if selector.startswith("//"):
#                     pdf_button = wait.until(EC.element_to_be_clickable((By.XPATH, selector)))
#                 else:
#                     pdf_button = wait.until(EC.element_to_be_clickable((By.CSS_SELECTOR, selector)))
#                 print(f"PDF 문서 버튼 찾음: {selector}")
#                 break
#             except:
#                 continue
        
#         if not pdf_button:
#             print("PDF 문서 버튼을 찾을 수 없습니다")
#             capture_screenshot("pdf_button_not_found")
#             return False
        
#         # PDF 문서 버튼 클릭
#         driver.execute_script("arguments[0].click();", pdf_button)
#         time.sleep(3)  # PDF 뷰어 로딩 대기
#         print("PDF 문서 버튼 클릭 완료")
        
#         # PDF 뷰어에서 저장 버튼 찾기
#         save_button = None
#         save_button_selectors = [
#             ".btn-icon.btn-save",
#             ".pdf-save",
#             "button[title='저장']",
#             "//button[contains(@title,'저장')]",
#             "//button[contains(@class,'save')]"
#         ]
        
#         for selector in save_button_selectors:
#             try:
#                 if selector.startswith("//"):
#                     save_button = wait.until(EC.element_to_be_clickable((By.XPATH, selector)))
#                 else:
#                     save_button = wait.until(EC.element_to_be_clickable((By.CSS_SELECTOR, selector)))
#                 print(f"저장 버튼 찾음: {selector}")
#                 break
#             except:
#                 continue
        
#         if not save_button:
#             print("저장 버튼을 찾을 수 없습니다")
#             capture_screenshot("save_button_not_found")
#             return False
        
#         # 저장 버튼 클릭
#         driver.execute_script("arguments[0].click();", save_button)
#         time.sleep(2)  # 파일 탐색기 다이얼로그 나타날 때까지 대기
#         print("저장 버튼 클릭 완료, 파일 탐색기 다이얼로그 대기 중...")
        
#         # 윈도우 파일 탐색기 다이얼로그 처리
#         try:
#             # 파일명 필드로 이동 (Tab 키 사용)
#             time.sleep(1)
#             pyautogui.press('tab')
#             time.sleep(0.5)
            
#             # 저장 버튼 클릭 (Alt+S)
#             pyautogui.hotkey('alt', 's')
#             print("파일 탐색기에서 저장 버튼 클릭")
            
#             # 파일 이미 존재하는 경우 대체 확인 다이얼로그 처리
#             time.sleep(2)
#             try:
#                 # 화면 찾기 - 확인 다이얼로그는
#                 # 대체(Y) 버튼 클릭
#                 pyautogui.press('y')
#                 print("파일 대체 확인 다이얼로그 처리됨")
#             except:
#                 pass
            
#             # 다운로드 완료 대기
#             time.sleep(5)
#             print("PDF 파일 저장 완료")
            
#             # PDF 뷰어 닫기 버튼 찾아 클릭
#             try:
#                 close_buttons = driver.find_elements(By.CSS_SELECTOR, ".btn-close, .close-button")
#                 if close_buttons:
#                     driver.execute_script("arguments[0].click();", close_buttons[0])
#                     print("PDF 뷰어 닫기 버튼 클릭")
#                     time.sleep(1)
#             except:
#                 print("PDF 뷰어 닫기 버튼을 찾을 수 없습니다")
            
#             return True
            
#         except Exception as e:
#             print(f"파일 탐색기 다이얼로그 처리 중 오류: {e}")
#             traceback.print_exc()
#             capture_screenshot("file_dialog_error")
#             return False
        
#     except Exception as e:
#         print(f"PDF 다운로드 중 오류 발생: {e}")
#         traceback.print_exc()
#         capture_screenshot("pdf_download_error")
#         return False

# def navigate_to_next_page(current_page):
#     try:
#         # 다음 페이지 버튼 찾기
#         next_page_button = None
        
#         # 여러 선택자 시도
#         selectors = [
#             f".pagination a[title='{current_page+1}페이지']",
#             f"//a[contains(@class, 'page-link') and text()='{current_page+1}']",
#             ".pagination .next",
#             ".pagination a.arrow_next"
#         ]
        
#         for selector in selectors:
#             try:
#                 if selector.startswith("//"):
#                     next_page_button = wait.until(EC.element_to_be_clickable((By.XPATH, selector)))
#                 else:
#                     next_page_button = wait.until(EC.element_to_be_clickable((By.CSS_SELECTOR, selector)))
#                 break
#             except:
#                 continue
        
#         if next_page_button:
#             driver.execute_script("arguments[0].click();", next_page_button)
#         else:
#             # 직접 자바스크립트로 페이지 이동 시도
#             try:
#                 driver.execute_script(f"goPage({current_page+1})")
#             except:
#                 print(f"페이지 {current_page+1}로 이동할 수 없습니다")
#                 capture_screenshot("page_navigation_failed")
#                 return False
        
#         # 페이지 로딩 대기
#         wait.until(EC.presence_of_element_located((By.CSS_SELECTOR, ".result-item")))
#         time.sleep(3)  # 추가 대기 시간
#         return True
        
#     except Exception as e:
#         print(f"페이지 이동 중 오류 발생: {e}")
#         traceback.print_exc()
#         capture_screenshot("page_navigation_error")
#         return False

# def main():
#     try:
#         # 검색 실행 및 총 페이지 수 가져오기
#         total_pages = search_patents()
#         if total_pages == 0:
#             print("검색 결과를 가져올 수 없어 프로그램을 종료합니다.")
#             return
            
#         # 지정된 페이지 범위만 처리
#         start_page = 1
#         end_page = min(total_pages, 100)  # 처음 100페이지만 처리
        
#         for current_page in range(start_page, end_page + 1):
#             print(f"\n=== 페이지 {current_page}/{end_page} 처리 중... ===")
#             process_patent_list(current_page)
            
#             # 마지막 페이지가 아니면 다음 페이지로 이동
#             if current_page < end_page:
#                 success = navigate_to_next_page(current_page)
#                 if not success:
#                     print(f"페이지 {current_page+1}로 이동 실패, 프로그램을 종료합니다.")
#                     break
            
#             # 다운로드 제한 고려 (09:00~19:00: 최대 500건, 그 외 시간: 최대 5,000건)
#             if current_page % 10 == 0:
#                 print(f"페이지 {current_page}까지 처리 완료. 10분간 휴식...")
#                 time.sleep(600)  # 10분 대기
        
#         print("모든 페이지 처리 완료!")
        
#     except Exception as e:
#         print(f"실행 중 오류 발생: {e}")
#         traceback.print_exc()
#         capture_screenshot("main_error")
    
#     finally:
#         # 최종 결과 저장
#         download_records.to_csv(os.path.join(download_path, "download_records_final.csv"), index=False)
#         # 브라우저 종료
#         driver.quit()

# if __name__ == "__main__":
#     main()
