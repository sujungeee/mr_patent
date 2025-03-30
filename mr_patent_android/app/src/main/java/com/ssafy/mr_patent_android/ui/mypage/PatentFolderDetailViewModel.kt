package com.ssafy.mr_patent_android.ui.mypage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.mr_patent_android.data.model.response.PatentListResponse
import com.ssafy.mr_patent_android.data.remote.RetrofitUtil.Companion.patentService
import kotlinx.coroutines.launch

class PatentFolderDetailViewModel : ViewModel() {
    private val _folderId = MutableLiveData<Int>()
    val folderId: LiveData<Int>
        get() = _folderId

    private val _patents = MutableLiveData<List<PatentListResponse.PatentSummaryInfo>>()
    val patents: LiveData<List<PatentListResponse.PatentSummaryInfo>>
        get() = _patents

    private val _patentId = MutableLiveData<Int>()
    val patentId: LiveData<Int>
        get() = _patentId

    private val _deleteFlag = MutableLiveData<Boolean>()
    val deleteFlag: LiveData<Boolean>
        get() = _deleteFlag

    fun setFolderId(folderId: Int) {
        _folderId.value = folderId
    }

    // TODO: delete
    fun setPatents(patents: List<PatentListResponse.PatentSummaryInfo>) {
        _patents.value = patents
    }

    fun setPatentId(patentId: Int) {
        _patentId.value = patentId
    }

    fun setDeleteFlag(deleteFlag: Boolean) {
        _deleteFlag.value = deleteFlag
    }

    fun getPatentList(folderId: Int) {
        viewModelScope.launch {
            runCatching{
                patentService.getPatentList(folderId)
            }.onSuccess {
                if (it.isSuccessful) {
                    it.body()?.let { response ->
                        _patents.value = response.data?.patents
                    }
                }
            }.onFailure {

            }
        }
    }
}