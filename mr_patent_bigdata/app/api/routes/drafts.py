from fastapi import APIRouter, HTTPException, Depends, Query
from typing import List, Optional, Dict, Any
from datetime import datetime, timezone
import logging

from app.core.database import database
from app.schemas.patent import PatentDraftCreate, PatentDraftResponse
from app.services.vectorizer import get_tfidf_vector, get_kobert_vector
import numpy as np

router = APIRouter(prefix="/api", tags=["drafts"])

# 로깅 설정
logger = logging.getLogger(__name__)

def get_current_timestamp():
    """현재 시간을 ISO 8601 형식으로 변환 (UTC)"""
    return datetime.now(timezone.utc).isoformat().replace('+00:00', 'Z')

@router.get("/draft/{patent_draft_id}", response_model=Dict[str, Any])
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
            detail={
                "code": "DRAFT_NOT_FOUND",
                "message": "해당 ID의 특허 초안을 찾을 수 없습니다.",
                "timestamp": get_current_timestamp()
            }
        )
    
    # 바이너리 벡터 필드 제외
    draft_dict = {k: v for k, v in dict(draft).items() if not k.endswith('_vector')}
    
    # 날짜/시간 필드 변환
    if "patent_draft_created_at" in draft_dict:
        draft_dict["created_at"] = draft_dict.pop("patent_draft_created_at").isoformat() + 'Z'
    if "patent_draft_updated_at" in draft_dict:
        draft_dict["updated_at"] = draft_dict.pop("patent_draft_updated_at").isoformat() + 'Z'
    
    return {
        "data": draft_dict,
        "timestamp": get_current_timestamp()
    }

