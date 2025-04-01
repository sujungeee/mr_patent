package com.ssafy.mr_patent_android.data.remote

import com.ssafy.mr_patent_android.base.BaseResponse
import com.ssafy.mr_patent_android.data.model.dto.FolderDto
import com.ssafy.mr_patent_android.data.model.dto.FolderRequest
import com.ssafy.mr_patent_android.data.model.response.FitnessResultResponse
import com.ssafy.mr_patent_android.data.model.response.PatentListResponse
import com.ssafy.mr_patent_android.data.model.response.PatentRecentResponse
import com.ssafy.mr_patent_android.data.model.response.SimiliarityResultResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
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

    @POST("folder/{folder_id}/patents")
    suspend fun getPatentList(
        @Path("folder_id") folderId: Int
    ): Response<BaseResponse<PatentListResponse>>

    @GET("patent/fitness/{user_patent_id")
    suspend fun getFitnessResult(
        @Path("user_patent_id") userPatentId: Int
    ): Response<BaseResponse<FitnessResultResponse>>

    @GET("patent/similiarity/{user_patent_id")
    suspend fun getSimiliarityResult(
        @Path("user_patent_id") userPatentId: Int
    ): Response<BaseResponse<SimiliarityResultResponse>>
}