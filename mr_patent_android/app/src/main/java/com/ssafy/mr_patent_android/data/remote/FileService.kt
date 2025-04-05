package com.ssafy.mr_patent_android.data.remote

import com.ssafy.mr_patent_android.base.BaseResponse
import com.ssafy.mr_patent_android.data.model.response.PatentContentResponse
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query
import retrofit2.http.Url

interface FileService {
    @GET("s3/upload-url")
    suspend fun getPreSignedUrl(
        @Query("filename") fileName: String,
        @Query("contenttype") contentType: String
    ): Response<BaseResponse<String>>

//    @GET("user/profile-image/upload-url")
//    suspend fun getImagePreSignedUrl(
//        @Query("filename") fileName: String,
//        @Query("contenttype") contentType: String
//    ): Response<BaseResponse>

    @PUT
    suspend fun uploadFile(@Url url: String, @Body file: RequestBody?): Response<Unit>

    @POST("patent/ocr/extract-draft")
    suspend fun getOcrContent(

    ): Response<BaseResponse<PatentContentResponse>>
}