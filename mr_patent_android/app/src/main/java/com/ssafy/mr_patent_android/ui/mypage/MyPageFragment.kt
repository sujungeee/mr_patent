package com.ssafy.mr_patent_android.ui.mypage

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.base.ApplicationClass.Companion.sharedPreferences
import com.ssafy.mr_patent_android.base.BaseFragment
import com.ssafy.mr_patent_android.databinding.FragmentMyPageBinding
import com.ssafy.mr_patent_android.ui.login.LoginActivity

class MyPageFragment : BaseFragment<FragmentMyPageBinding>(
    FragmentMyPageBinding::bind, R.layout.fragment_my_page
) {
    private val userLeaveViewModel : UserLeaveViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initObserver()
    }

    private fun initView() {
        binding.tvUserName.text = sharedPreferences.getUser().userName + "님"

        // 나의 프로필(이미지)
        // TODO

        binding.tvProfileUpdate.setOnClickListener { // 프로필 수정

        }

        // 특허 분석 리포트
        binding.tvPatentReports.setOnClickListener {
            findNavController().navigate(R.id.patentFolderFragment)
        }

        // 알림 설정
        // TODO

        binding.tvSettingPasswordChange.setOnClickListener {
            findNavController().navigate(R.id.pwdEditFragment)
        }

        binding.tvSettingLogout.setOnClickListener {
            // 로그아웃
            userLeaveViewModel.logout()
            // TODO: delete
            showCustomToast("로그아웃 되었습니다.")
            sharedPreferences.clearToken()
            val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            requireActivity().finish()
        }

        binding.tvSettingDelete.setOnClickListener {
            findNavController().navigate(R.id.userDeleteFragment)
        }
    }

    private fun initObserver() {
        userLeaveViewModel.toastMsg.observe(viewLifecycleOwner) {
            if (it == "로그아웃 되었습니다.") {
                showCustomToast(it)
                sharedPreferences.clearToken()
                val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                requireActivity().finish()
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(key: String, value: String) =
            MyPageFragment().apply {
                arguments = Bundle().apply {
                    putString(key, value)
                }
            }
    }
}