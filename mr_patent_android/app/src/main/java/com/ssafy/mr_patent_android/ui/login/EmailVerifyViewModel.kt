package com.ssafy.mr_patent_android.ui.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.mr_patent_android.base.ApplicationClass.Companion.networkUtil
import com.ssafy.mr_patent_android.data.model.dto.PwdChangeRequest
import com.ssafy.mr_patent_android.data.model.dto.UserDto
import com.ssafy.mr_patent_android.data.remote.RetrofitUtil.Companion.authService
import kotlinx.coroutines.launch

private const val TAG = "EmailVerifyViewModel_Mr_Patent"
class EmailVerifyViewModel:ViewModel() {
    private val _toastMsg = MutableLiveData<String>()
    val toastMsg: LiveData<String>
        get() = _toastMsg

    private  val _codeState = MutableLiveData<Boolean>()
    val codeState: LiveData<Boolean>
        get() = _codeState

    private val _emailVerifyState = MutableLiveData<Boolean>()
    val emailVerifyState: LiveData<Boolean>
        get() = _emailVerifyState

    fun setCodeState(state: Boolean) {
        _codeState.value = state
    }

    fun setEmailVerifyState(state: Boolean) {
        _emailVerifyState.value = state
    }

    fun emailVerify(email: String, code: Int) {
        _emailVerifyState.value = false
        viewModelScope.launch {
            runCatching {
                authService.sendCodePwd(
                    PwdChangeRequest(email, code, null)
                )
            }.onSuccess {
                if (it.isSuccessful) {
                    Log.d(TAG, "emailVerify: $it")
                    _emailVerifyState.value = true
                } else {
                    it.errorBody()?.let { it1 ->
                        val res = networkUtil.getErrorResponse(it1)
                    }
                }
            }.onFailure {
            }
        }
    }

    fun sendCode(email: String) {
        // 코드 전송 요청
        viewModelScope.launch {
            runCatching {
                authService.sendForgotCode(PwdChangeRequest(email,null,null))
            }.onSuccess { 
                if (it.isSuccessful) {
                    _codeState.value = true
                    it.body()?.data?.let { response ->
                        _toastMsg.value = response.message
                    }
                } else {
                    it.errorBody()?.let { it1 ->
                        val res=networkUtil.getErrorResponse(it1)
                    }
                }
            }.onFailure {
                Log.d(TAG, "sendCode: failure")
            }
        }
    }
}