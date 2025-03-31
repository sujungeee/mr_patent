package com.ssafy.mr_patent_android.ui.mypage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.data.model.response.PatentListResponse
import com.ssafy.mr_patent_android.databinding.ListItemPatentBinding

class PatentFolderDetailAdapter(var deleteFlag : Boolean = false, var patents: List<PatentListResponse.PatentSummaryInfo>, val itemClickListener : ItemClickListener)
    : RecyclerView.Adapter<PatentFolderDetailAdapter.PatentFolderDetailViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatentFolderDetailViewHolder {
        val binding = ListItemPatentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PatentFolderDetailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PatentFolderDetailViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = patents.size

    fun interface ItemClickListener {
        fun onItemClick(position: Int)
    }

    inner class PatentFolderDetailViewHolder(private val binding: ListItemPatentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.tvPatentName.text = patents[position].patentDraftTitle
            binding.tvDateTest.text = itemView.context.getString(R.string.tv_date_test, patents[position].createdAt)
            binding.tvSimiliarityTestResult.text = itemView.context.getString(R.string.tv_similiarity_test_result, patents[position].patentSimiliarityResult)
            binding.tvSimiliarityTestResultPercent.text = "${patents[position].patentSimiliarityResultScore}%"
            binding.pbSimiliarityResult.progress = patents[position].patentSimiliarityResultScore
            when (patents[position].patentSimiliarityResult) {
                "적합" -> {
                    binding.pbSimiliarityResult.progressTintList = itemView.context.getColorStateList(R.color.mr_blue)
                }
                "부분 적합" -> {
                    binding.pbSimiliarityResult.progressTintList = itemView.context.getColorStateList(R.color.mr_green)
                }
                "부적합" -> {
                    binding.pbSimiliarityResult.progressTintList = itemView.context.getColorStateList(R.color.mr_red)
                }
            }
            binding.tvPatentDetailGo.setOnClickListener {
                itemClickListener.onItemClick(position)
            }

            binding.ivPatentDelete.setOnClickListener {
                itemClickListener.onItemClick(position)
            }

            if (deleteFlag) {
                binding.ivPatentDelete.visibility = View.VISIBLE
            } else {
                binding.ivPatentDelete.visibility = View.GONE
            }
        }
    }
}