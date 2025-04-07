package com.ssafy.mr_patent_android.data.remote

import com.ssafy.mr_patent_android.base.BaseResponse
import com.ssafy.mr_patent_android.data.model.dto.FolderDto
import com.ssafy.mr_patent_android.data.model.dto.FolderRequest
import com.ssafy.mr_patent_android.data.model.dto.PatentDraftDto
import com.ssafy.mr_patent_android.data.model.response.AddDraftResponse
import com.ssafy.mr_patent_android.data.model.response.FitnessResultResponse
import com.ssafy.mr_patent_android.data.model.response.MessageResponse
import com.ssafy.mr_patent_android.data.model.response.PatentContentResponse
import com.ssafy.mr_patent_android.data.model.response.PatentListResponse
import com.ssafy.mr_patent_android.data.model.response.PatentRecentResponse
import com.ssafy.mr_patent_android.data.model.response.SimiliarityResultResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface PatentService {
    @GET("folders/{userid}")
    suspend fun getFolderList(
        @Path("user_id") userId: Int
    ): Response<BaseResponse<FolderDto>>

    @GET("drafts/recent")
    suspend fun getRecentPatentList(
        @Query("user_id") userId: Int,
        @Query("limit") limit: Int
    ): Response<BaseResponse<PatentRecentResponse>>

    @POST("folder")
    suspend fun addFolder(
        @Body request: FolderRequest
    ): Response<BaseResponse<Boolean>>

    @DELETE("folder/{folder_id}")
    suspend fun deleteFolder(
        @Path("folder_id") folderId: Int
    ): Response<BaseResponse<MessageResponse>>

    @PUT("folder/{folder_id}")
    suspend fun editFolder(
        @Path("folder_id") folderId: Int,
        @Query("folder_name") folderName: String
    ): Response<BaseResponse<MessageResponse>>

    @POST("folder/{folder_id}/draft")
    suspend fun addDraft(
        @Path("folder_id") folderId: Int,
        @Body patentDraftDto: PatentDraftDto
    ): Response<BaseResponse<AddDraftResponse>>

    @POST("folder/{folder_id}/patents")
    suspend fun getPatentList(
        @Path("folder_id") folderId: Int
    ): Response<BaseResponse<PatentListResponse>>

    @GET("draft/{patent_draft_id}")
    suspend fun getPatentContent(
        @Path("patent_draft_id") patentDraftId: Int
    ): Response<BaseResponse<PatentContentResponse>>
}