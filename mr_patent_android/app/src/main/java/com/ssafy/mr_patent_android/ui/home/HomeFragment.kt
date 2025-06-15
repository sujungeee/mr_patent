package com.ssafy.mr_patent_android.ui.home

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.WebViewActivity
import com.ssafy.mr_patent_android.base.ApplicationClass.Companion.sharedPreferences
import com.ssafy.mr_patent_android.base.BaseFragment
import com.ssafy.mr_patent_android.data.model.dto.UserDto
import com.ssafy.mr_patent_android.databinding.FragmentHomeBinding
import com.ssafy.mr_patent_android.ui.expert.ExpertListFragmentDirections
import com.ssafy.mr_patent_android.ui.login.LoginActivity
import kotlin.math.log

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
        Log.d(TAG, "onViewCreated: ${user}")

        initView()

    }

    fun initView() {
        binding.tvGoKipris.setOnClickListener {
            //특허 등록 페이지로 이동
            val intent = Intent(requireContext(), WebViewActivity::class.java)
            intent.putExtra("url", "https://www.kipris.or.kr/khome/main.do")

            startActivity(intent)

            }

        binding.tvGoPatent.setOnClickListener {
            //특허 등록 페이지로 이동
            val intent = Intent(requireContext(), WebViewActivity::class.java)
            intent.putExtra("url", "https://patent.go.kr/smart/portal/Main.do")
            startActivity(intent)
            }


        if (user.userRole == 0) {
            binding.btnGoExpert.visibility = View.VISIBLE
            binding.btnGoExpert.setOnClickListener {
//                findNavController().navigate(
//                    HomeFragmentDirections.actionNavFragmentHomeToPatentAttorneyFragment(
//
//                        2
//                    )
//                )
                //전문가 등록 페이지로 이동
                findNavController().navigate(HomeFragmentDirections.actionNavFragmentHomeToPatentAttorneyListFragment())
            }
        }


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