package com.ssafy.mr_patent_android.ui.chat

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.base.BaseFragment
import com.ssafy.mr_patent_android.databinding.FragmentChatBinding

private const val TAG = "ChatFragment"
class ChatFragment : BaseFragment<FragmentChatBinding>(FragmentChatBinding::bind, R.layout.fragment_chat) {
    lateinit var bottomSheetFragment: ChatBottomSheetFragment
    private val args: ChatFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()

    }

    fun initView(){
        binding.btnAdd.setOnClickListener {
            if (!::bottomSheetFragment.isInitialized){
                bottomSheetFragment = ChatBottomSheetFragment()
            }
            if (!bottomSheetFragment.isAdded) {
                bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
            }
    }
}
    }
