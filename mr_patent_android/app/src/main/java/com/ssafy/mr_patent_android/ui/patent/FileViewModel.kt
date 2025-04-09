package com.ssafy.mr_patent_android.ui.patent

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.mr_patent_android.base.ApplicationClass.Companion.networkUtil
import com.ssafy.mr_patent_android.data.model.response.PatentContentResponse
import com.ssafy.mr_patent_android.data.remote.RetrofitUtil.Companion.patentService
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

private const val TAG = "FileViewModel_Mr_Patent"
class FileViewModel : ViewModel() {
    private val _toastMsg = MutableLiveData<String>()
    val toastMsg: LiveData<String>
        get() = _toastMsg

    private val _extractionType = MutableLiveData<String>()
    val extractionType: LiveData<String>
        get() = _extractionType

    private val _fileUri = MutableLiveData<String?>()
    val fileUri: LiveData<String?>
        get() = _fileUri

    private val _uploadState = MutableLiveData<Boolean>()
    val uploadState: LiveData<Boolean>
        get() = _uploadState

    private val _patentContent = MutableLiveData<PatentContentResponse?>()
    val patentContent: LiveData<PatentContentResponse?>
        get() = _patentContent

    fun setExtractionType(type: String) {
        _extractionType.value = type
    }

    fun setFileUri(uri: String?) {
        _fileUri.value = uri
    }

    fun setPatentContent(patentContent: PatentContentResponse?) {
        _patentContent.value = patentContent
    }

    fun getOcrContent(file: File) {
        viewModelScope.launch {
            runCatching{
                patentService.getOcrContent(MultipartBody.Part.createFormData("file", file.name, file.asRequestBody("application/pdf".toMediaTypeOrNull())))
            }.onSuccess {
                if (it.isSuccessful) {
                    it.body()?.data.let { response ->
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