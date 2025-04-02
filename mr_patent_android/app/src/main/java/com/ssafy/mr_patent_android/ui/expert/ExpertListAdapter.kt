package com.ssafy.mr_patent_android.ui.expert

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.mr_patent_android.data.model.dto.UserDto
import com.ssafy.mr_patent_android.databinding.ListItemExpertBinding


class ExpertListAdapter(val groupList: List<UserDto>, val itemClickListener:ItemClickListener) : RecyclerView.Adapter<ExpertListAdapter.ExpertListAdapter>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpertListAdapter {
        val binding = ListItemExpertBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpertListAdapter(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ExpertListAdapter, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = groupList.size

    fun interface ItemClickListener {
        fun onItemClick(id: Int)
    }

    inner class ExpertListAdapter(private val binding: ListItemExpertBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {

            binding.listItemExpert.setOnClickListener {
                itemClickListener.onItemClick(groupList[position].expertId)
            }

        }
    }
}
