package com.ssafy.mr_patent_android.ui.join

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.activity.addCallback
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.base.BaseFragment
import com.ssafy.mr_patent_android.data.model.dto.JoinExpertRequest
import com.ssafy.mr_patent_android.databinding.FragmentJoinExpertBinding
import com.ssafy.mr_patent_android.ui.address.AddressViewModel
import com.ssafy.mr_patent_android.ui.login.EmailVerifyViewModel
import com.ssafy.mr_patent_android.util.FilePicker
import com.ssafy.mr_patent_android.util.FileUtil
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "JoinExpertFragment_Mr_Patent"
class JoinExpertFragment : BaseFragment<FragmentJoinExpertBinding>(
    FragmentJoinExpertBinding::bind, R.layout.fragment_join_expert
) {
    private val joinViewModel: JoinViewModel by activityViewModels()
    private val emailVerifyViewModel: EmailVerifyViewModel by activityViewModels()
    private val addressViewModel : AddressViewModel by activityViewModels()

    private lateinit var filePickerUtil: FilePicker

    private lateinit var license : String
    private lateinit var identification : String
    private lateinit var address : String
    private lateinit var categories : MutableList<JoinExpertRequest.Category>

    private var emailFlag : Boolean = false

    private lateinit var timer : CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initObserver()
    }

    private fun initView() {
        filePickerUtil = FilePicker(this) { uri ->
            val fileName = FileUtil().getFileName(requireContext(), uri)
            val fileSize = FileUtil().getFileSize(requireContext(), uri)
            val fileExtension = FileUtil().getFileExtension(requireContext(), uri)
            if (fileExtension != "pdf") {
                showCustomToast("PDF 파일만 업로드 가능합니다.")
                return@FilePicker
            } else if(fileSize >= 1024 * 1024 * 5) {
                setDialogSizeOver()
                return@FilePicker
            } else {
                joinViewModel.setFile(uri.toString())
                binding.ivLicenseUpload.visibility = View.GONE
                binding.ivPdf.visibility = View.VISIBLE
                binding.tvFileName.visibility = View.VISIBLE
                binding.tvFileSize.visibility = View.VISIBLE
                binding.tvFileName.text = fileName
                binding.tvFileSize.text = FileUtil().formatFileSize(fileSize)
                license = uri.toString()
            }
        }

        binding.tvBefore.setOnClickListener {
            if (joinViewModel.userImage.value == "") {
                joinViewModel.setUserImage(requireContext().resources.getString(R.string.default_image))
            }
            findNavController().popBackStack()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (joinViewModel.userImage.value == "") {
                joinViewModel.setUserImage(requireContext().resources.getString(R.string.default_image))
            }
            findNavController().popBackStack()
        }

        binding.ivLicenseUpload.setOnClickListener {
            filePickerUtil.checkPermissionAndOpenStorage()
        }

        binding.tvLicenseUpload.setOnClickListener {
            filePickerUtil.checkPermissionAndOpenStorage()
        }

        binding.btnJoinApply.setOnClickListener {
            if (emailFlag
                && isFillInput()
                && isValidName(binding.etName.text.toString())
                && isValidPw(binding.etPw.text.toString())
                && isValidPhone(binding.etPhone.text.toString())
                && isValidDescription(binding.etDescription.text.toString())
            ) {
                identification = binding.etIdentificationFront.text.toString() + "-" + binding.etIdentificationBack.text.toString()
                address = binding.etAddress1.text.toString() + "\\" + binding.etAddress2.text.toString()
                for (i in 0 until binding.cgFilter.childCount) {
                    val chip = binding.cgFilter.getChildAt(i) as Chip
                    if (chip.isChecked) {
                        val category = JoinExpertRequest.Category(chip.text.toString())
                        categories.add(category)
                    }
                }

                if (joinViewModel.userImage.value != null) {
                    // TODO: 확장자 추출

                    lifecycleScope.launch {
                        joinViewModel.uploadFile(joinViewModel.userImage.value!!, "image/jpeg")
                    }
                } else {
                    joinViewModel.joinExpert(
                        binding.etEmail.text.toString()
                        , binding.etPw.text.toString()
                        , binding.etName.text.toString()
                        ,""
                        , identification
                        , binding.etDescription.text.toString()
                        , address
                        , binding.etPhone.text.toString()
                        , license
                        , binding.tvDateChoice.text.toString()
                        , categories
                    )
                }
            }
        }

        binding.btnEmailDupl.setOnClickListener {
            if (binding.etEmail.text.toString()
                    .isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(binding.etEmail.text.toString())
                    .matches()
            ) {
                showCustomToast("이메일을 확인해주세요.")
            } else {
                joinViewModel.checkDuplEmail(binding.etEmail.text.toString())
            }
        }

        binding.btnVerificationSend.setOnClickListener {
            emailVerifyViewModel.sendCode(binding.etEmail.text.toString())
        }

        binding.btnVerify.setOnClickListener {
            emailVerifyViewModel.emailVerify(binding.etEmail.text.toString(), binding.etCode.text.toString())
        }

        binding.cgFilter.setOnCheckedStateChangeListener { group, index ->
            if (index.isEmpty()) {
                group.check(group.tag as? Int ?: R.id.chip_chemi)
            } else {
                group.tag = index.first()
            }
        }

        binding.ivSearch.setOnClickListener {
            findNavController().navigate(R.id.nav_addressSearchFragment)
        }

        binding.tvDateChoice.setOnClickListener {
            showDatePickerDialog()
        }

        binding.ivDateChoiceCancel.setOnClickListener {
            binding.tvDateChoice.text = "날짜를 선택하세요."
        }
    }

    private fun initObserver() {
        joinViewModel.checkDuplEmail.observe(viewLifecycleOwner, {
            if (it == true) {
                binding.tvIdDupl.visibility = View.VISIBLE
                binding.clVerify.visibility = View.GONE
            } else if (it == false){
                showCustomToast("사용 가능한 이메일입니다.")
                binding.tvIdDupl.visibility = View.GONE
                binding.clVerify.visibility = View.VISIBLE
            }
        })

        emailVerifyViewModel.codeState.observe(viewLifecycleOwner, {
            if (it) {
                // 코드 전송 성공
                startTimer()
                binding.btnVerificationSend.isEnabled = false
                binding.etCode.isEnabled = true
                showCustomToast("코드 전송 성공")

                binding.etCode.addTextChangedListener {
                    val isEnable = binding.etCode.text.toString().length == 6
                    // 코드 입력
                    binding.btnVerify.isEnabled = isEnable
                }
            } else {
                // 시간 끝
                binding.btnVerificationSend.isEnabled = true
                binding.etCode.isEnabled = false
                binding.btnVerify.isEnabled = false
            }
        })

        emailVerifyViewModel.emailVerifyState.observe(viewLifecycleOwner, {
            if (it) {
                showCustomToast("이메일 인증 성공")
                emailFlag = true
                binding.btnVerify.isEnabled = false
                stopTimer()
            } else {
                showCustomToast("이메일 인증 실패")
            }
        })

        addressViewModel.address.observe(viewLifecycleOwner) {
            binding.etAddress1.setText(it)
        }

        joinViewModel.uploadImageState.observe (viewLifecycleOwner, {
            if (it) {
                lifecycleScope.launch {
                    joinViewModel.uploadFile(joinViewModel.file.value!!, "application/pdf")
                }
            }
        })

        joinViewModel.uploadFileState.observe(viewLifecycleOwner, {
            if (it) {
                joinViewModel.joinExpert(
                    binding.etEmail.text.toString()
                    , binding.etPw.text.toString()
                    , binding.etName.text.toString()
                    , joinViewModel.userImage.value!!
                    , identification
                    , binding.etDescription.text.toString()
                    , address
                    , binding.etPhone.text.toString()
                    , license
                    , binding.tvDateChoice.text.toString()
                    , categories
                )
            }
        })

        joinViewModel.joinState.observe(viewLifecycleOwner, {
            if (it) {
                findNavController().navigate(R.id.nav_joinFinishFragment)
                joinViewModel.setJoinState(false)
            }
        })
    }

    fun showDatePickerDialog() {
        val constraintsBuilder = CalendarConstraints.Builder().setValidator(DateValidatorPointBackward.now())

        val builder= MaterialDatePicker.Builder.datePicker()
        builder.setTheme(R.style.customDatePickerDialog)
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .setCalendarConstraints(constraintsBuilder.build())

        val picker = builder.build()

        picker.addOnPositiveButtonClickListener { datepicker ->
            binding.tvDateChoice.text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Date(datepicker))
        }

        picker.show(childFragmentManager, picker.toString())
    }

    fun startTimer() {
        binding.tvTime.visibility = View.VISIBLE
        timer = object : CountDownTimer(600000, 1000) {
            override fun onTick(time: Long) {
                binding.tvTime.text = "남은 시간: ${time / 60000}분 ${(time % 60000) / 1000}초"
            }

            override fun onFinish() {
                showCustomToast("인증 시간이 만료되었습니다. 다시 시도해주세요.")
                binding.tvTime.text = ""
                emailVerifyViewModel.setCodeState(false)
            }
        }
        timer.start()
    }

    fun stopTimer() {
        timer?.cancel()
        binding.tvTime.text= ""
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

    fun isFillInput() : Boolean {
        val result = binding.etEmail.text.toString().isNotBlank()
                && binding.etName.text.toString().isNotBlank()
                && binding.etIdentificationFront.text.toString().isNotBlank()
                && binding.etIdentificationBack.text.toString().isNotBlank()
                && binding.etPw.text.toString().isNotBlank()
                && binding.etPhone.text.toString().isNotBlank()
                && binding.etAddress1.text.toString().isNotBlank()
                && binding.etDescription.text.toString().isNotBlank()
                && binding.tvDateChoice.text.toString() != "날짜를 선택하세요."
                && ::license.isInitialized
        if (result) {
            return true
        } else {
            showCustomToast("빈 칸을 입력해주세요.")
            return false
        }
    }

    fun isValidName(name: String): Boolean {
        val regex = "^[가-힣]{3,4}$".toRegex()
        val result = regex.matches(name)
        if (!result) {
            showCustomToast("이름이 형식에 맞지 않습니다.")
        }
        return regex.matches(name)
    }

    fun isValidPw(pw: String): Boolean {
        val regex = "^[a-zA-Z0-9!@#\$%^&*()]{8,16}$".toRegex()
        val result = regex.matches(pw)
        if (!result) {
            showCustomToast("비밀번호가 형식에 맞지 않습니다.")
        }
        return regex.matches(pw)
    }

    fun isValidDescription(description: String): Boolean {
        if (description.length <= 500) {
            return true
        }
        showCustomToast("최대 500자까지 작성 가능합니다.")
        return false
    }

    fun isValidPhone(phone: String): Boolean {
        if (phone.length in (9..11)) {
            return true
        }
        showCustomToast("전화번호가 형식에 맞지 않습니다.")
        return false
    }

    companion object {
        @JvmStatic
        fun newInstance(key: String, value: String) =
            JoinProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(key, value)
                }
            }
    }
}