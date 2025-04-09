package com.ssafy.mr_patent_android.ui.patent

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.base.BaseFragment
import com.ssafy.mr_patent_android.databinding.FragmentPatentContentExpBinding
import com.ssafy.mr_patent_android.ui.mypage.ReportResultViewModel

private const val TAG = "PatentContentExpFragment_Mr_Patent"
class PatentContentExpFragment : BaseFragment<FragmentPatentContentExpBinding>(
    FragmentPatentContentExpBinding::bind, R.layout.fragment_patent_content_exp
) {
    private val reportResultViewModel: ReportResultViewModel by activityViewModels()
    lateinit var patentFrameExpAdapter: PatentFrameExpAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
    }

    private fun initView() {
        patentFrameExpAdapter = PatentFrameExpAdapter(
            reportResultViewModel.mode.value!!,
            reportResultViewModel.patentExpContents.value!!
        )

        binding.rvPatentContentExp.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPatentContentExp.adapter = patentFrameExpAdapter
    }

    fun expFillInput(): Boolean {
        return patentFrameExpAdapter.expFillInput()
    }

    companion object {
        @JvmStatic
        fun newInstance(key: String, value: String) =
            PatentContentExpFragment().apply {
                arguments = Bundle().apply {
                    putString(key, value)
                }
            }
    }
}