package com.ssafy.mr_patent_android.ui.study

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.mr_patent_android.base.ApplicationClass
import com.ssafy.mr_patent_android.data.model.dto.ChatRoomDto
import com.ssafy.mr_patent_android.data.model.dto.LevelDto
import com.ssafy.mr_patent_android.data.remote.RetrofitUtil
import kotlinx.coroutines.launch

class LevelListViewModel : ViewModel() {
    private val _levelList = MutableLiveData<List<LevelDto>>()
    val levelList: LiveData<List<LevelDto>>
        get() = _levelList

//    fun getLevels() {
//        viewModelScope.launch {
//            try {
//                val response = studyService.getLevels()
//                if (response.isSuccessful) {
//                    _levelList.value = response.body()?.data?.levels
//                } else {
//                }
//            } catch (e: Exception) {
//            }
//        }
//    }



}