package com.ssafy.mr_patent_android.data.model.dto

import com.google.gson.annotations.SerializedName

data class LogoutRequest (
    @SerializedName("user_id") val userId: Int,
    @SerializedName("refresh_token") val refreshToken: String
)