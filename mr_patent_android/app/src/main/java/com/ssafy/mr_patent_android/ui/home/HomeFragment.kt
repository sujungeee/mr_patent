package com.ssafy.mr_patent_android.ui.home

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.base.ApplicationClass.Companion.sharedPreferences
import com.ssafy.mr_patent_android.base.BaseFragment
import com.ssafy.mr_patent_android.data.model.dto.UserDto
import com.ssafy.mr_patent_android.databinding.FragmentHomeBinding

private const val TAG = "HomeFragment"

class HomeFragment :
    BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::bind, R.layout.fragment_home) {
    val viewModel: HomeViewModel by viewModels()
    lateinit var user: UserDto

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        user = sharedPreferences.getUser()

        if (user.userRole == -1) {
            binding.btnGoExpert.visibility = View.VISIBLE
            binding.btnGoExpert.setOnClickListener {
                //전문가 등록 페이지로 이동
                findNavController().navigate(HomeFragmentDirections.actionNavFragmentHomeToPatentAttorneyListFragment())
            }
        }

        initView()

    }

    fun initView() {
        val spannableString = SpannableString(getString(R.string.home_intro, user.userName))
        val start = 6
        val end = start + user.userName.length
        spannableString.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.mr_blue)),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        binding.tvIntro.text = spannableString

    }


    companion object {
        val categoryMap = mapOf(
            R.id.chip_chemi to "화학공학",
            R.id.chip_mecha to "기계공학",
            R.id.chip_elec to "전기/전자",
            R.id.chip_life to "생명공학"
        )

    }
}