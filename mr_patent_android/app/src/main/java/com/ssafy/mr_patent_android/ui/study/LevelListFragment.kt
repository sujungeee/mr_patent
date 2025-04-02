package com.ssafy.mr_patent_android.ui.study

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.databinding.FragmentLevelListBinding

private const val TAG = "FragmentLevelListBinding"
class LevelListFragment : Fragment() {
    lateinit var binding: FragmentLevelListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLevelListBinding.inflate(inflater, container, false)
// Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setlevelCard(R.id.card_level1, "Lv.1", "학습완료 |90개", "#EFF4FF")
        setlevelCard(R.id.card_level2, "Lv.2", "학습완료 |90개", "#E7F0FF")
        setlevelCard(R.id.card_level3, "Lv.3", "학습완료 |90개", "#D4E5FF")
        setlevelCard(R.id.card_level4, "Lv.4", "학습완료 |90개", "#C6DBFD")
        setlevelCard(R.id.card_level5, "Lv.5", "", "#A0BFFB", true)
        setlevelCard(R.id.card_dictionary, "단어장", "", "#6D9AF0")

    }

    fun setlevelCard(
        cardId: Int,
        title: String,
        description: String,
        bgColor: String,
        isLocked: Boolean = false
    ) {
        // 여기서 view는 onCreateView에서 인플레이트된 fragment의 루트 뷰입니다.
        val cardView = view?.findViewById<CardView>(cardId)
        Log.d(TAG, "setlevelCard: $cardView")
        // cardView가 null이 아닌지 확인
        cardView?.let {
            val card = it.findViewById<CardView>(cardId)
            val titleTv = it.findViewById<TextView>(R.id.tv_level_title)
            val descTv = it.findViewById<TextView>(R.id.tv_level_desc)
            val iconIv = it.findViewById<ImageView>(R.id.iv_level_icon)
            val arrowIv = it.findViewById<ImageView>(R.id.iv_arrow)
            val icon= it.findViewById<ImageView>(R.id.iv_icon)

            card.setCardBackgroundColor(Color.parseColor(bgColor))

            titleTv.text = title

            if (description.isNotEmpty()) {
                descTv.text = description
                icon?.visibility = View.GONE
            } else {
                descTv.visibility = View.GONE
                arrowIv.visibility = View.GONE
            }

            if (isLocked) {
                iconIv.visibility = View.VISIBLE
            }


        }
    }

}