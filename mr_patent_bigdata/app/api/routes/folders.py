# app/api/routes/folders.py - 폴더 관련 API

from fastapi import APIRouter, HTTPException, Depends, Query
from typing import List, Optional
from datetime import datetime
import sqlalchemy

from app.core.database import database
from app.schemas.patent import FolderCreate, FolderResponse, PatentDraftResponse

router = APIRouter(prefix="/api", tags=["folders"])

@router.get("/folders/{user_id}", response_model=List[FolderResponse])
async def get_user_folders(user_id: int):
    """사용자의 모든 특허 폴더 목록 조회"""
    query = """
    SELECT * FROM user_patent_folder
    WHERE user_id = :user_id
    ORDER BY user_patent_folder_updated_at DESC
    """
    
    folders = await database.fetch_all(
        query=query,
        values={"user_id": user_id}
    )
    
    if not folders:
        return []
    
    return [dict(folder) for folder in folders]

@router.post("/folder", response_model=FolderResponse)
async def create_folder(folder: FolderCreate):
    """새 특허 폴더 생성"""
    now = datetime.utcnow()
    
    query = """
    INSERT INTO user_patent_folder (
        user_id, 
        user_patent_folder_name, 
        user_patent_folder_created_at, 
        user_patent_folder_updated_at
    ) 
    VALUES (
        :user_id, 
        :folder_name, 
        :created_at, 
        :updated_at
    )
    """
    
    values = {
        "user_id": folder.user_id,
        "folder_name": folder.user_patent_folder_name,
        "created_at": now,
        "updated_at": now
    }
    
    try:
        folder_id = await database.execute(query=query, values=values)
        
        # 생성된 폴더 정보 조회
        get_query = """
        SELECT * FROM user_patent_folder 
        WHERE user_patent_folder_id = :folder_id
        """
        created_folder = await database.fetch_one(
            query=get_query, 
            values={"folder_id": folder_id}
        )
        
        return dict(created_folder)
    except Exception as e:
        raise HTTPException(
            status_code=500,
            detail=f"폴더 생성 중 오류가 발생했습니다: {str(e)}"
        )

@router.get("/folder/{folder_id}/patents", response_model=List[PatentDraftResponse])
async def get_folder_patents(folder_id: int):
    """특정 폴더에 속한 특허 초안 목록 조회"""
    # 폴더 존재 확인
    folder_query = """
    SELECT * FROM user_patent_folder 
    WHERE user_patent_folder_id = :folder_id
    """
    folder = await database.fetch_one(
        query=folder_query, 
        values={"folder_id": folder_id}
    )
    
    if not folder:
        raise HTTPException(
            status_code=404,
            detail="지정한 폴더를 찾을 수 없습니다."
        )
    
    # 폴더 내 특허 초안 조회
    patents_query = """
    SELECT * FROM patent_draft
    WHERE user_patent_folder_id = :folder_id
    ORDER BY patent_draft_updated_at DESC
    """
    
    patents = await database.fetch_all(
        query=patents_query,
        values={"folder_id": folder_id}
    )
    
    if not patents:
        return []
    
    return [dict(patent) for patent in patents]

@router.delete("/folder/{folder_id}")
async def delete_folder(folder_id: int):
    """폴더 및 폴더 내 모든 특허 초안 삭제"""
    # 트랜잭션 시작
    transaction = await database.transaction()
    
    try:
        # 폴더 존재 확인
        folder_query = """
        SELECT * FROM user_patent_folder 
        WHERE user_patent_folder_id = :folder_id
        """
        folder = await database.fetch_one(
            query=folder_query, 
            values={"folder_id": folder_id}
        )
        
        if not folder:
            await transaction.rollback()
            raise HTTPException(
                status_code=404,
                detail="지정한 폴더를 찾을 수 없습니다."
            )
        
        # 폴더 내 특허 초안 삭제
        delete_drafts_query = """
        DELETE FROM patent_draft
        WHERE user_patent_folder_id = :folder_id
        """
        await database.execute(
            query=delete_drafts_query,
            values={"folder_id": folder_id}
        )
        
        # 폴더 삭제
        delete_folder_query = """
        DELETE FROM user_patent_folder
        WHERE user_patent_folder_id = :folder_id
        """
        await database.execute(
            query=delete_folder_query,
            values={"folder_id": folder_id}
        )
        
        await transaction.commit()
        
        return {
            "status": True,
            "message": "폴더가 성공적으로 삭제되었습니다."
        }
        
    except Exception as e:
        await transaction.rollback()
        raise HTTPException(
            status_code=500,
            detail=f"폴더 삭제 중 오류가 발생했습니다: {str(e)}"
        )
