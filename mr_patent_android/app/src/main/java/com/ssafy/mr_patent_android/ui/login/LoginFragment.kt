package com.ssafy.mr_patent_android.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ssafy.mr_patent_android.MainActivity
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.databinding.FragmentLoginBinding

private const val TAG = "LoginFragment"
class LoginFragment : Fragment() {
    lateinit var binding: FragmentLoginBinding
    val viewModel: LoginViewModel by viewModels()
    var emailFlag= false
    var pwdFlag= false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentLoginBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initObserver()

    }
    fun initView(){
        binding.tvPwdForgot.setOnClickListener {
            // 비밀번호 찾기
            findNavController().navigate(R.id.nav_emailVerifyFragment)
        }

        binding.tvSignup.setOnClickListener {
            // 회원가입
            findNavController().navigate(R.id.nav_joinBranchFragment)
        }

        binding.btnLogin.setOnClickListener {
            if(checkEmail(binding.etEmail.text.toString()) && binding.etPwd.text.length >=8){
                viewModel.login(binding.etEmail.text.toString(), binding.etPwd.text.toString())
            }
            (requireActivity() as LoginActivity).navigateToMain()


        }

        binding.etEmail.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (checkEmail(binding.etEmail.text.toString())) {
                    emailFlag = true
                } else {
                    binding.etEmail.error = "이메일 형식이 아닙니다."
                }
            }
        }
        binding.etPwd.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                if (binding.etPwd.text.length <8) {
                    binding.etPwd.error = "8자 이상 입력해주세요."
                }
            }
        }

        binding.etEmail.addTextChangedListener {
            if (checkEmail(binding.etEmail.text.toString())) {
                emailFlag = true
            } else {
                emailFlag = false
            }
            if (emailFlag && pwdFlag) {
                binding.btnLogin.isEnabled = true
            } else {
                binding.btnLogin.isEnabled = false
            }
        }

        binding.etPwd.addTextChangedListener {
            if (binding.etPwd.text.length >= 8) {
                pwdFlag = true
            } else {
                pwdFlag = false
            }
            if (emailFlag && pwdFlag) {
                binding.btnLogin.isEnabled = true
            } else {
                binding.btnLogin.isEnabled = false
            }
        }

}
    fun initObserver(){
        viewModel.loginState.observe(viewLifecycleOwner, {
            if (it){
                // 로그인
                val intent= Intent(requireContext(), MainActivity::class.java)
                intent.flags= Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        })
    }

    fun checkEmail(email: String): Boolean {
        return email.contains("@") &&email.contains(".")
    }

}