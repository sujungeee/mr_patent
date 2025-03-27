package com.ssafy.mr_patent_android.ui.login

import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.base.BaseFragment
import com.ssafy.mr_patent_android.databinding.FragmentEmailVerifyBinding

class EmailVerifyFragment : BaseFragment<FragmentEmailVerifyBinding>(FragmentEmailVerifyBinding::bind, R.layout.fragment_email_verify) {
    val viewModel : EmailVerifyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initObserver()
    }

    fun initView() {
        binding.tvBack.setOnClickListener {
            // 뒤로가기
            requireActivity().onBackPressed()
        }

        binding.btnSendCode.setOnClickListener {
            // 코드 전송
            if(binding.etEmail.text.toString().isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(binding.etEmail.text.toString()).matches()) {
                showCustomToast("이메일을 확인해주세요.")
            } else {
                viewModel.setCodeState(true)
                viewModel.sendCode((binding.etEmail.text.toString()))
            }
        }

        binding.btnVerifyEmail.setOnClickListener {
            // 코드 확인
            viewModel.emailVerify(binding.etEmail.text.toString(), binding.etPwd.text.toString())

        }
    }

    fun initObserver() {
        viewModel.codeState.observe(viewLifecycleOwner, {
            if(it) {
                // 코드 전송 성공
                startTimer()
                binding.btnSendCode.isEnabled = false
                binding.etPwd.isEnabled = true
                showCustomToast("코드 전송 성공")

                binding.etPwd.addTextChangedListener {
                    val isEnable = binding.etPwd.text.toString().length == 6
                    // 코드 입력
                    binding.btnVerifyEmail.isEnabled = isEnable
                }
            } else {
                // 시간 끝
                binding.btnSendCode.isEnabled = true
                binding.btnVerifyEmail.isEnabled = false
            }
        })

        viewModel.emailVerifyState.observe(viewLifecycleOwner, {
            if(it) {
                // 이메일 인증 성공
                showCustomToast("이메일 인증 성공")
                findNavController().navigate(EmailVerifyFragmentDirections.actionEmailVerifyFragment2ToPwdChangeFragment(binding.etEmail.text.toString(), binding.etPwd.text.toString().toInt()))
            } else {
                // 이메일 인증 실패
                showCustomToast("이메일 인증 실패")
                }
        })
    }

    fun startTimer() {
        val timer = object : CountDownTimer(600000, 1000) {
            override fun onTick(time: Long) {
                binding.tvTime.text = "남은 시간: ${time/60000}분 ${(time%60000) / 1000}초"
            }

            override fun onFinish() {
                binding.tvTime.text=""
                viewModel.setCodeState(false)
            }
        }
        timer.start()
    }
}