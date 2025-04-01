package com.ssafy.mr_patent_android.data.remote

import com.ssafy.mr_patent_android.base.BaseResponse
import com.ssafy.mr_patent_android.data.model.dto.EmailCodeRequest
import com.ssafy.mr_patent_android.data.model.dto.LoginRequest
import com.ssafy.mr_patent_android.data.model.response.LoginResponse
import com.ssafy.mr_patent_android.data.model.dto.PwdChangeRequest
import com.ssafy.mr_patent_android.data.model.dto.UserDto
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface FileService {
    @GET("s3/upload-url")
    suspend fun getPreSignedUrl(
        @Query("filename") fileName: String,
        @Query("contenttype") contentType: String
    ): Response<BaseResponse<String>>

    @PUT
    suspend fun uploadFile(@Url url: String, @Body file: RequestBody?): Response<Unit>
}