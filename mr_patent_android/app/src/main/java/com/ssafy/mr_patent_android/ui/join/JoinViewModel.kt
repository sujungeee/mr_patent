package com.ssafy.mr_patent_android.ui.join

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.mr_patent_android.base.ApplicationClass.Companion.networkUtil
import com.ssafy.mr_patent_android.data.model.dto.JoinExpertRequest
import com.ssafy.mr_patent_android.data.model.dto.JoinRequest
import com.ssafy.mr_patent_android.data.remote.RetrofitUtil.Companion.authService
import kotlinx.coroutines.launch

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

    private val _checkDuplEmail = MutableLiveData<Boolean>()
    val checkDuplEmail: LiveData<Boolean>
        get() = _checkDuplEmail

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

    fun setJoinState(joinState: Boolean) {
        _joinState.value = joinState
    }

    fun joinMember(userEmail: String, userName: String, userPw: String) {
        viewModelScope.launch {
            runCatching {
                authService.joinMember(
                    JoinRequest(
                        userEmail
                        , userName
                        , userPw
                        , userImage.value!!
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


    fun joinExpert(userEmail: String, userPw: String, userName: String, expertIdentification: String
                   , expertDescription: String, expertAddress: String, expertPhone: String
                   , expertLicense: String, expertGetDate: String, expertCategory: MutableList<String>) {
        viewModelScope.launch {
            runCatching {
                authService.joinExpert(
                    JoinExpertRequest(
                        userEmail
                        , userPw
                        , userName
                        , userImage.value!!
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
}