package com.ssafy.mr_patent_android.ui.join

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.base.BaseFragment
import com.ssafy.mr_patent_android.databinding.FragmentJoinMemberBinding
import com.ssafy.mr_patent_android.ui.login.EmailVerifyViewModel
import com.ssafy.mr_patent_android.ui.login.LoginActivity

private const val TAG = "JoinMemberFragment_Mr_Patent"
class JoinMemberFragment : BaseFragment<FragmentJoinMemberBinding>(
    FragmentJoinMemberBinding::bind, R.layout.fragment_join_member
) {
    private val joinViewModel : JoinViewModel by activityViewModels()
    private val emailVerifyViewModel : EmailVerifyViewModel by activityViewModels()

    private var emailFlag : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initObserver()
    }

    private fun initView() {
        binding.tvBefore.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnJoin.setOnClickListener {
            // TODO: add
//            if (emailFlag
//                && isFillInput()
//                && isValidName(binding.etName.text.toString())
//                && isValidPw(binding.etPw.text.toString())) {
//                joinViewModel.joinMember(binding.etEmail.text.toString(), binding.etName.text.toString(), binding.etPw.text.toString())
//            }
            findNavController().navigate(R.id.nav_joinFinishFragment) // TODO: delete
        }

        binding.btnEmailDupl.setOnClickListener {
            if(binding.etEmail.text.toString().isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(binding.etEmail.text.toString()).matches()) {
                showCustomToast("이메일을 확인해주세요.")
            } else {
                joinViewModel.checkDuplEmail(binding.etEmail.text.toString())
            }
        }

        binding.btnVerificationSend.setOnClickListener {
            emailVerifyViewModel.sendCode((binding.etEmail.text.toString()))
        }

        binding.btnVerify.setOnClickListener {
            emailVerifyViewModel.emailVerify(binding.etEmail.text.toString(), binding.etCode.text.toString())
        }
    }

    private fun initObserver() {
        joinViewModel.checkDuplEmail.observe(viewLifecycleOwner,{
            if (it) {
                // 이메일 중복
                binding.tvIdDupl.visibility = View.VISIBLE
                binding.btnVerificationSend.visibility = View.GONE
            } else {
                // 이메일 중복 X
                binding.btnVerificationSend.visibility = View.VISIBLE
                binding.tvIdDupl.visibility = View.GONE
            }
        })

        emailVerifyViewModel.codeState.observe(viewLifecycleOwner, {
            if(it) {
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
            if(it) {
                // 이메일 인증 성공
                showCustomToast("이메일 인증 성공")
                emailFlag = true
            } else {
                // 이메일 인증 실패
                showCustomToast("이메일 인증 실패")
            }
        })

        joinViewModel.joinState.observe(viewLifecycleOwner, {
            if(it) {
                findNavController().navigate(R.id.nav_joinFinishFragment)
                joinViewModel.setJoinState(false)
            }
        })
    }

    fun startTimer() {
        binding.tvTime.visibility = View.VISIBLE
        val timer = object : CountDownTimer(600000, 1000) {
            override fun onTick(time: Long) {
                binding.tvTime.text = "남은 시간: ${time/60000}분 ${(time%60000) / 1000}초"
            }

            override fun onFinish() {
                binding.tvTime.text=""
                emailVerifyViewModel.setCodeState(false)
            }
        }
        timer.start()
    }

    fun isFillInput(): Boolean {
        val result = binding.etEmail.text.toString().isNotBlank()
                && binding.etName.text.toString().isNotBlank()
                && binding.etPw.text.toString().isNotBlank()

        if (result) {
            return true
        } else {
            showCustomToast("빈 칸을 입력해주세요.")
            return false
        }
    }

    fun isValidName(name: String): Boolean {
        val regex = "^[a-zA-Z가-힣]{4,12}$".toRegex()
        val result = regex.matches(name)
        if (!result) {
            showCustomToast("닉네임이 형식에 맞지 않습니다.")
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

    companion object {
        @JvmStatic
        fun newInstance(key : String, value : String) =
            JoinProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(key, value)
                }
            }
    }
}