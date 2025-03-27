from pydantic import BaseModel

class ProcessRequest(BaseModel):
    rtf_directory: str

class ProcessResponse(BaseModel):
    status: bool
    message: str
    task_id: str
    directory: str
