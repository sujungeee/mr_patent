package com.ssafy.mr_patent_android.ui.patent

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.mr_patent_android.base.ApplicationClass.Companion.networkUtil
import com.ssafy.mr_patent_android.base.ApplicationClass.Companion.sharedPreferences
import com.ssafy.mr_patent_android.data.model.dto.FolderDto
import com.ssafy.mr_patent_android.data.model.dto.FolderRequest
import com.ssafy.mr_patent_android.data.model.response.PatentRecentResponse
import com.ssafy.mr_patent_android.data.remote.RetrofitUtil.Companion.patentService
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class PatentViewModel : ViewModel() {
    private val _draftType = MutableLiveData<String>()
    val draftType: LiveData<String>
        get() = _draftType

    private val _patentsRecent = MutableLiveData<List<PatentRecentResponse.PatentDraft>>()
    val patentsRecent: LiveData<List<PatentRecentResponse.PatentDraft>>
        get() = _patentsRecent

    private val _folders = MutableLiveData<MutableList<FolderDto.Folder>>()
    val folders: LiveData<MutableList<FolderDto.Folder>>
        get() = _folders

    // 미리 작성된 초안의 경우 그 초안을 불러오기 위한 id
    private val _patentDraftId = MutableLiveData<Int>()
    val patentId: LiveData<Int>
        get() = _patentDraftId

    // 폴더 id
    private val _folderId = MutableLiveData<Int>()
    val folderId: LiveData<Int>
        get() = _folderId

    fun setDraftType(draftType: String) {
        _draftType.value = draftType
    }

    fun setPatentDraftId(patentId: Int) {
        _patentDraftId.value = patentId
    }

    fun setFolderId(folderId: Int) {
        _folderId.value = folderId
    }

    fun getFolderList() {
        viewModelScope.launch {
            runCatching {
                patentService.getFolderList(sharedPreferences.getUser().userId)
            }.onSuccess {
                if (it.isSuccessful) {
                    it.body()?.data?.let { response ->
                        _folders.value = response.folders.toMutableList()
                    }
                } else {
                    it.errorBody()?.let {
                        it1 -> networkUtil.getErrorResponse(it1)
                    }
                }
            }.onFailure {

            }
        }
    }

    fun getRecentPatentList() {
        viewModelScope.launch {
            runCatching {
                patentService.getRecentPatentList(sharedPreferences.getUser().userId, 5)
            }.onSuccess {
                if (it.isSuccessful) {
                    it.body()?.data?.let { response ->
                        _patentsRecent.value = response.patentDrafts
                    }
                } else {
                    it.errorBody()?.let {
                        it1 -> networkUtil.getErrorResponse(it1)
                    }
                }
            }.onFailure {

            }
        }
    }

    fun addFolder(folderName: String) {
        viewModelScope.launch {
            runCatching {
                patentService.addFolder(FolderRequest(
                    sharedPreferences.getUser().userId,
                    folderName
                ))
            }.onSuccess {
                if (it.isSuccessful) {
                    it.body()?.data?.let { response ->
                        if (response) {
                            _folders.value?.add(
                                FolderDto.Folder(
                                    sharedPreferences.getUser().userId,
                                    folderName,
                                    LocalDateTime.now().toString()
                                )
                            )
                        }
                    }
                }
            }.onFailure {

            }
        }
    }
}