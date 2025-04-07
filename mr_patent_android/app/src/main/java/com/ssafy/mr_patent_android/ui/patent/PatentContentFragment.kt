package com.ssafy.mr_patent_android.ui.patent

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.tabs.TabLayoutMediator
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.base.ApplicationClass.Companion.sharedPreferences
import com.ssafy.mr_patent_android.base.BaseFragment
import com.ssafy.mr_patent_android.data.model.dto.PatentDraftDto
import com.ssafy.mr_patent_android.data.model.dto.PatentFrameDto
import com.ssafy.mr_patent_android.data.model.dto.PatentTitleDto
import com.ssafy.mr_patent_android.databinding.FragmentPatentContentBinding
import com.ssafy.mr_patent_android.ui.mypage.ReportResultViewModel
import kotlin.math.exp

private const val TAG = "PatentContentFragment_Mr_Patent"
class PatentContentFragment : BaseFragment<FragmentPatentContentBinding>(
    FragmentPatentContentBinding::bind, R.layout.fragment_patent_content
) {
    private val args: PatentContentFragmentArgs by navArgs()
    private val fileViewModel: FileViewModel by activityViewModels()
    private val patentViewModel : PatentViewModel by activityViewModels()
    private val reportResultViewModel : ReportResultViewModel by activityViewModels()
    private val similiarityTestViewModel : SimiliarityTestViewModel by activityViewModels()

    private lateinit var expFragment: PatentContentExpFragment
    private lateinit var claimFragment: PatentContentClaimFragment
    private lateinit var summaryFragment: PatentContentSummaryFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initObserver()
    }

    private fun initView() {
        reportResultViewModel.setMode(args.mode)
        when (args.mode) {
            "select" -> {
                binding.btnSimiliarityTest.visibility = View.GONE
                binding.clReportItems.visibility = View.VISIBLE
                reportResultViewModel.setUserPatentId(args.id)
            }
            "update" -> {
                binding.btnSimiliarityTest.visibility = View.VISIBLE
                binding.clReportItems.visibility = View.GONE
                reportResultViewModel.setUserPatentId(args.id)
            }
            "write" -> {
                binding.btnSimiliarityTest.visibility = View.VISIBLE
                binding.clReportItems.visibility = View.GONE
            }
            "upload" -> {
                binding.btnSimiliarityTest.visibility = View.VISIBLE
                binding.clReportItems.visibility = View.GONE
            }
        }

        // TODO: delete
        splitContent()

        initViewPager()

        binding.tvBefore.setOnClickListener {
            when (args.mode) {
                "select" -> findNavController().popBackStack()
                "update" -> findNavController().popBackStack()
                "write" -> findNavController().popBackStack()
                "upload" -> {
                    fileViewModel.setUploadState(false)
                    findNavController().popBackStack(R.id.fileUploadFragment, false)
                }
            }
        }

        binding.btnReportConfirm.setOnClickListener {
            findNavController().navigate(R.id.reportResultFragment)
        }

        binding.btnFileExtraction.setOnClickListener {
            setDialogFileExtraction()
        }

        binding.btnSimiliarityTest.setOnClickListener {
            if (isFillInput()) {
                val expAdapter = expFragment.binding.rvPatentContentExp
                similiarityTestViewModel.addDraft(patentViewModel.folderId.value!!,
                    PatentDraftDto(
                        expAdapter[0].findViewById<EditText>(R.id.et_spec_content).text.toString()
                        , expAdapter[1].findViewById<EditText>(R.id.et_spec_content).text.toString()
                        , expAdapter[2].findViewById<EditText>(R.id.et_spec_content).text.toString()
                        , expAdapter[3].findViewById<EditText>(R.id.et_spec_content).text.toString()
                        , expAdapter[4].findViewById<EditText>(R.id.et_spec_content).text.toString()
                        , expAdapter[5].findViewById<EditText>(R.id.et_spec_content).text.toString()
                        , expAdapter[6].findViewById<EditText>(R.id.et_spec_content).text.toString()
                        , claimFragment.getPatentClaimContents()
                        , summaryFragment.getPatentSummaryContents()
                    )
                )
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when (args.mode) {
                    "upload" -> {
                        fileViewModel.setUploadState(false)
                        findNavController().popBackStack(R.id.fileUploadFragment, false)
                    }
                    "write" -> {
                        findNavController().popBackStack()
                    }
                    "update" -> {
                        findNavController().popBackStack()
                    }
                    "select" -> {
                        findNavController().popBackStack()
                    }
                }
            }
        })
    }

    private fun initObserver() {
        reportResultViewModel.userPatentId.observe(viewLifecycleOwner, {
            reportResultViewModel.getPatentContent(it)
        })

        reportResultViewModel.patentContent.observe(viewLifecycleOwner, { // select, update
            if (args.mode == "select") {
                binding.tvDraftWrite.text =  it.patentDraftTitle
            }
            // 파일 업로드 시
//            splitContent(it)
        })

        similiarityTestViewModel.addState.observe(viewLifecycleOwner, {
            if (it) {
                similiarityTestViewModel.similiaritytest(similiarityTestViewModel.patentId.value!!)
                similiarityTestViewModel.setStatus("ongoing")
                findNavController().navigate(R.id.similiarityTestFragment)
            }
        })
    }

    // TODO: add
