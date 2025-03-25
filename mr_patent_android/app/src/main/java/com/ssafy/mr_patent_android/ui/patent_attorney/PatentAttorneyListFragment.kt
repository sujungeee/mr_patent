package com.ssafy.mr_patent_android.ui.patent_attorney

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.databinding.FragmentPatentAttorneyListBinding


class PatentAttorneyListFragment : Fragment() {
    lateinit var binding: FragmentPatentAttorneyListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPatentAttorneyListBinding.inflate(inflater, container, false)
        val items = resources.getStringArray(R.array.align_array)

        val myAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, items)

        binding.spinnerPatentAttorneys.adapter = myAdapter

        binding.spinnerPatentAttorneys.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {

                    when (position) {
                        0 -> {
                        }
                        1 -> {
                        }
                        else -> {
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {

                }
            }
        return binding.root
    }
}