package com.ssafy.mr_patent_android.ui.study

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.data.model.dto.ChatRoomDto
import com.ssafy.mr_patent_android.data.model.dto.WordDto
import com.ssafy.mr_patent_android.databinding.ListItemCardBinding
import com.ssafy.mr_patent_android.databinding.ListItemChatRoomBinding

class WordCardAdapter(val word_list: List<WordDto.Word>, val itemClickListener:ItemClickListener) : RecyclerView.Adapter<WordCardAdapter.WordCardAdapter>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordCardAdapter {
        val binding =
            ListItemCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)


        return WordCardAdapter(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: WordCardAdapter, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = word_list.size

    fun interface ItemClickListener {
        fun onItemClick(position: Int)
    }

    inner class WordCardAdapter(private val binding: ListItemCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.flashCardWord.cardStudyText.text = word_list[position].word_name
            binding.flashCardMean.cardStudyText.text = word_list[position].word_mean
            binding.tBtnBookmark.setOnClickListener {
                itemClickListener.onItemClick(position)
            }

        }
    }
}