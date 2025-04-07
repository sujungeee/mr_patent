package com.ssafy.mr_patent_android.ui.chat

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.launchdarkly.eventsource.ConnectStrategy
import com.launchdarkly.eventsource.EventSource
import com.launchdarkly.eventsource.background.BackgroundEventSource
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.base.ApplicationClass.Companion.sharedPreferences
import com.ssafy.mr_patent_android.base.BaseFragment
import com.ssafy.mr_patent_android.data.model.dto.ChatRoomDto
import com.ssafy.mr_patent_android.databinding.FragmentChatListBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL
import java.util.concurrent.TimeUnit

private const val TAG = "ChatListFragment"
class ChatListFragment : BaseFragment<FragmentChatListBinding>(FragmentChatListBinding::bind, R.layout.fragment_chat_list) {
    val viewModel: ChatListViewModel by viewModels()
    val eventSource by lazy {
        BackgroundEventSource
            .Builder(
                SseEventHandler(viewModel),
                EventSource.Builder(
                    ConnectStrategy
                        .http(URL("https://j12d208.p.ssafy.io/api/chat/rooms/subscribe/${sharedPreferences.getUser().userId}"))
                        .header("Authorization", "Bearer ${sharedPreferences.getAToken()}")
                        .connectTimeout(5, TimeUnit.SECONDS)
                        .readTimeout(600, TimeUnit.SECONDS)
                )
            )
            .threadPriority(Thread.MAX_PRIORITY)
            .build()
    }
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
    viewModel.getChatRoomList()
}

private fun initObserver() {
    val adapter = ChatListAdapter { roomDto ->
        findNavController().navigate(
            ChatListFragmentDirections.actionNavFragmentChatToChatFragment(
                roomDto.receiverId, roomDto.expertId, roomDto.roomId, roomDto.userName, roomDto.userImage?:""
            ),
        )
    }
    binding.rvChatList.adapter = adapter

    viewModel.chatRoomList.observe(viewLifecycleOwner) { chatList ->
        Log.d(TAG, "initObserver: $chatList")
        adapter.submitList(chatList.sortedByDescending { it.lastMessageTime })
    }
}
    private fun initSse() {
        Log.d(TAG, "initSse: sse 시도 $eventSource")
        eventSource.start()
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onDestroy: sse 종료")
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                eventSource.close()
            } catch (e: Exception) {
                Log.e(TAG, "SSE 종료 중 예외 발생", e)
            }
        }
    }
}