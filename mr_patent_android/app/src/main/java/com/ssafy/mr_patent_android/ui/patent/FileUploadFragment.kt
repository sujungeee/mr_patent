package com.ssafy.mr_patent_android.ui.patent

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.base.BaseFragment
import com.ssafy.mr_patent_android.databinding.FragmentFileUploadBinding

class FileUploadFragment : BaseFragment<FragmentFileUploadBinding>(
    FragmentFileUploadBinding::bind, R.layout.fragment_file_upload
) {


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
            findNavController().popBackStack()
        }
    }

    private fun initObserver() {

    }

    companion object {
        @JvmStatic
        fun newInstance(key: String, value: String) =
            FileUploadFragment().apply {
                arguments = Bundle().apply {
                    putString(key, value)
                }
            }
    }
}