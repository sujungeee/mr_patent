package com.ssafy.mr_patent_android.ui.join

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.mr_patent_android.base.ApplicationClass.Companion.networkUtil
import com.ssafy.mr_patent_android.data.model.dto.JoinExpertRequest
import com.ssafy.mr_patent_android.data.model.dto.JoinRequest
import com.ssafy.mr_patent_android.data.remote.RetrofitUtil.Companion.authService
import com.ssafy.mr_patent_android.data.remote.RetrofitUtil.Companion.fileService
import com.ssafy.mr_patent_android.ui.login.LoginActivity
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private const val TAG = "JoinViewModel_Mr_Patent"
class JoinViewModel: ViewModel() {
    private val _toastMsg = MutableLiveData<String>()
    val toastMsg: LiveData<String>
        get() = _toastMsg

    private val _userRole = MutableLiveData<Int>()
    val userRole: LiveData<Int>
        get() = _userRole

    private val _userImage = MutableLiveData<String>()
    val userImage: LiveData<String>
        get() = _userImage

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String>
        get() = _userName

    private val _address = MutableLiveData<String>()
    val address: LiveData<String>
        get() = _address

    private val _file = MutableLiveData<String>()
    val file: LiveData<String>
        get() = _file

    private val _checkDuplEmail = MutableLiveData<Boolean>()
    val checkDuplEmail: LiveData<Boolean>
        get() = _checkDuplEmail

    private val _uploadImageState = MutableLiveData<Boolean>()
    val uploadImageState: LiveData<Boolean>
        get() = _uploadImageState

    private val _uploadFileState = MutableLiveData<Boolean>()
    val uploadFileState: LiveData<Boolean>
        get() = _uploadFileState

    private val _joinState = MutableLiveData<Boolean>()
    val joinState: LiveData<Boolean>
        get() = _joinState

    fun setToastMsg(toastMsg: String) {
        _toastMsg.value = toastMsg
    }

    fun setUserRole(userRole: Int) {
        _userRole.value = userRole
    }

    fun setUserImage(userImage: String) {
        _userImage.value = userImage
    }

    fun setFile(file: String) {
        _file.value = file
    }

    fun setJoinState(joinState: Boolean) {
        _joinState.value = joinState
    }

    fun reset() {
        _userRole.value = -1
        _userImage.value = ""
        _userName.value = ""
        _address.value = ""
        _file.value = ""
        _checkDuplEmail.value = false
        _uploadImageState.value = false
        _uploadFileState.value = false
        _joinState.value = false
        _toastMsg.value = ""
    }

    fun joinMember(userEmail: String, userName: String, userPw: String, userImage: String) {
        viewModelScope.launch {
            runCatching {
                authService.joinMember(
                    JoinRequest(
                        userEmail
                        , userName
                        , userPw
                        , userImage
                        , 0
                    )
                )
            }.onSuccess {
                if (it.isSuccessful) {
                    it.body()?.let { response ->
                        if (response.data != null) {
                            _toastMsg.value = "회원가입에 성공했어요!"
                            _joinState.value = true
                            _userName.value = userName
                        }
                    }
                } else {
                    it.errorBody()?.let {
                        it1 -> networkUtil.getErrorResponse(it1)
                    }

                }
            }.onFailure {
                _toastMsg.value = "회원가입에 실패했어요!"
            }
        }
    }


    fun joinExpert(userEmail: String, userPw: String, userName: String, userImage: String
                   , expertIdentification: String, expertDescription: String, expertAddress: String, expertPhone: String
                   , expertLicense: String, expertGetDate: String, expertCategory: MutableList<String>) {
        viewModelScope.launch {
            runCatching {
                authService.joinExpert(
                    JoinExpertRequest(
                        userEmail
                        , userPw
                        , userName
                        , userImage
                        , 1
                        , expertIdentification
                        , expertDescription
                        , expertAddress
                        , expertPhone
                        , expertLicense
                        , expertGetDate
                        , expertCategory
                    )
                )
            }.onSuccess {
                if (it.isSuccessful) {
                    it.body()?.let { response ->
                        if (response.data != null) {
                            _toastMsg.value = "회원가입에 성공했어요!"
                            _joinState.value = true
                        }
                    }
                } else {
                    it.errorBody()?.let {
                        it1 -> networkUtil.getErrorResponse(it1)
                    }
                }
            }.onFailure {
                _toastMsg.value = "회원가입에 실패했어요!"
            }
        }
    }

    fun checkDuplEmail(userEmail: String) {
        viewModelScope.launch {
            runCatching {
                authService.checkDuplEmail(userEmail)
            }.onSuccess {
                if (it.isSuccessful) {
                    it.body()?.let {
                        it.data?.let { response ->
                            if (response) {
                                _checkDuplEmail.value = true
                            } else {
                                _checkDuplEmail.value = false
                            }
                        }
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

    suspend fun uploadFile(file: String, contentType: String, context: LoginActivity) {
        runCatching {
            var response = false
            var preSignedUrl = ""

            if (file != null) {
                preSignedUrl = getPreSignedUrl(file, contentType)
                response = uploadFileS3(preSignedUrl, file, contentType, context)
            }
            if (response) {
                _uploadImageState.value = true
                _uploadFileState.value = true
                when (contentType) {
                    "image/jpeg" -> _userImage.value = preSignedUrl
                    "pdf" -> _file.value = preSignedUrl
                }
            } else {
                throw Exception("업로드 실패")
            }
        }.onFailure {
            it.printStackTrace()
        }
    }

    suspend fun getPreSignedUrl(file: String, contentType: String) : String {
        return suspendCoroutine { continuation ->
            viewModelScope.launch {
                runCatching {
                    fileService.getPreSignedUrl(file, contentType)
                }.onSuccess {
                    if (it.isSuccessful) {
                        it.body()?.let {
                            continuation.resume(it.data.toString())
                        }
                    } else {
                        continuation.resumeWithException(Exception("발급 실패"))
                    }
                }.onFailure {
                    it.printStackTrace()
                    continuation.resumeWithException(it)
                }
            }
        }
    }

    suspend fun uploadFileS3(preSignedUrl: String, filePath: String, contentType: String, context: Context) : Boolean {
        return suspendCoroutine { continuation ->
            viewModelScope.launch {
                runCatching {
                    val file = File(filePath)
                    val requestBody = file.asRequestBody(contentType.toMediaType())
                    fileService.uploadFile(preSignedUrl, requestBody)
                }.onSuccess {
                    if (it.isSuccessful) {
                        continuation.resume(true)
                    } else {
                        continuation.resumeWithException(Exception("업로드 실패"))
                    }
                }.onFailure {
                    it.printStackTrace()
                    continuation.resumeWithException(it)
                }
            }
        }
    }
}