package com.ssafy.mr_patent_android.ui.study

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.data.model.dto.ChatRoomDto
import com.ssafy.mr_patent_android.data.model.dto.WordDto
import com.ssafy.mr_patent_android.databinding.ListItemCardBinding
import com.ssafy.mr_patent_android.databinding.ListItemChatRoomBinding
import com.ssafy.mr_patent_android.databinding.ListItemWordBinding

class WordAllAdapter(
    private val wordList: MutableList<WordDto.Word>,
    private val itemClickListener: ItemClickListener
) : RecyclerView.Adapter<WordAllAdapter.WordViewHolder>() {
    var lastClickTime = 0L
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val binding = ListItemWordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WordViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = wordList.size

    fun interface ItemClickListener {
        fun onItemClick(position: Int): Boolean
    }

    inner class WordViewHolder(private val binding: ListItemWordBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val word = wordList[position]
            binding.tvWord.text = word.word_name
            binding.tvMean.text = word.word_mean
            binding.tBtnBookmark.isChecked = word.is_bookmarked

            binding.cardLevel.setOnClickListener {
                binding.tBtnExpand.isChecked = !binding.expandableLayout.isVisible
                binding.expandableLayout.isVisible = !binding.expandableLayout.isVisible
            }
            binding.tBtnExpand.setOnClickListener {
                binding.tBtnExpand.isChecked = !binding.expandableLayout.isVisible
                binding.expandableLayout.isVisible = !binding.expandableLayout.isVisible
            }

            setBookmarkIcon(word.is_bookmarked)

            binding.tBtnBookmark.setThrottleClickListener {
                val toggled = !word.is_bookmarked

                setBookmarkIcon(toggled)

                val success = itemClickListener.onItemClick(position)
                if (!success) {
                    setBookmarkIcon(word.is_bookmarked)
                }
            }

            binding.tBtnBookmark.setThrottleClickListener {
                itemClickListener.onItemClick(position)
            }
        }
        private fun setBookmarkIcon(isBookmarked: Boolean) {
            val resId = if (isBookmarked) {
                R.drawable.bookmark_fill
            } else {
                R.drawable.bookmark_border
            }
            binding.tBtnBookmark.setBackgroundResource(resId)
        }
    }



    fun updateBookmarkState(position: Int, updatedWord: WordDto.Word) {
        wordList[position] = updatedWord
        notifyItemChanged(position)
    }

    fun View.setThrottleClickListener(interval: Long = 2000L, onClick: (View) -> Unit) {
        setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime >= interval) {
                lastClickTime = currentTime
                onClick(it)
            }
        }
    }
}
