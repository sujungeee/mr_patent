package com.ssafy.mr_patent_android.data.model.dto

import com.google.gson.annotations.SerializedName

data class ChatRoomDto(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("user_name") val userName: String,
    @SerializedName("user_image") val userImage: String,
    @SerializedName("room_id")val roomId: String,
    @SerializedName("last_message") val lastMessage: String,
    @SerializedName("last_message_time")val lastMessageTime: String,
    @SerializedName("unreadCount")val unreadCount: Int
){
    constructor() : this(-1,"","","","","",0)
}