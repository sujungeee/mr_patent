package com.ssafy.mr_patent_android.ui.mypage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.data.model.response.SimiliarityResultResponse
import com.ssafy.mr_patent_android.databinding.ListItemSimiliarityPatentContentBinding

class ReportSimiliarContentAdapter(var similiarContents: List<SimiliarityResultResponse.Comparison.SimiliarContext>)
    : RecyclerView.Adapter<ReportSimiliarContentAdapter.ReportSimiliarContentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportSimiliarContentViewHolder {
        val binding = ListItemSimiliarityPatentContentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReportSimiliarContentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportSimiliarContentViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = similiarContents.size

    inner class ReportSimiliarContentViewHolder(private val binding: ListItemSimiliarityPatentContentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.tvPatentContentSimiliarity.text = itemView.context.getString(
                R.string.tv_patent_content_similiarity,
                (similiarContents[position].similarityScore * 100).toInt()
            )

            binding.tvPatentContentWrite.text = similiarContents[position].userText
            binding.tvPatentContentRegister.text = similiarContents[position].patentText
        }
    }
}