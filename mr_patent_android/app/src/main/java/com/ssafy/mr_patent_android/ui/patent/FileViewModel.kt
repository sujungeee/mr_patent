package com.ssafy.mr_patent_android.ui.patent

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.mr_patent_android.base.ApplicationClass.Companion.networkUtil
import com.ssafy.mr_patent_android.data.model.response.PatentContentResponse
import com.ssafy.mr_patent_android.data.remote.RetrofitUtil.Companion.fileService
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class FileViewModel : ViewModel() {
    private val _toastMsg = MutableLiveData<String>()
    val toastMsg: LiveData<String>
        get() = _toastMsg

    private val _extractionType = MutableLiveData<String>()
    val extractionType: LiveData<String>
        get() = _extractionType

    private val _fileUri = MutableLiveData<String>()
    val fileUri: LiveData<String>
        get() = _fileUri

    private val _uploadState = MutableLiveData<Boolean>()
    val uploadState: LiveData<Boolean>
        get() = _uploadState

    private val _patentContent = MutableLiveData<PatentContentResponse>()
    val patentContent: LiveData<PatentContentResponse>
        get() = _patentContent

    fun setExtractionType(type: String) {
        _extractionType.value = type
    }

    fun setFileUri(uri: String) {
        _fileUri.value = uri
    }

    // TODO: delete
    fun setUploadState(state: Boolean) {
        _uploadState.value = state
    }

    fun getOcrContent(file: File) {
        viewModelScope.launch {
            runCatching{
                fileService.getOcrContent(MultipartBody.Part.createFormData("file", file.name, file.asRequestBody()))
            }.onSuccess {
                if (it.isSuccessful) {
                    it.body()?.data.let { response ->
                        _uploadState.value = true
                        _patentContent.value = response!!
                    }
                } else {
                    it.errorBody()?.let {
                        it1 -> networkUtil.getErrorResponse(it1)
                    }
                }
            }.onFailure {
                it.printStackTrace()
            }
        }
    }
}