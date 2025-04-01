package com.ssafy.mr_patent_android.ui.join

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.base.BaseFragment
import com.ssafy.mr_patent_android.databinding.FragmentJoinProfileBinding
import com.ssafy.mr_patent_android.util.ImagePicker

private const val TAG = "JoinProfileFragment_Mr_Patent"
class JoinProfileFragment : BaseFragment<FragmentJoinProfileBinding>(
    FragmentJoinProfileBinding::bind, R.layout.fragment_join_profile
) {

    private val joinViewModel: JoinViewModel by activityViewModels()
    private lateinit var imagePickerUtil: ImagePicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initObserver()
    }

    private fun initView() {
        imagePickerUtil = ImagePicker(this) { uri ->
            joinViewModel.setUserImage(uri.toString())
            Glide.with(requireContext())
                .load(uri)
                .into(binding.ivProfile)
        }
        
        binding.tvBefore.setOnClickListener {
            joinViewModel.setUserImage("android.resource://com.ssafy.mr_patent_android/drawable/user_profile")
            findNavController().popBackStack()
        }

        binding.btnPass.setOnClickListener {
            joinViewModel.setUserImage("android.resource://com.ssafy.mr_patent_android/drawable/user_profile")
            if (joinViewModel.userRole.value == 0)
                findNavController().navigate(R.id.nav_joinMemberFragment)
            else {
                findNavController().navigate(R.id.nav_joinExpertFragment)
            }
        }

        binding.btnNext.setOnClickListener {
            if (joinViewModel.userRole.value == 0) {
                findNavController().navigate(R.id.nav_joinMemberFragment)
            } else {
                findNavController().navigate(R.id.nav_joinExpertFragment)
            }
        }

        binding.ivProfile.setOnClickListener {
            imagePickerUtil.checkPermissionAndOpenGallery()
        }

        binding.tvProfileRegister.setOnClickListener {
            imagePickerUtil.checkPermissionAndOpenGallery()
        }
    }

    private fun initObserver() {
        joinViewModel.userImage.observe(viewLifecycleOwner) {
            Glide.with(requireContext())
                .load(joinViewModel.userImage.value)
                .fallback(R.drawable.user_profile)
                .into(binding.ivProfile)
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