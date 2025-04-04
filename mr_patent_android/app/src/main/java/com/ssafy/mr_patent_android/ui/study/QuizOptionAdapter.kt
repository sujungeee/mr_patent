package com.ssafy.mr_patent_android.ui.study

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.data.model.dto.Question
import com.ssafy.mr_patent_android.databinding.ItemQuizOptionBinding

class QuizOptionAdapter(
    private var options: List<Question.Option>,
    private val correctOptionId: Int,
    private val wordId: Int,
    private val viewModel: QuizViewModel,
    val itemClickListener:ItemClickListener
) : RecyclerView.Adapter<QuizOptionAdapter.OptionViewHolder>() {

    private var selectedOption: Int? = null
    private var isAnswerChecked = false

    inner class OptionViewHolder(private val binding: ItemQuizOptionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(option: Question.Option) {
            binding.tvOptionText.text = option.optionText

            binding.tvOptionText.setOnClickListener {
                if (!isAnswerChecked) {
                isAnswerChecked = true
                itemClickListener.onItemClick(option.optionId)
                binding.tvOptionText.setBackgroundResource(R.drawable.selector_quiz_option)
                binding.tvOptionText.setBackgroundResource(
                    when {
                        option.optionId == correctOptionId -> R.drawable.quiz_option_correct
                        else -> R.drawable.quiz_option_wrong
                    }
                )
//                    notifyDataSetChanged()
            }
            }
        }
    }

    fun interface ItemClickListener {
        fun onItemClick(id: Int)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionViewHolder {
        val binding = ItemQuizOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OptionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {
        holder.bind(options[position])
    }

    override fun getItemCount(): Int = options.size
}
