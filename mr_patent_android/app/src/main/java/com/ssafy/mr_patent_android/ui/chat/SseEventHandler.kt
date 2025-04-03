package com.ssafy.mr_patent_android.ui.chat

import android.util.Log
import com.google.gson.Gson
import com.launchdarkly.eventsource.MessageEvent
import com.launchdarkly.eventsource.background.BackgroundEventHandler
import com.ssafy.mr_patent_android.data.model.dto.ChatRoomDto

private const val TAG = "SseEventHandler"
class SseEventHandler(private val viewModel: ChatListViewModel) : BackgroundEventHandler {

    override fun onOpen() {
        Log.d(TAG, "✅ SSE 연결 열림")
    }

    override fun onClosed() {
        // SSE 연결 종료시 처리 로직 작성
    }

    override fun onMessage(event: String, messageEvent: MessageEvent) {
        Log.d(TAG, "onMessage: $event, ${messageEvent.lastEventId}, ${messageEvent.data}")
    // SSE 이벤트 도착시 처리 로직 작성

        if (messageEvent != null) {
            val newMessage = Gson().fromJson(messageEvent.data, ChatRoomDto::class.java)
            Log.d(TAG, "새로운 메시지 수신: $newMessage")

            // ✅ 새로운 메시지를 받아서 채팅방 리스트 갱신
            viewModel.updateChatList(newMessage)

        }

        // event: String = 이벤트가 속한 채널 또는 토픽 이름
        // messageEvent.lastEventId: String = 도착한 이벤트 ID
        // messageEvent.data: String = 도착한 이벤트 데이터
    }

    override fun onComment(comment: String) {
    }

    override fun onError(t: Throwable?) {
        Log.e(TAG, "❌ SSE 오류 발생", t)
    }
}