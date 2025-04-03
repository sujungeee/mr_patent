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
import com.ssafy.mr_patent_android.data.remote.RetrofitUtil.Companion.studyService
import kotlinx.coroutines.launch

class LevelListViewModel : ViewModel() {
    private val _levelList = MutableLiveData<LevelDto?>()
    val levelList: LiveData<LevelDto?>
        get() = _levelList

    fun getLevels() {
        viewModelScope.launch {
            runCatching {
                studyService.getLevels()
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    response.body()?.data.let { levelList ->
                        _levelList.value = levelList
                    }
                } else {

                    _levelList.value = LevelDto(listOf(
                        LevelDto.Level(8, true,true, 1, "Lv.1"),
                        LevelDto.Level(8, true,true, 1, "Lv.2"),
                        LevelDto.Level(100, true,false, 1, "Lv.3"),
                        LevelDto.Level(4, true,false, 1, "Lv.4"),
                        LevelDto.Level(100, false,false, 1, "Lv.5"),
                        LevelDto.Level(100, false,false, 1, "단어장")))
                    Log.e("LevelListViewModel", "getLevels: ${response.errorBody()}")
                }
            }.onFailure {
                Log.e("LevelListViewModel", "getLevels: ${it.message}")
            }
        }
    }



}