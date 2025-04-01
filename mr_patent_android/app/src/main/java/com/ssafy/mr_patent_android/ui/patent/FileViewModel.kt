package com.ssafy.mr_patent_android.ui.patent

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FileViewModel : ViewModel() {
    private val _toastMsg = MutableLiveData<String>()
    val toastMsg: LiveData<String>
        get() = _toastMsg

    private val _extractionType = MutableLiveData<String>()
    val extractionType: LiveData<String>
        get() = _extractionType

    fun setExtractionType(type: String) {
        _extractionType.value = type
    }
}