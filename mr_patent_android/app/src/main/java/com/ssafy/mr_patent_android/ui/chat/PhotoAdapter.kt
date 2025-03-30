package com.ssafy.mr_patent_android.ui.chat

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.data.model.dto.ChatMessageDto
import com.ssafy.mr_patent_android.data.model.dto.ChatRoomDto
import com.ssafy.mr_patent_android.databinding.ListItemChatRoomBinding

class PhotoAdapter(val photoList: List<ChatMessageDto.Files>, val itemClickListener:ItemClickListener) : RecyclerView.Adapter<PhotoAdapter.PhotoAdapter>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoAdapter {
        val binding =
            ListItemChatRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PhotoAdapter(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: PhotoAdapter, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = photoList.size

    fun interface ItemClickListener {
        fun onItemClick(id: Int)
    }

    inner class PhotoAdapter(private val binding: ListItemChatRoomBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {

        }
    }
}