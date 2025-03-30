from pydantic import BaseModel, Field
from typing import List, Optional
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
    user_patent_folder_name: str

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
    patent_draft_title: Optional[str] = None
    patent_draft_technical_field: Optional[str] = None
    patent_draft_background: Optional[str] = None
    patent_draft_problem: Optional[str] = None
    patent_draft_solution: Optional[str] = None
    patent_draft_effect: Optional[str] = None
    patent_draft_detailed: Optional[str] = None
    patent_draft_summary: Optional[str] = None
    patent_draft_claim: Optional[str] = None

class PatentDraftCreate(PatentDraftBase):
    user_patent_folder_id: int
    
class PatentDraftResponse(PatentDraftBase):
    patent_draft_id: int
    user_patent_folder_id: int
    patent_draft_created_at: datetime
    patent_draft_updated_at: datetime
    
    class Config:
        orm_mode = True
