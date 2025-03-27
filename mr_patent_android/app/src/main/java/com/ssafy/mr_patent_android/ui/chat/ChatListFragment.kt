package com.ssafy.mr_patent_android.ui.chat

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.base.BaseFragment
import com.ssafy.mr_patent_android.data.model.dto.ChatRoomDto
import com.ssafy.mr_patent_android.databinding.FragmentChatListBinding

private const val TAG = "ChatListFragment"
class ChatListFragment : BaseFragment<FragmentChatListBinding>(FragmentChatListBinding::bind, R.layout.fragment_chat_list) {
    val viewModel: ChatListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initObserver()

    }

    private fun initView() {

        val list= listOf(
            ChatRoomDto(1,"d","da","da","d","da",1),
            ChatRoomDto(1,"d","da","da","d","da",1),
            ChatRoomDto(1,"d","da","da","d","da",1),
            ChatRoomDto(1,"d","da","da","d","da",1),
            ChatRoomDto(1,"d","da","da","d","da",1),
            ChatRoomDto(1,"d","da","da","d","da",1),
            ChatRoomDto(1,"d","da","da","d","da",0),
            ChatRoomDto(1,"d","da","da","d","da",0),
            ChatRoomDto(1,"d","da","da","d","da",1),
            ChatRoomDto(1,"d","da","da","d","da",1),
        )
        Log.d(TAG, "initView: $list ")
        binding.rvChatList.adapter = ChatListAdapter(list){ its ->
            findNavController().navigate(ChatListFragmentDirections.actionNavFragmentChatToChatFragment(its))
        }
        Log.d(TAG, "initView: ${binding.rvChatList.adapter}")


//        viewModel.getChatRoomList()
    }

    private fun initObserver() {
        viewModel.chatRoomList.observe(viewLifecycleOwner) {
            Log.d(TAG, "initObserver: $it")
            binding.rvChatList.adapter = ChatListAdapter(it){ its ->
                findNavController().navigate(ChatListFragmentDirections.actionNavFragmentChatToChatFragment(its))
            }
        }
    }
}