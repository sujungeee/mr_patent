package com.ssafy.mr_patent_android.ui.chat

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.ssafy.mr_patent_android.databinding.FragmentChatBottomSheetBinding


private const val TAG = "ChatBottomSheetFragment"
class ChatBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentChatBottomSheetBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var inputMethodManager: InputMethodManager


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomSheet = view.parent as View
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager


        binding.btnDown.setOnClickListener {
            dismiss()
        }
    }

}
