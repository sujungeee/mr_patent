package com.ssafy.mr_patent_android.ui.patent

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.base.BaseFragment
import com.ssafy.mr_patent_android.data.model.dto.FolderDto
import com.ssafy.mr_patent_android.databinding.FragmentPatentFolderChoiceBinding

private const val TAG = "PatentFolderChoiceFragment_Mr_Patent"
class PatentFolderChoiceFragment : BaseFragment<FragmentPatentFolderChoiceBinding>(
    FragmentPatentFolderChoiceBinding::bind, R.layout.fragment_patent_folder_choice
) {
    private val patentViewModel : PatentViewModel by activityViewModels()

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
            FolderDto.Folder(1, "폴더 1", "2024-03-28"),
            FolderDto.Folder(2, "폴더 2", "2024-03-27"),
            FolderDto.Folder(3, "폴더 3", "2024-03-26")
        )
        binding.rvFolderItems.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFolderItems.adapter = FolderAdapter(tmp) { position ->
            patentViewModel.setFolderId(tmp[position].userPatentFolderId)
            when(patentViewModel.draftType.value) {
                "FileUpload" -> findNavController().navigate(R.id.fileUploadFragment)
                "Write" -> findNavController().navigate(R.id.patentContentFragment)
                "Update" -> findNavController().navigate(R.id.patentContentFragment)
            }
        }
    }

    private fun initView() {
        patentViewModel.getFolderList()

        binding.tvBefore.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.ivFolderAdd.setOnClickListener {
            setDialogFolderAdd()
        }
    }

    private fun initObserver() {
        patentViewModel.folders.observe(viewLifecycleOwner) {
            binding.rvFolderItems.layoutManager = LinearLayoutManager(requireContext())
            binding.rvFolderItems.adapter = FolderAdapter(it) { position ->
                patentViewModel.setFolderId(it[position].userPatentFolderId)
                when(patentViewModel.draftType.value) {
                    "FileUpload" -> findNavController().navigate(R.id.fileUploadFragment)
                    "Write" -> findNavController().navigate(R.id.patentContentFragment)
                    "Update" -> findNavController().navigate(R.id.patentContentFragment)
                }
            }
        }
    }

    private fun setDialogFolderAdd() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_folder_add, null)
        val dialogBuilder = Dialog(requireContext())
        dialogBuilder.setContentView(dialogView)
        dialogBuilder.create()
        dialogBuilder.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(
                ((context.resources.displayMetrics.widthPixels) * 0.8).toInt(),
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        }
        dialogBuilder.show()

        val btnFolderAdd = dialogView.findViewById<Button>(R.id.btn_folder_add)
        val etFolderName = dialogView.findViewById<EditText>(R.id.et_folder_name)

        btnFolderAdd.setOnClickListener {
            patentViewModel.addFolder(etFolderName.text.toString())
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(key: String, value: String) =
            PatentFolderChoiceFragment().apply {
                arguments = Bundle().apply {
                    putString(key, value)
                }
            }
    }
}