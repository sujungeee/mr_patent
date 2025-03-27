package com.ssafy.mr_patent_android.ui.chat

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.mr_patent_android.data.model.dto.ChatMessageDto
import com.ssafy.mr_patent_android.data.model.dto.ChatRoomDto
import com.ssafy.mr_patent_android.data.model.dto.UserDto
import com.ssafy.mr_patent_android.databinding.ListItemChatBinding
import com.ssafy.mr_patent_android.databinding.ListItemChatRoomBinding
import com.ssafy.mr_patent_android.databinding.ListItemExpertBinding


class MessageListAdapter(val groupList: List<ChatMessageDto>, val itemClickListener:ItemClickListener) : RecyclerView.Adapter<MessageListAdapter.MessageListAdapter>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageListAdapter {
        val binding = ListItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageListAdapter(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MessageListAdapter, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = groupList.size

    fun interface ItemClickListener {
        fun onItemClick(id: Int)
    }

    inner class MessageListAdapter(private val binding: ListItemChatBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {


        }
    }
}
