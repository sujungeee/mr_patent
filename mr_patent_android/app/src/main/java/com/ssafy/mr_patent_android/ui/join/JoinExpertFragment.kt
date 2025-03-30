package com.ssafy.mr_patent_android.ui.join

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.MaterialDatePicker
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.base.BaseFragment
import com.ssafy.mr_patent_android.databinding.FragmentJoinExpertBinding
import com.ssafy.mr_patent_android.ui.login.EmailVerifyViewModel
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Date
import java.util.Locale

private const val TAG = "JoinExpertFragment_Mr_Patent"
class JoinExpertFragment : BaseFragment<FragmentJoinExpertBinding>(
    FragmentJoinExpertBinding::bind, R.layout.fragment_join_expert
) {
    private val joinViewModel: JoinViewModel by activityViewModels()
    private val emailVerifyViewModel: EmailVerifyViewModel by activityViewModels()

    private lateinit var license : String
    private var emailFlag : Boolean = false

//    private val args: JoinExpertFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initObserver()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.nav_joinProfileFragment)
            }
        })

    }

    private fun initView() {
//        args.address.let {
//            binding.etAddress1.setText(it)
//        }

        binding.tvBefore.setOnClickListener {
            findNavController().navigate(R.id.nav_joinProfileFragment)
        }

        // TODO: delete
        binding.btnJoinApply.setOnClickListener {
            joinViewModel.setJoinState(true)
        }

        binding.btnJoinApply.setOnClickListener { // TODO: add
            if ( emailFlag
                && isFillInput()
                && isValidName(binding.etName.text.toString())
                && isValidPw(binding.etPw.text.toString())
                && isValidDescription(binding.etDescription.text.toString())
            ) {
                val identification =
                    binding.etIdentificationFront.text.toString() + "-" + binding.etIdentificationBack.text.toString()
                val address =
                    binding.etAddress1.text.toString() + " " + binding.etAddress2.text.toString()
                val categories = mutableListOf<String>()
                for (i in 0 until binding.cgFilter.childCount) {
                    val chip = binding.cgFilter.getChildAt(i) as Chip
                    if (chip.isChecked) {
                        categories.add(chip.text.toString())
                    }
                }

                joinViewModel.joinExpert(
                    binding.etEmail.text.toString(),
                    binding.etPw.text.toString(),
                    binding.etName.text.toString(),
                    identification,
                    binding.etDescription.text.toString()
                    , address
                    , binding.etPhone.text.toString()
                    , license
                    , binding.tvDateChoice.text.toString()
                    , categories
                )
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
            emailVerifyViewModel.sendCode((binding.etEmail.text.toString()))
        }

        binding.btnVerify.setOnClickListener {
            emailVerifyViewModel.emailVerify(
                binding.etEmail.text.toString(),
                binding.etCode.text.toString()
            )
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
            if (it) {
                binding.tvIdDupl.visibility = View.VISIBLE
                binding.btnVerificationSend.visibility = View.GONE
            } else {
                binding.btnVerificationSend.visibility = View.VISIBLE
                binding.tvIdDupl.visibility = View.GONE
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
                binding.btnVerify.isEnabled = false
            }
        })

        emailVerifyViewModel.emailVerifyState.observe(viewLifecycleOwner, {
            if (it) {
                // 이메일 인증 성공
                showCustomToast("이메일 인증 성공")
                emailFlag = true
            } else {
                // 이메일 인증 실패
                showCustomToast("이메일 인증 실패")
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
        val builder= MaterialDatePicker.Builder.datePicker()
        builder.setTheme(R.style.customDatePickerDialog)
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())

        val picker = builder.build()

        picker.addOnPositiveButtonClickListener { datepicker ->
            binding.tvDateChoice.text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Date(datepicker))
        }

        picker.show(childFragmentManager, picker.toString())
    }

    fun startTimer() {
        binding.tvTime.visibility = View.VISIBLE
        val timer = object : CountDownTimer(600000, 1000) {
            override fun onTick(time: Long) {
                binding.tvTime.text = "남은 시간: ${time / 60000}분 ${(time % 60000) / 1000}초"
            }

            override fun onFinish() {
                binding.tvTime.text = ""
                emailVerifyViewModel.setCodeState(false)
            }
        }
        timer.start()
    }

    fun isFillInput() : Boolean {
        val result = binding.etEmail.text.toString().isNotBlank()
                && binding.etName.text.toString().isNotBlank()
                && binding.etIdentificationFront.text.toString().isNotBlank()
                && binding.etIdentificationBack.text.toString().isNotBlank()
                && binding.etPw.text.toString().isNotBlank()
                && binding.etPhone.text.toString().isNotBlank()
                && binding.etAddress1.text.toString().isNotBlank()
                && binding.etAddress2.text.toString().isNotBlank()
                && binding.etDescription.text.toString().isNotBlank()
                && binding.tvDateChoice.text.toString() != "날짜를 선택하세요."
                && license.isNotBlank()
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
        val regex = "^[a-zA-Z가-힣!@#\$%^&*()]{8,16}$".toRegex()
        val result = regex.matches(pw)
        if (!result) {
            showCustomToast("비밀번호가 형식에 맞지 않습니다.")
        }
        return regex.matches(pw)
    }

    fun isValidDescription(description: String): Boolean {
        if (description.length > 500) {
            showCustomToast("최대 500자까지 작성 가능합니다.")
            return false
        }
        return true
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