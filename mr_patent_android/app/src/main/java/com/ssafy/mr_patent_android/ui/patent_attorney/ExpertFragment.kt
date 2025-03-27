package com.ssafy.mr_patent_android.ui.patent_attorney

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.base.ApplicationClass.Companion.sharedPreferences
import com.ssafy.mr_patent_android.base.BaseFragment
import com.ssafy.mr_patent_android.databinding.FragmentExpertBinding
import com.ssafy.mr_patent_android.ui.login.PwdChangeFragmentArgs

class ExpertFragment :
    BaseFragment<FragmentExpertBinding>(FragmentExpertBinding::bind, R.layout.fragment_expert) {
    val viewModel: ExpertViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        initView()
        initObserver()

    }

    fun initView() {
        viewModel.getExpert(id)
        if (id == sharedPreferences.getUser().expertId) {
            binding.btnEditProfile.visibility = View.VISIBLE
            binding.btnEditProfile.setOnClickListener {
                findNavController().navigate(ExpertFragmentDirections.actionPatentAttorneyFragmentToProfileEditFragment())
            }
            binding.fabChat.visibility = View.GONE
        } else {
            binding.fabChat.setOnClickListener {
                findNavController().navigate(
                    ExpertFragmentDirections.actionPatentAttorneyFragmentToChatFragment(
                        id
                    )
                )
            }
        }
    }

    fun initObserver() {
        viewModel.expert.observe(viewLifecycleOwner) {
            binding.tvName.text = it.userName
            binding.tvIntro.text = it.expertDescription
            binding.tvInfo.text = it.expertGetDate
            binding.tvPhone.text = it.expertPhone
            binding.tvAddress.text = it.expertAddress

            Glide
                .with(binding.root)
                .load(it.userImage)
                .circleCrop()
                .placeholder(R.drawable.user_profile)
                .into(binding.ivProfile);

        }
    }
}