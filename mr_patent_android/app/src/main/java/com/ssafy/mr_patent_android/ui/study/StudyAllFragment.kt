package com.ssafy.mr_patent_android.ui.study

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.base.BaseFragment
import com.ssafy.mr_patent_android.databinding.FragmentStudyAllBinding

class StudyAllFragment : BaseFragment<FragmentStudyAllBinding>(FragmentStudyAllBinding::bind, R.layout.fragment_study_all) {
    val viewModel: StudyCardViewModel by viewModels()
    val level_id by lazy {
        navArgs<StudyAllFragmentArgs>().value.levelId }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getWordList(level_id)

        initObserver()
    }

    fun initObserver(){
        viewModel.wordList.observe(viewLifecycleOwner, {
            binding.rvStudyAll.adapter = WordAllAdapter(it){

            }
        })
    }

}