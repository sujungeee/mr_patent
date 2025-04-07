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
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.base.BaseFragment
import com.ssafy.mr_patent_android.databinding.FragmentStudyCardBinding

private const val TAG = "StudyCardFragment"
class StudyCardFragment : BaseFragment<FragmentStudyCardBinding>(FragmentStudyCardBinding::bind, R.layout.fragment_study_card) {
    val viewModel: StudyCardViewModel by viewModels()
    lateinit var wordCardAdapter: WordCardAdapter
    val level_id by lazy {
        navArgs<StudyCardFragmentArgs>().value.levelId }
    val type by lazy {
        navArgs<StudyCardFragmentArgs>().value.type
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initObserver()


    }

    fun initView(){
        if (type == "bookmark") {
            binding.tvTitle.text = "북마크 단어장"
            viewModel.getBookmarkWordList(level_id)
            binding.tvSequence.text = "${binding.vpStudyCard.currentItem+1}/${viewModel.total.value ?: ""}"
            binding.vpStudyCard.setPageTransformer { page, position ->
                binding.tvSequence.text = "${binding.vpStudyCard.currentItem+1}/${viewModel.total.value?: ""}"
            }
        } else {
            viewModel.getWordList(level_id)
            binding.tvSequence.text = "${binding.vpStudyCard.currentItem+1}/30"
            binding.vpStudyCard.setPageTransformer { page, position ->
                binding.tvSequence.text = "${binding.vpStudyCard.currentItem+1}/30"
            }
        }
        binding.btnAll.setOnClickListener {
            findNavController().navigate(StudyCardFragmentDirections.actionStudyCardFragmentToStudyAllFragment(level_id, type),
            NavOptions.Builder()
                .setPopUpTo(R.id.studyCardFragment, true)
                .build())
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
        viewModel.wordList.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                binding.tvSequence.text = "0/0"
                return@observe
            }
            it?.let { list ->
                if (!::wordCardAdapter.isInitialized) {
                    wordCardAdapter = WordCardAdapter(list.toMutableList()) { position ->
                        if (viewModel.isLoading.value == true) return@WordCardAdapter false

                        val result = viewModel.createBookmark(position)
                        val updatedWord = viewModel.wordList.value?.get(position)

                        if (result && updatedWord != null) {
                            wordCardAdapter.updateBookmarkState(position, updatedWord)
                        }

                        return@WordCardAdapter result
                    }
                    binding.vpStudyCard.adapter = wordCardAdapter
                } else {
                    // 최초 이후엔 데이터 갱신만 필요하면 아래처럼 처리할 수도 있어요
//                    wordCardAdapter.submitList(list.toMutableList()) // 필요 시 함수 직접 만들어도 OK
                }
            }
        }

    }
}