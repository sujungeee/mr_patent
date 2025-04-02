package com.ssafy.mr_patent_android.ui.patent

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class SimiliarityTestViewModel : ViewModel() {
    private val _toastMsg = MutableLiveData<String>()
    val toastMsg: LiveData<String>
        get() = _toastMsg

    private val _status = MutableLiveData<String>()
    val status: LiveData<String>
        get() = _status

    private val _addFlag = MutableLiveData<Boolean>()
    val addFlag: LiveData<Boolean>
        get() = _addFlag

    private val _patentId = MutableLiveData<Int>()
    val patentId: LiveData<Int>
        get() = _patentId

    fun setStatus(status: String) {
        _status.value = status
    }

    fun setPatentId(patentId: Int) {
        _patentId.value = patentId
    }

    fun addDraft() {
        viewModelScope.launch {
            runCatching {

            }.onSuccess {
                // folderid 주면
                _addFlag.value = true
            }.onFailure {

            }
        }
    }

    fun similiaritytest() {
        viewModelScope.launch {
            runCatching {
//                similiarityTestService.similiarityTest(patentDraftId)
            }.onSuccess {
                // success 시 ststus.value = "finished"
                // patentId 받기
            }.onFailure {

            }
        }
    }
}