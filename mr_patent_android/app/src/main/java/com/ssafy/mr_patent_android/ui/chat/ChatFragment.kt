package com.ssafy.mr_patent_android.ui.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.databinding.FragmentChatBinding

class ChatFragment : Fragment() {
    lateinit var bottomSheetFragment: ChatBottomSheetFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentChatBinding.inflate(inflater, container, false)

        binding.btnAdd.setOnClickListener {
            if (!::bottomSheetFragment.isInitialized){
                bottomSheetFragment = ChatBottomSheetFragment()
            }
            if (!bottomSheetFragment.isAdded){
                bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
            }


        }


        return binding.root
    }
}
