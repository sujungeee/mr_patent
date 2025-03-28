package com.ssafy.mr_patent_android.ui.address

import android.os.Bundle
import android.view.View
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.base.BaseFragment
import com.ssafy.mr_patent_android.databinding.FragmentAddressEditBinding

class AddressEditFragment : BaseFragment<FragmentAddressEditBinding>(
    FragmentAddressEditBinding::bind, R.layout.fragment_address_edit
) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        @JvmStatic
        fun newInstance(key: String, value: String) =
            AddressEditFragment().apply {
                arguments = Bundle().apply {
                    putString(key, value)
                }
            }
    }
}