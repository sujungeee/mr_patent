package com.ssafy.mr_patent_android.ui.chat

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.launchdarkly.eventsource.ConnectStrategy
import com.launchdarkly.eventsource.EventSource
import com.launchdarkly.eventsource.background.BackgroundEventSource
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.base.ApplicationClass.Companion.sharedPreferences
import com.ssafy.mr_patent_android.base.BaseFragment
import com.ssafy.mr_patent_android.data.model.dto.ChatRoomDto
import com.ssafy.mr_patent_android.databinding.FragmentChatListBinding
import java.net.URL
import java.util.concurrent.TimeUnit

private const val TAG = "ChatListFragment"
class ChatListFragment : BaseFragment<FragmentChatListBinding>(FragmentChatListBinding::bind, R.layout.fragment_chat_list) {
    val viewModel: ChatListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    initSse()
    initView()
    initObserver()
}

fun initView() {
    // 임시 데이터 추가
    val dummyData = listOf(
        ChatRoomDto(1,1, "User A","", "Hello!", "2025-04-02 10:00", "https://example.com/imageA.jpg",1),
        ChatRoomDto(2,1, "User B","", "How are you?", "2025-04-02 09:45", "https://example.com/imageB.jpg",2),
        ChatRoomDto(3,1, "User C","", "See you later", "2025-04-02 08:30", "https://example.com/imageC.jpg",3)
    )
    viewModel.setChatRoomList(dummyData)
}

private fun initObserver() {
    val adapter = ChatListAdapter { roomDto ->
        findNavController().navigate(
            ChatListFragmentDirections.actionNavFragmentChatToChatFragment(
                roomDto.userId, roomDto.expertId, roomDto.roomId, roomDto.userName, roomDto.userImage
            )
        )
    }
    binding.rvChatList.adapter = adapter

    viewModel.chatRoomList.observe(viewLifecycleOwner) { chatList ->
        Log.d(TAG, "initObserver: $chatList")
        adapter.submitList(chatList.sortedByDescending { it.lastMessageTime })
    }
}
    private fun initSse() {
        val eventSource: BackgroundEventSource = BackgroundEventSource
            .Builder(
                SseEventHandler(viewModel),
                EventSource.Builder(
                    ConnectStrategy
                        .http(URL("https://j12d208.p.ssafy.io/api/chat/rooms/subscribe/${sharedPreferences.getUser().userId}"))
                        // 커스텀 요청 헤더를 명시
                        .header(
                            "Authorization",
                            "Bearer ${sharedPreferences.getAToken()}"
                        )
                        .connectTimeout(3, TimeUnit.SECONDS)
                        // 최대 연결 유지 시간을 설정, 서버에 설정된 최대 연결 유지 시간보다 길게 설정
                        .readTimeout(600, TimeUnit.SECONDS)
                )
            )
            .threadPriority(Thread.MAX_PRIORITY)
            .build()

        eventSource.start()
    }
}