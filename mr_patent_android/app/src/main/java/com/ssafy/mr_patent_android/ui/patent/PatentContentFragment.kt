package com.ssafy.mr_patent_android.ui.patent

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.base.ApplicationClass.Companion.sharedPreferences
import com.ssafy.mr_patent_android.base.BaseFragment
import com.ssafy.mr_patent_android.databinding.FragmentPatentContentBinding
import com.ssafy.mr_patent_android.ui.mypage.ReportResultViewModel

private const val TAG = "PatentContentFragment_Mr_Patent"
class PatentContentFragment : BaseFragment<FragmentPatentContentBinding>(
    FragmentPatentContentBinding::bind, R.layout.fragment_patent_content
) {
    private val args: PatentContentFragmentArgs by navArgs()
    private val fileViewModel: FileViewModel by activityViewModels()
    private val reportResultViewModel : ReportResultViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initObserver()
    }

    fun initView() {
        when (args.mode) {
            "select" -> {
                binding.btnSimiliarityTest.visibility = View.GONE
                binding.clReportItems.visibility = View.VISIBLE
            }
            "edit" -> {
                binding.btnSimiliarityTest.visibility = View.VISIBLE
                binding.clReportItems.visibility = View.GONE
            }

        }
        reportResultViewModel.setUserPatentId(args.id)

        binding.tvBefore.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnReportConfirm.setOnClickListener {
            findNavController().navigate(R.id.reportResultFragment)
        }

        binding.btnFileExtraction.setOnClickListener {
            setDialogFileExtraction()
        }
    }

    fun initObserver() {

    }

    private fun setDialogFileExtraction() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_file_extraction, null)
        val dialogBuilder = Dialog(requireContext())
        dialogBuilder.setContentView(dialogView)
        dialogBuilder.create()
        dialogBuilder.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(
                ((context.resources.displayMetrics.widthPixels) * 0.8).toInt(),
                ((context.resources.displayMetrics.heightPixels) * 0.3).toInt()
            )
        }
        dialogBuilder.show()

        val btnExtraction = dialogView.findViewById<Button>(R.id.btn_extraction)
        val tvPdf = dialogView.findViewById<TextView>(R.id.tv_pdf)
        val tvWord = dialogView.findViewById<TextView>(R.id.tv_word)

        tvPdf.setOnClickListener {
            fileViewModel.setExtractionType("pdf")
            tvPdf.setBackgroundResource(R.drawable.rounded_background_stroke_active)
            tvWord.setBackgroundResource(R.drawable.rounded_background_stroke)
        }

        tvWord.setOnClickListener {
            fileViewModel.setExtractionType("word")
            tvWord.setBackgroundResource(R.drawable.rounded_background_stroke_active)
            tvPdf.setBackgroundResource(R.drawable.rounded_background_stroke)
        }

        btnExtraction.setOnClickListener {
            if (fileViewModel.extractionType.value == null || fileViewModel.extractionType.value == "") {
                showCustomToast("파일 형식을 선택하세요!")
            } else {
                // TODO: 파일 다운로드

                dialogBuilder.dismiss()
                setDialogFileComplete()
            }
        }
    }

    private fun setDialogFileComplete() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_file_complete, null)
        val dialogBuilder = Dialog(requireContext())
        dialogBuilder.setContentView(dialogView)
        dialogBuilder.create()
        dialogBuilder.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(
                ((context.resources.displayMetrics.widthPixels) * 0.8).toInt(),
                ((context.resources.displayMetrics.heightPixels) * 0.2).toInt()
            )
        }

        fileViewModel.setExtractionType("")

        val tvExpertConsult = dialogView.findViewById<TextView>(R.id.tv_expert_consult)
        val btnListExpertGo = dialogView.findViewById<Button>(R.id.btn_list_expert_go)

        if (sharedPreferences.getUser().userRole == 1) {
            tvExpertConsult.text= "파일 내용을 확인해주세요."
            btnListExpertGo.text = "확인"
        }

        btnListExpertGo.setOnClickListener {
            dialogBuilder.dismiss()
            if (sharedPreferences.getUser().userRole == 0) {
                findNavController().navigate(R.id.patentAttorneyListFragment)
            }
        }

        dialogBuilder.show()
    }

    companion object {
        @JvmStatic
        fun newInstance(key: String, value: String) =
            PatentContentFragment().apply {
                arguments = Bundle().apply {
                    putString(key, value)
                }
            }
    }
}