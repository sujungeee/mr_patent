package com.ssafy.mr_patent_android.data.remote

import com.ssafy.mr_patent_android.base.BaseResponse
import com.ssafy.mr_patent_android.data.model.dto.EmailCodeRequest
import com.ssafy.mr_patent_android.data.model.dto.JoinExpertRequest
import com.ssafy.mr_patent_android.data.model.dto.JoinRequest
import com.ssafy.mr_patent_android.data.model.dto.LoginRequest
import com.ssafy.mr_patent_android.data.model.response.LoginResponse
import com.ssafy.mr_patent_android.data.model.dto.PwdChangeRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthService {
    @POST("user/reissue")
    suspend fun reissue(
        @Body refreshToken: String
    ): Response<BaseResponse<LoginResponse>>

    @POST("user/login")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): Response<BaseResponse<LoginResponse>>

    @POST("email/verification")
    suspend fun emailVerify(
        @Body emailCodeRequest: EmailCodeRequest
    ): Response<BaseResponse<Boolean>>

    @POST("email/request")
    suspend fun sendCode(
        @Body userEmail: String
    ): Response<BaseResponse<String>>

    @PATCH("user/pw-change")
    suspend fun changePassword(
        @Body pwdChangeRequest: PwdChangeRequest
    ): Response<BaseResponse<Boolean>>

    @POST("user/")
    suspend fun joinMember(
        @Body joinRequest: JoinRequest
    ): Response<BaseResponse<Boolean>>

    @POST("user/expert")
    suspend fun joinExpert(
        @Body joinExpertRequest: JoinExpertRequest
    ): Response<BaseResponse<Boolean>>

    @GET("user/check-email")
    suspend fun checkDuplEmail(
        @Query("user_email") userEmail: String
    ): Response<BaseResponse<Boolean>>
}