package com.ssafy.mr_patent_android.ui.join

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.base.BaseFragment
import com.ssafy.mr_patent_android.databinding.FragmentJoinProfileBinding
import com.ssafy.mr_patent_android.ui.login.LoginActivity

private const val TAG = "JoinProfileFragment_Mr_Patent"
class JoinProfileFragment : BaseFragment<FragmentJoinProfileBinding>(
    FragmentJoinProfileBinding::bind, R.layout.fragment_join_profile
) {

    private val joinViewModel: JoinViewModel by activityViewModels()

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
            findNavController().navigate(R.id.nav_joinBranchFragment)
        }

        binding.btnPass.setOnClickListener {
            joinViewModel.setUserImage("")
            if (joinViewModel.userRole.value == 0)
                findNavController().navigate(R.id.nav_joinMemberFragment)
            else {
                findNavController().navigate(R.id.nav_joinExpertFragment)
            }
        }

        binding.btnNext.setOnClickListener {
            joinViewModel.setUserImage("")
            if (joinViewModel.userRole.value == 0) {
                findNavController().navigate(R.id.nav_joinMemberFragment)
            } else {
                findNavController().navigate(R.id.nav_joinExpertFragment)
            }
        }
    }

    private fun initObserver() {
        joinViewModel.userImage.observe(viewLifecycleOwner) { // 프로필 사진 보여주기

        }
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