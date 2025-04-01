package com.ssafy.mr_patent_android.ui.mypage

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.base.BaseFragment
import com.ssafy.mr_patent_android.data.model.response.PatentListResponse
import com.ssafy.mr_patent_android.databinding.FragmentPatentFolderDetailBinding
import com.ssafy.mr_patent_android.ui.patent.FolderAdapter

private const val TAG = "PatentFolderDetailFragment_Mr_Patent"
class PatentFolderDetailFragment : BaseFragment<FragmentPatentFolderDetailBinding>(
    FragmentPatentFolderDetailBinding::bind, R.layout.fragment_patent_folder_detail
) {
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
        patentFolderDetailViewModel.getPatentList(patentFolderDetailViewModel.folderId.value!!)
        // TODO: delete
        val tmp = mutableListOf<PatentListResponse.PatentSummaryInfo>()
        tmp.add(PatentListResponse.PatentSummaryInfo(
            1, "더블버블빨대", "적합", 48, "2023-03-01"
        ))
        tmp.add(PatentListResponse.PatentSummaryInfo(
            2, "더블버블빨대", "부분 적합", 70, "2023-03-01"
        ))
        patentFolderDetailViewModel.setPatents(tmp)

        binding.tvBefore.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnPatentDelete.setOnClickListener {
            patentFolderDetailViewModel.setDeleteFlag(true)
            binding.rvFolderItems.adapter = PatentFolderDetailAdapter(true, patentFolderDetailViewModel.patents.value!!) { position ->
                setDialogFolderDetailDelete()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (patentFolderDetailViewModel.deleteFlag.value == true ) {
                    findNavController().popBackStack()
                    findNavController().navigate(R.id.patentFolderDetailFragment)
                    patentFolderDetailViewModel.setDeleteFlag(false)
                } else {
                    findNavController().popBackStack()
                }
            }
        })
    }

    private fun initObserver() {
        patentFolderDetailViewModel.patents.observe(viewLifecycleOwner) {
            binding.rvFolderItems.layoutManager = LinearLayoutManager(requireContext())
            binding.rvFolderItems.adapter = PatentFolderDetailAdapter(false, it) { position ->
                patentFolderDetailViewModel.setPatentId(it[position].patentDraftId)
                findNavController().navigate(
                    PatentFolderDetailFragmentDirections.actionPatentFolderDetailFragmentToPatentContentFragment(it[position].patentDraftId, "select")
                )
            }
        }
    }

    private fun setDialogFolderDetailDelete() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_patent_delete, null)
        val dialogBuilder = Dialog(requireContext())
        dialogBuilder.setContentView(dialogView)
        dialogBuilder.create()
        dialogBuilder.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(
                ((context.resources.displayMetrics.widthPixels) * 0.6).toInt(),
                ((context.resources.displayMetrics.heightPixels) * 0.14).toInt()
            )
        }
        dialogBuilder.show()

        val btnFolderDeleteConfirm = dialogView.findViewById<Button>(R.id.btn_patent_delete_confirm)
        val btnFolderDeleteCancel = dialogView.findViewById<Button>(R.id.btn_patent_delete_cancel)

        btnFolderDeleteConfirm.setOnClickListener {
            // TODO: delete

            showCustomToast("폴더가 삭제되었습니다.")
            dialogBuilder.dismiss()
        }

        btnFolderDeleteCancel.setOnClickListener {
            dialogBuilder.dismiss()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(key: String, value: String) =
            PatentFolderDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(key, value)
                }
            }
    }
}