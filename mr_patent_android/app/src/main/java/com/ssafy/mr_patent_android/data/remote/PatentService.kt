package com.ssafy.mr_patent_android.data.remote

import com.ssafy.mr_patent_android.base.BaseResponse
import com.ssafy.mr_patent_android.data.model.dto.FolderDto
import com.ssafy.mr_patent_android.data.model.dto.FolderRequest
import com.ssafy.mr_patent_android.data.model.response.PatentRecentResponse
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
    ) : Response<BaseResponse<FolderDto>>

    @GET("drafts/recent")
    suspend fun getRecentPatentList(
        @Query("user_id") userId: Int,
        @Query("limit") limit: Int
    ): Response<BaseResponse<PatentRecentResponse>>

    @POST("folder")
    suspend fun addFolder(
        @Body request: FolderRequest
    ) : Response<BaseResponse<Boolean>>

}