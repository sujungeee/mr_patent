package com.ssafy.mr_patent_android.ui.study

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.base.BaseFragment
import com.ssafy.mr_patent_android.databinding.FragmentStudyCardBinding

private const val TAG = "StudyCardFragment"
class StudyCardFragment : BaseFragment<FragmentStudyCardBinding>(FragmentStudyCardBinding::bind, R.layout.fragment_study_card) {
    val viewModel: StudyCardViewModel by viewModels()
    val level_id by lazy {
        navArgs<StudyCardFragmentArgs>().value.levelId }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initObserver()


    }

    fun initView(){
        viewModel.getWordList(level_id)
        binding.btnAll.setOnClickListener {
            findNavController().navigate(StudyCardFragmentDirections.actionStudyCardFragmentToStudyAllFragment(level_id))
        }
        binding.tvSequence.text = "${binding.vpStudyCard.currentItem+1}/30"
        binding.vpStudyCard.setPageTransformer { page, position ->
            binding.tvSequence.text = "${binding.vpStudyCard.currentItem+1}/30"
        }

        
        binding.btnNext.setOnClickListener {
            binding.vpStudyCard.setCurrentItem(binding.vpStudyCard.currentItem+1, true)
        }

        binding.btnBack.setOnClickListener {
            binding.vpStudyCard.setCurrentItem(binding.vpStudyCard.currentItem-1, true)
        }

        binding.vpStudyCard.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                outRect.right = 48
                outRect.left = 48
            }
        })
    }

    fun initObserver(){
        viewModel.wordList.observe(viewLifecycleOwner){
            it?.let {
                binding.vpStudyCard.adapter = WordCardAdapter(it){
                        // 북마크 클릭
                }
            }
        }
    }
}