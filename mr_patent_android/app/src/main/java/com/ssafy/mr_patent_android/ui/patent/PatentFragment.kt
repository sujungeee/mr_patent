package com.ssafy.mr_patent_android.ui.patent

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.annotations.SerializedName
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.base.BaseFragment
import com.ssafy.mr_patent_android.data.model.dto.FolderDto
import com.ssafy.mr_patent_android.data.model.response.PatentRecentResponse
import com.ssafy.mr_patent_android.databinding.FragmentPatentBinding

private const val TAG = "PatentFragment_Mr_Patent"
class PatentFragment : BaseFragment<FragmentPatentBinding>(
    FragmentPatentBinding::bind, R.layout.fragment_patent
) {

    private val patentViewModel: PatentViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initObserver()
        // TODO: delete
        initAdapter()
    }

    // TODO: delete
    private fun initAdapter() {
        val tmp = mutableListOf(
            PatentRecentResponse.PatentDraft(1, "더블버블빨대", "본 발명은 더블버블빨대에 관한 것으로, 보다 상세하게는 상부와 하부로 배치되는 더블더블을 적층하도록 하여 공간 활용도를 높임과 동시에, 본 발명은 더블버블빨대에 관한 것으로, 보다 상세하게는 상부와 하부로 배치되는 더블더블을 적층하도록 하여 높임과···", "2024-03-28"),
            PatentRecentResponse.PatentDraft(2, "다이아몬드빨대", "본 발명은 더블버블빨대에 관한 것으로, 보다 상세하게는 상부와 하부로 배치되는 더블더블을 적층하도록 하여 공간 활용도를 높임과 동시에, 본 발명은 더블버블빨대에 관한 것으로, 보다 상세하게는 상부와 하부로 배치되는 더블더블을 적층하도록 하여 높임과···", "2024-03-28"),
        )
        binding.rvRecentDrafts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecentDrafts.adapter = PatentRecentAdapter(tmp) { position ->
            patentViewModel.setDraftType("update")
            patentViewModel.setPatentDraftId(tmp[position].parentDraftId)
            findNavController().navigate(R.id.patentFolderChoiceFragment)
        }
    }

    private fun initView() {
        patentViewModel.getRecentPatentList()

        binding.clFileUpload.setOnClickListener {
            patentViewModel.setDraftType("upload")
            findNavController().navigate(R.id.patentFolderChoiceFragment)
        }

        binding.clWrite.setOnClickListener {
            patentViewModel.setDraftType("write")
            findNavController().navigate(R.id.patentFolderChoiceFragment)
        }
    }

    private fun initObserver() {
        patentViewModel.patentsRecent.observe(viewLifecycleOwner) {
            binding.rvRecentDrafts.layoutManager = LinearLayoutManager(requireContext())
            binding.rvRecentDrafts.adapter = PatentRecentAdapter(it) { position ->
                patentViewModel.setDraftType("Update")
                patentViewModel.setPatentDraftId(it[position].parentDraftId)
                findNavController().navigate(R.id.patentFolderChoiceFragment)
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(key: String, value: String) =
            PatentFragment().apply {
                arguments = Bundle().apply {
                    putString(key, value)
                }
            }
    }
}