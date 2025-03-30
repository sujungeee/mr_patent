package com.ssafy.mr_patent_android.ui.chat

import android.content.Context
import android.os.Build
import android.renderscript.ScriptGroup.Binding
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.mr_patent_android.base.ApplicationClass.Companion.sharedPreferences
import com.ssafy.mr_patent_android.data.model.dto.ChatMessageDto
import com.ssafy.mr_patent_android.databinding.ListItemChatDividerBinding
import com.ssafy.mr_patent_android.databinding.ListItemChatFileBinding
import com.ssafy.mr_patent_android.databinding.ListItemChatMessageBinding
import com.ssafy.mr_patent_android.databinding.ListItemChatPhotoBinding
import java.text.SimpleDateFormat


class MessageListAdapter(val messageList: List<ChatMessageDto>, val itemClickListener:ItemClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var binding: Any


        if (viewType == MESSAGE_CONTENT) {
            binding = ListItemChatMessageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return MessageViewHolder(binding)
        }
        else if (viewType == FILE_CONTENT) {
            binding = ListItemChatFileBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return FileViewHolder(binding)
        }
        else if (viewType == PHOTO_CONTENT) {
            binding = ListItemChatPhotoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return PhotoViewHolder(binding)
        }
        else {
            binding = ListItemChatDividerBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return DividerViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is MessageViewHolder -> holder.bind(position)
            is FileViewHolder -> holder.bind(position)
            is PhotoViewHolder -> holder.bind(position)
            is DividerViewHolder -> holder.bind(position)
        }
    }

    override fun getItemCount(): Int = messageList.size

    override fun getItemViewType(position: Int): Int {
        return when {
            messageList[position].messageType == "TEXT" -> MESSAGE_CONTENT
            messageList[position].messageType == "PDF" || messageList[position].messageType=="DOC" -> FILE_CONTENT
            messageList[position].messageType == "IMAGE" -> PHOTO_CONTENT
            else -> DIVIDER
        }
    }

    interface ItemClickListener {
        fun onItemClick(id: Int)
        fun onPhotoClick(id: Int)
    }

    inner class MessageViewHolder(private val binding: ListItemChatMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            messageList[position].let {
                if (it.userId == sharedPreferences.getUser().userId) {
                    binding.llOtherMessageText.visibility = View.GONE
                    binding.tvUserMessageText.text = it.message
                    binding.tvUserMessageTime.text = it.timestamp
                    binding.vUserMessageIsRead.visibility = if (it.isRead) View.GONE else View.VISIBLE
                } else {
                    binding.llUserMessageText.visibility = View.GONE
                    binding.tvOtherMessageText.text = it.message
                    binding.tvOtherMessageTime.text = it.timestamp
                }
            }

        }
    }

    inner class FileViewHolder(private val binding: ListItemChatFileBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            if (messageList[position].userId == sharedPreferences.getUser().userId) {
                binding.llOtherMessageFile.visibility = View.GONE
                binding.icUserMessageFile.tvFileName.text = messageList[position].files[0].fileName
            } else {
                binding.llUserMessageFile.visibility = View.GONE
                binding.icOtherMessageFile.tvFileName.text = messageList[position].files[0].fileName
            }
        }
    }

    inner class PhotoViewHolder(private val binding: ListItemChatPhotoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            if (messageList[position].userId == sharedPreferences.getUser().userId) {
                binding.llOtherMessagePhoto.visibility = View.GONE
                binding.rvUserMessagePhoto.adapter = PhotoAdapter(messageList[position].files){
                    itemClickListener.onPhotoClick(it)
                }
            } else {
                binding.llUserMessagePhoto.visibility = View.GONE
                binding.llOtherMessagePhoto.visibility = View.GONE
                binding.rvUserMessagePhoto.adapter = PhotoAdapter(messageList[position].files){
                    itemClickListener.onPhotoClick(it)
                }
            }

        }
    }

    inner class DividerViewHolder(private val binding: ListItemChatDividerBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val format = SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val date = format.parse(messageList[position].timestamp)

            binding.tvChatDividerTime.text = format.format(date.date)
        }
    }

    companion object{
        const val MESSAGE_CONTENT = 0
        const val FILE_CONTENT = 1
        const val PHOTO_CONTENT = 2
        const val DIVIDER = 3
    }
}
