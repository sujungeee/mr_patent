package com.ssafy.mr_patent_android.data.remote

import com.ssafy.mr_patent_android.base.BaseResponse
import com.ssafy.mr_patent_android.data.model.dto.ChatMessageDto
import com.ssafy.mr_patent_android.data.model.dto.ChatRoomDto
import com.ssafy.mr_patent_android.data.model.dto.ChatRoomRequest
import com.ssafy.mr_patent_android.data.model.dto.EmailCodeRequest
import com.ssafy.mr_patent_android.data.model.dto.LoginRequest
import com.ssafy.mr_patent_android.data.model.response.LoginResponse
import com.ssafy.mr_patent_android.data.model.dto.PwdChangeRequest
import com.ssafy.mr_patent_android.data.model.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ChatService {
    @POST("chat/rooms/create")
    suspend fun createChatRoom(
        @Body request: ChatRoomRequest
    ) : Response<BaseResponse<String>>

    @GET("chat/rooms/{userid}")
    suspend fun getChatRoomList(
        @Path("userid") userId: Int
    ) : Response<BaseResponse<List<ChatRoomDto>>>

    @GET("chat/messages/{roomid}?lastMessageid={lastmessageid}")
    suspend fun getChatMessageList(
        @Path("roomid") roomId: Int,
        @Path("lastmessageid") lastMessageId: Int
    ) : Response<BaseResponse<List<ChatMessageDto>>>
}