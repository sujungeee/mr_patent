package com.ssafy.mr_patent_android.ui.patent

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.base.BaseFragment
import com.ssafy.mr_patent_android.databinding.FragmentFileUploadBinding
import com.ssafy.mr_patent_android.ui.mypage.ReportResultViewModel
import com.ssafy.mr_patent_android.util.FilePicker
import com.ssafy.mr_patent_android.util.FileUtil
import java.io.File

private const val TAG = "FileUploadFragment_Mr_Patent"
class FileUploadFragment : BaseFragment<FragmentFileUploadBinding>(
    FragmentFileUploadBinding::bind, R.layout.fragment_file_upload
) {
    private val fileViewModel : FileViewModel by activityViewModels()

    private lateinit var filePickerUtil: FilePicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initObserver()
    }

    private fun initView() {
        filePickerUtil = FilePicker(this) { uri, fileName, fileSize ->
            if(fileSize >= 1024 * 1024 * 5) {
                setDialogSizeOver()
                return@FilePicker
            } else {
                // TODO: edit: file extraction type
                val mode = "pdf"

                when (mode) {
                    "pdf" -> {
                        binding.ivPdf.visibility = View.VISIBLE
                    }
                    "word" -> {
                        binding.ivWord.visibility = View.VISIBLE
                    }
                }
                binding.ivFileUpload.visibility = View.INVISIBLE
                binding.tvFileName.visibility = View.VISIBLE
                binding.tvFileSize.visibility = View.VISIBLE
                binding.tvFileName.text = fileName
                binding.tvFileSize.text = FileUtil().formatFileSize(fileSize)
                fileViewModel.setFileUri(uri.toString())
            }
        }

        binding.tvBefore.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.ivFileUpload.setOnClickListener {
            filePickerUtil.checkPermissionAndOpenStorage()
        }

        binding.tvFileUpload.setOnClickListener {
            filePickerUtil.checkPermissionAndOpenStorage()
        }

        binding.btnSpecConfirm.setOnClickListener {
            if (fileViewModel.fileUri.value != null) {
                // OCR API 호출
                fileViewModel.getOcrContent()
                // TODO: delete
                fileViewModel.setUploadState(true)
            } else {
                showCustomToast("명세서 파일을 등록해주세요!")
            }
        }
    }

    private fun initObserver() {
        fileViewModel.uploadState.observe(viewLifecycleOwner) {
            if (it) {
                // TODO: add
//                reportResultViewModel.setPatentContent(fileViewModel.patentContent.value!!)
                findNavController().navigate(FileUploadFragmentDirections.actionFileUploadFragmentToPatentContentFragment(-1, "upload"))
            }
        }
    }

    private fun setDialogSizeOver() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_size_over, null)
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

        val dlBtnYes = dialogView.findViewById<Button>(R.id.dl_btn_yes)

        dlBtnYes.setOnClickListener {
            dialogBuilder.dismiss()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(key: String, value: String) =
            FileUploadFragment().apply {
                arguments = Bundle().apply {
                    putString(key, value)
                }
            }
    }
}