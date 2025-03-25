package com.ssafy.mr_patent_android.data.model.dto

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("user_id") val user_id: Int,
    @SerializedName("user_nickname")val user_nickname: String,
    @SerializedName("user_role")val user_role: Int
)