package com.ssafy.mr_patent_android.data.model.dto

import com.google.gson.annotations.SerializedName

data class ChatMessageDto(
    @SerializedName("chat_id")val chatId: String,
    @SerializedName("is_read")val isRead: Boolean,
    @SerializedName("message")val message: String,
    @SerializedName("receiver_id")val receiverId: String,
    @SerializedName("room_id")val roomId: Int,
    @SerializedName("timestamp")val timestamp: String,
    @SerializedName("type")val type: String,
    @SerializedName("user_id")val userId: String
)