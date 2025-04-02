package com.ssafy.mr_patent_android.data.model.dto

import android.net.Uri
import com.google.gson.annotations.SerializedName

data class ChatMessageDto(
    @SerializedName("chat_id")val chatId: Int?,
    @SerializedName("read")val isRead: Boolean,
    @SerializedName("message")val message: String?,
    @SerializedName("receiver_id")val receiverId: Int?,
    @SerializedName("room_id")val roomId: String,
    @SerializedName("timestamp")val timestamp: String?,
    @SerializedName("type")val type: String,
    @SerializedName("user_id")val userId: Int,
    @SerializedName("file_name")val fileName: String?,
    @SerializedName("file_url")val fileUrl: String?,
    @SerializedName("file_uri")val fileUri: Uri?,
    @SerializedName("message_type")val messageType: String
){
    constructor(): this(null, false, null, null, "", null, "", 0, "", "", Uri.EMPTY, "")

    constructor(messageType: String): this(null, false, null, null, "", null, "", 0, "", "", Uri.EMPTY, messageType)


    data class Files(
        @SerializedName("file_name")val fileName: String,
        @SerializedName("file_url") var fileUrl: String,
        @SerializedName("file_uri")val fileUri: Uri
    )

}