@router.post("/folder/{user_patent_folder_id}/draft", response_model=Dict[str, Any])
async def create_or_update_draft(
    user_patent_folder_id: int,
    draft: PatentDraftCreate
):
    """특허 초안 저장 또는 수정"""
    try:
        # 폴더 존재 확인
        folder_query = """
        SELECT * FROM user_patent_folder 
        WHERE user_patent_folder_id = :folder_id
        """
        folder = await database.fetch_one(
            query=folder_query, 
            values={"folder_id": user_patent_folder_id}
        )
        
        if not folder:
            raise HTTPException(
                status_code=404,
                detail={
                    "code": "FOLDER_NOT_FOUND",
                    "message": "지정한 폴더를 찾을 수 없습니다.",
                    "timestamp": get_current_timestamp()
                }
            )
        
        now = datetime.now(timezone.utc)
        
        # 각 필드에 대한 벡터 생성
        vector_fields = {}
        fields = ["title", "technical_field", "background", "problem", 
                "solution", "effect", "detailed", "summary", "claim"]
        
        for field in fields:
            text = getattr(draft, f"patent_draft_{field}", "")
            if text:
                try:
                    # 벡터 생성
                    tfidf_vector = get_tfidf_vector(text)
                    kobert_vector = get_kobert_vector(text)
                    
                    # 벡터 필드에 저장
                    vector_fields[f"patent_draft_{field}_tfidf_vector"] = tfidf_vector.tobytes()
                    vector_fields[f"patent_draft_{field}_kobert_vector"] = kobert_vector.tobytes()
                except Exception as e:
                    # 벡터화 실패 시 로그 기록
                    logger.error(f"벡터화 실패 ({field}): {str(e)}")
                    # 빈 벡터 할당
                    vector_fields[f"patent_draft_{field}_tfidf_vector"] = np.zeros(10).tobytes()
                    vector_fields[f"patent_draft_{field}_kobert_vector"] = np.zeros(768).tobytes()
        
        # 초안이 이미 존재하는지 확인
        existing_draft_query = """
        SELECT * FROM patent_draft
        WHERE user_patent_folder_id = :folder_id
        AND patent_draft_id = :draft_id
        """
        
        draft_id = getattr(draft, "patent_draft_id", None)
        existing_draft = None
        
        if draft_id:
            existing_draft = await database.fetch_one(
                query=existing_draft_query,
                values={
                    "folder_id": user_patent_folder_id,
                    "draft_id": draft_id
                }
            )
        
        if existing_draft:
            # 초안 업데이트
            update_fields = ", ".join([
                f"patent_draft_{field} = :{field}" 
                for field in ["title", "technical_field", "background", "problem", 
                            "solution", "effect", "detailed", "summary", "claim"]
            ])
            update_fields += ", patent_draft_updated_at = :updated_at"
            
            # 벡터 필드 추가
            for field_name in vector_fields.keys():
                field_suffix = field_name.split('patent_draft_')[1]
                update_fields += f", {field_name} = :{field_suffix}"
            
            update_query = f"""
            UPDATE patent_draft
            SET {update_fields}
            WHERE patent_draft_id = :draft_id
            """
            
            # 안전하게 draft 필드만 추출
            draft_values = {}
            for field in draft.__dict__:
                if field.startswith("patent_draft_"):
                    field_suffix = field.split("patent_draft_")[1]
                    draft_values[field_suffix] = getattr(draft, field)
            
            values = draft_values
            values.update({
                "updated_at": now,
                "draft_id": existing_draft["patent_draft_id"]
            })
            
            # 벡터 값 추가 (안전하게)
            for field_name, vector_value in vector_fields.items():
                field_suffix = field_name.split('patent_draft_')[1]
                values[field_suffix] = vector_value
            
            # id 키가 포함되어 있으면 제거 (SQL 바인딩 오류 방지)
            if 'id' in values and ':id' not in update_query:
                values.pop('id')
            
            await database.execute(query=update_query, values=values)
            
            return {
                "data": {
                    "patent_draft_id": existing_draft["patent_draft_id"],
                    "created_at": now.isoformat().replace('+00:00', 'Z')
                },
                "timestamp": get_current_timestamp()
            }
        else:
            # 새 초안 생성
            field_names = ["user_patent_folder_id"]
            field_names.extend([f"patent_draft_{field}" for field in [
                "title", "technical_field", "background", "problem", 
                "solution", "effect", "detailed", "summary", "claim"
            ]])
            field_names.extend(["patent_draft_created_at", "patent_draft_updated_at"])
            # 벡터 필드 추가
            field_names.extend(vector_fields.keys())
            
            # 안전한 바인딩 파라미터 생성
            placeholders = []
            for field in field_names:
                if field == "user_patent_folder_id":
                    placeholders.append(":user_patent_folder_id")
                elif field == "patent_draft_created_at":
                    placeholders.append(":created_at")
                elif field == "patent_draft_updated_at":
                    placeholders.append(":updated_at")
                else:
                    field_suffix = field.split('patent_draft_')[1]
                    placeholders.append(f":{field_suffix}")
            
            insert_query = f"""
            INSERT INTO patent_draft (
                {', '.join(field_names)}
            ) VALUES (
                {', '.join(placeholders)}
            )
            """
            
            # 안전하게 draft 필드만 추출
            draft_values = {}
            for field in draft.__dict__:
                if field.startswith("patent_draft_"):
                    field_suffix = field.split("patent_draft_")[1]
                    draft_values[field_suffix] = getattr(draft, field)
            
            values = draft_values
            values.update({
                "user_patent_folder_id": user_patent_folder_id,
                "created_at": now,
                "updated_at": now
            })
            
            # 벡터 값 추가 (안전하게)
            for field_name, vector_value in vector_fields.items():
                field_suffix = field_name.split('patent_draft_')[1]
                values[field_suffix] = vector_value
            
            # id 키가 포함되어 있으면 제거 (SQL 바인딩 오류 방지)
            if 'id' in values and ':id' not in insert_query:
                values.pop('id')
                
            draft_id = await database.execute(query=insert_query, values=values)
            
            return {
                "data": {
                    "patent_draft_id": draft_id,
                    "created_at": now.isoformat().replace('+00:00', 'Z')
                },
                "timestamp": get_current_timestamp()
            }
    except Exception as e:
        logger.error(f"특허 초안 저장 중 오류: {str(e)}")
        raise HTTPException(
            status_code=500,
            detail={
                "code": "INTERNAL_ERROR",
                "message": f"특허 초안 저장 중 오류가 발생했습니다: {str(e)}",
                "timestamp": get_current_timestamp()
            }
        )

@router.get("/drafts/recent", response_model=Dict[str, Any])
async def get_recent_drafts(user_id: int, limit: int = 5):
    """사용자의 최근 특허 초안 목록 조회 (최대 5개)"""
    # 최근 초안 조회 (최대 limit 개수만큼)
    query = """
    SELECT pd.patent_draft_id, pd.patent_draft_title, pd.patent_draft_summary, pd.patent_draft_updated_at 
    FROM patent_draft pd
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
        return {
            "data": {"patent_drafts": []},
            "timestamp": get_current_timestamp()
        }
    
    result = []
    for draft in drafts:
        result.append({
            "patent_draft_id": draft["patent_draft_id"],
            "patent_draft_title": draft["patent_draft_title"],
            "patent_draft_summary": draft["patent_draft_summary"],
            "updated_at": draft["patent_draft_updated_at"].isoformat() + 'Z'
        })
    
    return {
        "data": {"patent_drafts": result},
        "timestamp": get_current_timestamp()
    }
