package com.ssafy.mr_patent_android.data.remote

import com.ssafy.mr_patent_android.base.BaseResponse
import com.ssafy.mr_patent_android.data.model.dto.EmailCodeRequest
import com.ssafy.mr_patent_android.data.model.dto.FcmRequest
import com.ssafy.mr_patent_android.data.model.dto.JoinExpertRequest
import com.ssafy.mr_patent_android.data.model.dto.JoinRequest
import com.ssafy.mr_patent_android.data.model.dto.LoginRequest
import com.ssafy.mr_patent_android.data.model.dto.LogoutRequest
import com.ssafy.mr_patent_android.data.model.response.LoginResponse
import com.ssafy.mr_patent_android.data.model.dto.PwdChangeRequest
import com.ssafy.mr_patent_android.data.model.dto.PwdEditRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthService {
    @POST("user")
    suspend fun joinMember(
        @Body joinRequest: JoinRequest
    ): Response<BaseResponse<Boolean?>>

    @POST("user/reissue")
    suspend fun reissue(
        @Body refreshToken: String
    ): Response<BaseResponse<LoginResponse>>

    @POST("user/logout")
    suspend fun logout(
        @Body logoutRequest: LogoutRequest
    ): Response<BaseResponse<Boolean?>>

    @POST("user/login")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): Response<LoginResponse>

    @POST("user/expert")
    suspend fun joinExpert(
        @Body joinExpertRequest: JoinExpertRequest
    ): Response<BaseResponse<Boolean?>>


    @GET("user/me")
    suspend fun getMyInfo(

    ): Response<BaseResponse<LoginResponse>>

    @DELETE("user/me")
    suspend fun deleteUser (
        @Path("user_id") userId: Int
    ): Response<BaseResponse<Boolean?>>

    @PATCH("user/me")
    suspend fun editUser(
        @Body userDto: LoginResponse
    ): Response<BaseResponse<Boolean?>>

    @PATCH("user/me/pw")
    suspend fun pwdEdit(
        @Body pwdEditRequest: PwdEditRequest
    ): Response<BaseResponse<Boolean?>>


    @POST("email/verification")
    suspend fun emailVerify(
        @Body emailCodeRequest: EmailCodeRequest
    ): Response<BaseResponse<Boolean>>

    @POST("email/request")
    suspend fun sendCode(
        @Body userEmail: String
    ): Response<BaseResponse<String>>

    @POST("email/password/verification")
    suspend fun sendCodePwd(
        @Body user_email: String
    ): Response<BaseResponse<String>>

    @PATCH("email/password/reset")
    suspend fun changePassword(
        @Body pwdChangeRequest: PwdChangeRequest
    ): Response<BaseResponse<Boolean>>

    @GET("email/check")
    suspend fun checkDuplEmail(
        @Query("email") userEmail: String
    ): Response<BaseResponse<Boolean>>

    @POST("fcm/token")
    suspend fun sendFcmToken(
        @Body fcmRequest: FcmRequest
    ): Response<BaseResponse<Boolean>>
}