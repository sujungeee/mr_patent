package com.ssafy.mr_patent_android.ui.mypage

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.data.model.response.SimiliarityResultResponse
import com.ssafy.mr_patent_android.databinding.ListItemSimiliarityPatentBinding

private const val TAG = "ReportResultAdapter_Mr_Patent"
class ReportResultAdapter(var results: List<SimiliarityResultResponse.Comparison>)
    : RecyclerView.Adapter<ReportResultAdapter.ReportResultViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportResultViewHolder {
        val binding = ListItemSimiliarityPatentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReportResultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportResultViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = results.size

    inner class ReportResultViewHolder(private val binding: ListItemSimiliarityPatentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.tvPatentSimiliarity.text = itemView.context.getString(
                R.string.tv_patent_similiarity,
                (results[position].detailedComparisonTotalScore * 100).toInt()
            )

            binding.tvPatentNumExp.text = itemView.context.getString(
                R.string.tv_patent_similiarity_id,
                (results[position].similarityPatentId)
            )

            binding.rvSimiliarContents.layoutManager = LinearLayoutManager(binding.root.context)
            binding.rvSimiliarContents.adapter = ReportSimiliarContentAdapter(results[position].comparisonContexts)
        }
    }
}