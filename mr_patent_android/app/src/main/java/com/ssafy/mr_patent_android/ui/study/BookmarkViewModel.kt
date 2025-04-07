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

private const val TAG = "BookmarkViewModel"
class BookmarkViewModel : ViewModel() {
    private val _bookmarkList = MutableLiveData<LevelDto?>()
    val bookmarkList: LiveData<LevelDto?>
        get() = _bookmarkList



    fun getBookmarkList() {
        viewModelScope.launch {
            runCatching {
                studyService.getBookmarkList()
            }.onSuccess { response ->
                if (response.isSuccessful) {
                    _bookmarkList.value = response.body()?.data
                } else {
                    Log.d(TAG, "getBookmarkList: ${response.message()}")
                }
            }.onFailure { e ->
                Log.d(TAG, "getBookmarkList: $e")
            }
        }
    }




}