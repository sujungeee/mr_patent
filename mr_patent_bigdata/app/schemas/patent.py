from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any
from datetime import datetime

class ProcessRequest(BaseModel):
    rtf_directory: str

class ProcessResponse(BaseModel):
    status: bool
    message: str
    task_id: str
    directory: str

class ResumeRequest(BaseModel):
    rtf_directory: str
    start_index: int = 0  # 시작할 특허 인덱스 (기본값: 0)

class ResumeResponse(BaseModel):
    status: bool
    message: str
    task_id: str
    directory: str

class FolderBase(BaseModel):
    user_patent_folder_title: str

class FolderCreate(FolderBase):
    user_id: int
    
class FolderResponse(FolderBase):
    user_patent_folder_id: int
    user_id: int
    user_patent_folder_created_at: datetime
    user_patent_folder_updated_at: datetime
    
    class Config:
        orm_mode = True
        
class PatentDraftBase(BaseModel):
    patent_draft_title: str
    patent_draft_technical_field: str = ""
    patent_draft_background: str = ""
    patent_draft_problem: str = ""
    patent_draft_solution: str = ""
    patent_draft_effect: str = "" 
    patent_draft_detailed: str = ""
    patent_draft_summary: str = ""
    patent_draft_claim: str = ""

class PatentDraftCreate(PatentDraftBase):
    patent_draft_id: Optional[int] = None
    user_patent_folder_id: Optional[int] = None
    
class PatentDraftResponse(PatentDraftBase):
    patent_draft_id: int
    user_patent_folder_id: int
    patent_draft_created_at: datetime
    patent_draft_updated_at: datetime
    
    class Config:
        orm_mode = True