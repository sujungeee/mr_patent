package com.ssafy.mr_patent_android.ui.patent

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.base.BaseFragment
import com.ssafy.mr_patent_android.databinding.FragmentSimiliarityTestBinding
import com.ssafy.mr_patent_android.ui.mypage.PatentFolderDetailFragmentDirections
import com.ssafy.mr_patent_android.ui.mypage.PatentFolderDetailViewModel

private const val TAG = "SimiliarityTestFragment_Mr_Patent"
class SimiliarityTestFragment : BaseFragment<FragmentSimiliarityTestBinding>(
    FragmentSimiliarityTestBinding::bind, R.layout.fragment_similiarity_test
) {
    private val similiarityTestViewModel : SimiliarityTestViewModel by activityViewModels()

    private val patentViewModel : PatentViewModel by activityViewModels()
    private val patentFolderDetailViewModel : PatentFolderDetailViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initObserver()
    }

    private fun initView() {
        binding.btnSimiliarityTestConfirm.setOnClickListener {
            // 화면 이동
            if (similiarityTestViewModel.status.value == "finished") {
                findNavController().popBackStack(R.id.nav_fragment_patent, false)
                val bottomNavController = requireActivity().findNavController(R.id.nav_fragment_mypage)
                bottomNavController.navigate(R.id.fragment_patent_folder)
                patentFolderDetailViewModel.setFolderId(patentViewModel.folderId.value!!)
                bottomNavController.navigate(R.id.fragment_patent_folder_detail)
                bottomNavController.navigate(
                    PatentFolderDetailFragmentDirections.actionPatentFolderDetailFragmentToPatentContentFragment(similiarityTestViewModel.patentId.value!!, "select")
                )
            }
            if (similiarityTestViewModel.status.value == "ongoing") {
                findNavController().popBackStack(R.id.nav_fragment_patent, false)
            }
        }
    }

    private fun initObserver() {
        similiarityTestViewModel.status.observe(viewLifecycleOwner) {
            when (it) {
                "finished" -> {
                    binding.ivStatusOngoing.visibility = View.GONE
                    binding.tvStatusOngoing.visibility = View.GONE
                    binding.ivStatusFinished.visibility = View.VISIBLE
                    binding.tvStatusFinished.visibility = View.VISIBLE
                    binding.btnSimiliarityTestConfirm.text = "유사도 분석 결과 확인하기"
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(key: String, value: String) =
            SimiliarityTestFragment().apply {
                arguments = Bundle().apply {
                    putString(key, value)
                }
            }
    }
}