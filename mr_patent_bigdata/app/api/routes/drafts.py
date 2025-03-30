# app/api/routes/drafts.py - 특허 초안 관련 API

from fastapi import APIRouter, HTTPException, Depends, Query
from typing import List, Optional
from datetime import datetime

from app.core.database import database
from app.schemas.patent import PatentDraftCreate, PatentDraftResponse

router = APIRouter(prefix="/api", tags=["drafts"])

@router.get("/draft/{patent_draft_id}", response_model=PatentDraftResponse)
async def get_patent_draft(patent_draft_id: int):
    """특정 ID의 특허 초안 조회"""
    query = """
    SELECT * FROM patent_draft
    WHERE patent_draft_id = :draft_id
    """
    
    draft = await database.fetch_one(
        query=query,
        values={"draft_id": patent_draft_id}
    )
    
    if not draft:
        raise HTTPException(
            status_code=404,
            detail="해당 ID의 특허 초안을 찾을 수 없습니다."
        )
    
    # 바이너리 벡터 필드 제외
    draft_dict = {k: v for k, v in dict(draft).items() if not k.endswith('_vector')}
    
    return draft_dict

@router.post("/topic/{user_patent_topic_id}/draft", response_model=PatentDraftResponse)
async def create_or_update_draft(
    user_patent_topic_id: int,
    draft: PatentDraftCreate
):
    """특허 초안 저장 또는 수정"""
    # 폴더 존재 확인
    folder_query = """
    SELECT * FROM user_patent_folder 
    WHERE user_patent_folder_id = :folder_id
    """
    folder = await database.fetch_one(
        query=folder_query, 
        values={"folder_id": draft.user_patent_folder_id}
    )
    
    if not folder:
        raise HTTPException(
            status_code=404,
            detail="지정한 폴더를 찾을 수 없습니다."
        )
    
    now = datetime.utcnow()
    
    # 초안이 이미 존재하는지 확인
    existing_draft_query = """
    SELECT * FROM patent_draft
    WHERE user_patent_folder_id = :folder_id
    AND patent_draft_id = :topic_id
    """
    existing_draft = await database.fetch_one(
        query=existing_draft_query,
        values={
            "folder_id": draft.user_patent_folder_id,
            "topic_id": user_patent_topic_id
        }
    )
    
    if existing_draft:
        # 초안 업데이트
        update_query = """
        UPDATE patent_draft
        SET 
            patent_draft_title = :title,
            patent_draft_technical_field = :technical_field,
            patent_draft_background = :background,
            patent_draft_problem = :problem,
            patent_draft_solution = :solution,
            patent_draft_effect = :effect,
            patent_draft_detailed = :detailed,
            patent_draft_summary = :summary,
            patent_draft_claim = :claim,
            patent_draft_updated_at = :updated_at
        WHERE patent_draft_id = :draft_id
        """
        
        values = {
            "title": draft.patent_draft_title,
            "technical_field": draft.patent_draft_technical_field,
            "background": draft.patent_draft_background,
            "problem": draft.patent_draft_problem,
            "solution": draft.patent_draft_solution,
            "effect": draft.patent_draft_effect,
            "detailed": draft.patent_draft_detailed,
            "summary": draft.patent_draft_summary,
            "claim": draft.patent_draft_claim,
            "updated_at": now,
            "draft_id": existing_draft["patent_draft_id"]
        }
        
        await database.execute(query=update_query, values=values)
        
        # 업데이트된 초안 조회
        updated_draft = await database.fetch_one(
            query="SELECT * FROM patent_draft WHERE patent_draft_id = :draft_id",
            values={"draft_id": existing_draft["patent_draft_id"]}
        )
        
        # 바이너리 벡터 필드 제외
        draft_dict = {k: v for k, v in dict(updated_draft).items() if not k.endswith('_vector')}
        return draft_dict
    else:
        # 새 초안 생성
        insert_query = """
        INSERT INTO patent_draft (
            user_patent_folder_id,
            patent_draft_title,
            patent_draft_technical_field,
            patent_draft_background,
            patent_draft_problem,
            patent_draft_solution,
            patent_draft_effect,
            patent_draft_detailed,
            patent_draft_summary,
            patent_draft_claim,
            patent_draft_created_at,
            patent_draft_updated_at
        ) VALUES (
            :folder_id,
            :title,
            :technical_field,
            :background,
            :problem,
            :solution,
            :effect,
            :detailed,
            :summary,
            :claim,
            :created_at,
            :updated_at
        )
        """
        
        values = {
            "folder_id": draft.user_patent_folder_id,
            "title": draft.patent_draft_title,
            "technical_field": draft.patent_draft_technical_field,
            "background": draft.patent_draft_background,
            "problem": draft.patent_draft_problem,
            "solution": draft.patent_draft_solution,
            "effect": draft.patent_draft_effect,
            "detailed": draft.patent_draft_detailed,
            "summary": draft.patent_draft_summary,
            "claim": draft.patent_draft_claim,
            "created_at": now,
            "updated_at": now
        }
        
        draft_id = await database.execute(query=insert_query, values=values)
        
        # 생성된 초안 조회
        created_draft = await database.fetch_one(
            query="SELECT * FROM patent_draft WHERE patent_draft_id = :draft_id",
            values={"draft_id": draft_id}
        )
        
        # 바이너리 벡터 필드 제외
        draft_dict = {k: v for k, v in dict(created_draft).items() if not k.endswith('_vector')}
        return draft_dict

@router.get("/drafts/recent", response_model=List[PatentDraftResponse])
async def get_recent_drafts(user_id: int, limit: int = 5):
    """사용자의 최근 특허 초안 목록 조회 (최대 5개)"""
    # 최근 초안 조회 (최대 limit 개수만큼)
    query = """
    SELECT pd.* FROM patent_draft pd
    JOIN user_patent_folder upf ON pd.user_patent_folder_id = upf.user_patent_folder_id
    WHERE upf.user_id = :user_id
    ORDER BY pd.patent_draft_updated_at DESC
    LIMIT :limit
    """
    
    drafts = await database.fetch_all(
        query=query,
        values={
            "user_id": user_id,
            "limit": min(limit, 5)  # 최대 5개로 제한
        }
    )
    
    if not drafts:
        return []
    
    # 각 초안에서 바이너리 벡터 필드 제외
    result = []
    for draft in drafts:
        draft_dict = {k: v for k, v in dict(draft).items() if not k.endswith('_vector')}
        result.append(draft_dict)
    
    return result
