package com.ssafy.mr_patent_android.data.model.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password")val password: String,
)