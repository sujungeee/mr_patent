package com.ssafy.mr_patent_android.ui.chat

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.data.model.dto.ChatRoomDto
import com.ssafy.mr_patent_android.data.model.dto.UserDto
import com.ssafy.mr_patent_android.databinding.ListItemChatRoomBinding
import com.ssafy.mr_patent_android.databinding.ListItemExpertBinding

private const val TAG = "ChatListAdapter"
class ChatListAdapter(val chatRoomList: List<ChatRoomDto>, val itemClickListener:ItemClickListener) : RecyclerView.Adapter<ChatListAdapter.ChatListAdapter>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListAdapter {
        val binding = ListItemChatRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatListAdapter(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ChatListAdapter, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = chatRoomList.size

    fun interface ItemClickListener {
        fun onItemClick(roomDto: ChatRoomDto)
    }

    inner class ChatListAdapter(private val binding: ListItemChatRoomBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            Log.d(TAG, "bind: ${chatRoomList[position]}")
            binding.tvName.text = chatRoomList[position].userName
            binding.tvChatPreview.text = chatRoomList[position].lastMessage
            binding.tvTime.text = chatRoomList[position].lastMessageTime
            if(chatRoomList[position].unreadCount > 0){
                binding.tvUnreadCount.visibility = ViewGroup.VISIBLE
                binding.tvUnreadCount.text = chatRoomList[position].unreadCount.toString()
            }
            Glide.with(binding.root)
                .load(chatRoomList[position].userImage)
                .fallback(R.drawable.user_profile)
                .error(R.drawable.image_load_error_icon)
                .into(binding.ivPatentAttorney)

            binding.listItemChatRoom.setOnClickListener {
                itemClickListener.onItemClick(chatRoomList[position])
            }
        }
    }
}
