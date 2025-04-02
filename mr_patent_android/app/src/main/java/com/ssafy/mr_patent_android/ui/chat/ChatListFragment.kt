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

//        initSse()
        initView()
        initObserver()

    }

    private fun initSse() {
        val eventSource: BackgroundEventSource = BackgroundEventSource
            .Builder(
                SseEventHandler(),
                EventSource.Builder(
                    ConnectStrategy
                        .http(URL("{https://j12d208.p.ssafy.io/api/chat/rooms/subscribe/${sharedPreferences.getUser().userId}"))
                        // 커스텀 요청 헤더를 명시
                        .header(
                            "Authorization",
                            "Bearer {token}"
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

    private fun initView() {

        val list= listOf(
            ChatRoomDto(1,-1,"d","da","da","d","da",1),
            ChatRoomDto(1,1,"d","da","da","d","da",1),
            ChatRoomDto(1,1,"d","da","da","d","da",1),
            ChatRoomDto(1,1,"d","da","da","d","da",1),
            ChatRoomDto(1,1,"d","da","da","d","da",1),
            ChatRoomDto(1,1,"d","da","da","d","da",1),
            ChatRoomDto(1,1,"d","da","da","d","da",0),
            ChatRoomDto(1,1,"d","da","da","d","da",0),
            ChatRoomDto(1,1,"d","da","da","d","da",1),
            ChatRoomDto(1,1,"d","da","da","d","da",1),
        )
        Log.d(TAG, "initView: $list ")
        binding.rvChatList.adapter = ChatListAdapter(list){ it->
            findNavController().navigate(ChatListFragmentDirections.actionNavFragmentChatToChatFragment(it.userId,it.expertId,it.roomId,it.userName, it.userImage))
        }
        Log.d(TAG, "initView: ${binding.rvChatList.adapter}")


//        viewModel.getChatRoomList()
    }

    private fun initObserver() {
        viewModel.chatRoomList.observe(viewLifecycleOwner) {
            Log.d(TAG, "initObserver: $it")
            binding.rvChatList.adapter = ChatListAdapter(it){ it ->
                findNavController().navigate(ChatListFragmentDirections.actionNavFragmentChatToChatFragment(it.userId,it.expertId,it.roomId,it.userName, it.userImage))
            }
        }
    }
}