//    private fun splitContent(patentContentResponse: PatentContentResponse) {
    // TODO: delete
    private fun splitContent() {
        val patentTitleExp = PatentTitleDto.PatentTitleExpDto()
        reportResultViewModel.setPatentExpContents(listOf(
//            PatentFrameDto(patentTitleExp.patentDraftTitleExp, patentContentResponse.patentDraftTitle)
//            , PatentFrameDto(patentTitleExp.patentDraftTechnicalFieldExp, patentContentResponse.patentDraftTechnicalField)
//            , PatentFrameDto(patentTitleExp.patentDraftBackgroundExp, patentContentResponse.patentDraftBackground)
//            , PatentFrameDto(patentTitleExp.patentDraftProblemExp, patentContentResponse.patentDraftProblem)
//            , PatentFrameDto(patentTitleExp.patentDraftSolutionExp, patentContentResponse.patentDraftSolution)
//            , PatentFrameDto(patentTitleExp.patentDraftEffectExp, patentContentResponse.patentDraftEffect)
//            , PatentFrameDto(patentTitleExp.patentDraftDetailedExp, patentContentResponse.patentDraftDetailed)
            PatentFrameDto(patentTitleExp.patentDraftTitleExp, "발명의 명칭ㅋ")
            , PatentFrameDto(patentTitleExp.patentDraftTechnicalFieldExp, "본 발명은 신발 내부에...")
            , PatentFrameDto(patentTitleExp.patentDraftBackgroundExp, "종래의 기술에서는...")
            , PatentFrameDto(patentTitleExp.patentDraftProblemExp, "해결하려는 과제는...")
            , PatentFrameDto(patentTitleExp.patentDraftSolutionExp, "과제의 해결 수단은...")
            , PatentFrameDto(patentTitleExp.patentDraftEffectExp, "발명의 효과는...")
            , PatentFrameDto(patentTitleExp.patentDraftDetailedExp, "발명을 실시하기 위한 구체적인 내용...")
        ))

        val patentTitleClaim = PatentTitleDto.PatentTitleClaimDto()
        reportResultViewModel.setPatentClaimContents(listOf(
//            PatentFrameDto(patentTitleClaim.patentDraftClaim, patentContentResponse.patentDraftClaim)
            PatentFrameDto(patentTitleClaim.patentDraftClaim, "[청구항 1] 이 시트는...")
        ))

        val patentTitleSummary = PatentTitleDto.PatentTitleSummaryDto()
        reportResultViewModel.setPatentSummaryContents(listOf(
//            PatentFrameDto(patentTitleSummary.patentDraftSummary, patentContentResponse.patentDraftSummary)
//            , PatentFrameDto(patentTitleSummary.patentDraftFloorPlan, patentContentResponse.patentDraftFloorPlan)
            PatentFrameDto(patentTitleSummary.patentDraftSummary, "요약 ....")
        ))
    }

    private fun initViewPager() {
        val vpContents = binding.vpContents
        val tlContentsClassification = binding.tlContentsClassificaton

        val fragmentList = ArrayList<Fragment>()
        expFragment = PatentContentExpFragment()
        claimFragment = PatentContentClaimFragment()
        summaryFragment = PatentContentSummaryFragment()

        fragmentList.add(expFragment)
        fragmentList.add(claimFragment)
        fragmentList.add(summaryFragment)

        vpContents.offscreenPageLimit = 3
        vpContents.adapter = ViewContentAdapter(fragmentList, requireActivity() as AppCompatActivity)
        val tabArray = arrayOf("발명의 설명", "청구범위", "요약서")
        TabLayoutMediator(tlContentsClassification, vpContents) { tab, position ->
            tab.text = tabArray[position]
        }.attach()
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

        tvPdf.setOnClickListener {
            fileViewModel.setExtractionType("pdf")
            tvPdf.setBackgroundResource(R.drawable.rounded_background_stroke_active)
        }

        btnExtraction.setOnClickListener {
            dialogBuilder.dismiss()
            setDialogFileComplete()
            // 파일 추출 api 삽입
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

    fun isFillInput() : Boolean {
        val result = expFragment.expFillInput()
                && summaryFragment.summaryFillInput()
                && claimFragment.claimFillInput()

        if (result) {
            return true
        } else {
            showCustomToast("빈 칸을 입력해주세요.")
            return false
        }
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