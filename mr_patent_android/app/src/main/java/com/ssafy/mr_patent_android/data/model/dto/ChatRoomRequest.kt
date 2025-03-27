package com.ssafy.mr_patent_android.data.model.dto

import com.google.gson.annotations.SerializedName

data class ChatRoomRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("receiver_id")val receiverId: String,
)