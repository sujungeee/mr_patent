package com.ssafy.mr_patent_android.ui.patent_attorney

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.base.BaseFragment
import com.ssafy.mr_patent_android.data.model.dto.UserDto
import com.ssafy.mr_patent_android.databinding.FragmentExpertListBinding
import com.ssafy.mr_patent_android.ui.home.HomeFragment.Companion.categoryMap
import com.ssafy.mr_patent_android.ui.login.EmailVerifyFragmentDirections
import kotlin.math.log

private const val TAG = "ExpertListFragment"

class ExpertListFragment : BaseFragment<FragmentExpertListBinding>(
    FragmentExpertListBinding::bind,
    R.layout.fragment_expert_list
) {
    val viewModel: ExpertListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initExpertView()
        initSort()
        initAdapter()
        initObserver()

    }

    private fun initAdapter() {
        viewModel.expertList.observe(viewLifecycleOwner) {
            binding.rvPatentAttorneys.adapter = ExpertListAdapter(it) { id ->
                findNavController().navigate(
                    ExpertListFragmentDirections.actionPatentAttorneyListFragmentToPatentAttorneyFragment(
                        id
                    )
                )
            }
        }
    }

    private fun initSort() {
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
                    if (viewModel.expertList.value.isNullOrEmpty()) return
                    else {
                        val expertList = viewModel.expertList.value!!

                        when (position) {
                            0 -> {
                                Log.d(
                                    TAG,
                                    "onItemSelected: 최신순 ${expertList.sortedBy { it.expertCreatedAt }}"
                                )
                                viewModel.setNewExpertList(expertList.sortedBy { it.expertCreatedAt })
                                binding.rvPatentAttorneys.adapter = ExpertListAdapter(
                                    viewModel.newExpertList.value ?: listOf()
                                ) { id ->
                                    findNavController().navigate(
                                        ExpertListFragmentDirections.actionPatentAttorneyListFragmentToPatentAttorneyFragment(
                                            id
                                        )
                                    )
                                }
                            }

                            1 -> {
                                Log.d(
                                    TAG,
                                    "onItemSelected: 오래된순 ${expertList.sortedByDescending { it.expertCreatedAt }}"
                                )
                                viewModel.setNewExpertList(expertList.sortedByDescending { it.expertCreatedAt })
                                binding.rvPatentAttorneys.adapter = ExpertListAdapter(
                                    viewModel.newExpertList.value ?: listOf()
                                ) { id ->
                                    findNavController().navigate(
                                        ExpertListFragmentDirections.actionPatentAttorneyListFragmentToPatentAttorneyFragment(
                                            id
                                        )
                                    )
                                }
                            }

                            else -> {
                                Log.d(
                                    TAG,
                                    "onItemSelected: 경력순 ${expertList.sortedBy { it.expertGetDate }}"
                                )
                                viewModel.setNewExpertList(expertList.sortedByDescending { it.expertGetDate })
                                binding.rvPatentAttorneys.adapter = ExpertListAdapter(
                                    viewModel.newExpertList.value ?: listOf()
                                ) { id ->
                                    findNavController().navigate(
                                        ExpertListFragmentDirections.actionPatentAttorneyListFragmentToPatentAttorneyFragment(
                                            id
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {

                }
            }
    }

    private fun initExpertView() {

        binding.cgFilter.setOnCheckedStateChangeListener { group, checkedIds ->
            viewModel.setFilterState(checkedIds.map { categoryMap[it] })
        }
    }

    private fun initObserver() {
        viewModel.filterState.observe(viewLifecycleOwner) {
            Log.d(TAG, "initObserver: $it")
            if (it.isEmpty()) {
                viewModel.setNewExpertList(viewModel.expertList.value)
            } else {
                viewModel.setNewExpertList(viewModel.expertList.value?.filter { its ->
                    it.containsAll(its.expertCategory)
                })
            }
            binding.rvPatentAttorneys.adapter =
                ExpertListAdapter(viewModel.newExpertList.value ?: listOf()) { id ->
                    findNavController().navigate(
                        ExpertListFragmentDirections.actionPatentAttorneyListFragmentToPatentAttorneyFragment(
                            id
                        )
                    )
                }
        }

    }
}