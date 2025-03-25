package com.ssafy.mr_patent_android.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.mr_patent_android.base.ApplicationClass.Companion.networkUtil
import com.ssafy.mr_patent_android.data.model.dto.EmailCodeRequest
import com.ssafy.mr_patent_android.data.remote.RetrofitUtil.Companion.authService
import kotlinx.coroutines.launch

class EmailVerifyViewModel:ViewModel() {
    private  val _codeState = MutableLiveData<Boolean>()
    val codeState: LiveData<Boolean>
        get() = _codeState

    private val _emailVerifyState = MutableLiveData<Boolean>()
    val emailVerifyState: LiveData<Boolean>
        get() = _emailVerifyState


    fun setCodeState(state: Boolean) {
        _codeState.value = state
    }

    fun emailVerify(email: String, code: String) {
        // 이메일 인증 요청
        viewModelScope.launch {
            runCatching {
                authService.emailVerify(EmailCodeRequest(email, code))
            }.onSuccess {
                // 이메일 인증 성공
                if (it.isSuccessful) {
                    // 이메일 인증 성공
                    _emailVerifyState.value = true
                    it.body()?.data?.let { res ->
                        // 이메일 인증 성공
                    }
                } else {
                    it.errorBody()?.let { it1 ->
                        val res=networkUtil.getErrorResponse(it1)
                    }
                    // 이메일 인증 실패
                }

            }.onFailure {
                // 이메일 인증 실패
            }
        }
    }

    fun sendCode(email: String) {
        // 코드 전송 요청
        viewModelScope.launch {
            runCatching {
                authService.sendCode(email)
            }.onSuccess {
                // 코드 전송 성공
                if (it.isSuccessful) {
                    // 코드 전송 성공
                    it.body()?.data?.let { res ->
                        // 코드 전송 성공
                        _codeState.value = true
                    }
                } else {
                    it.errorBody()?.let {
                        it1 -> networkUtil.getErrorResponse(it1)
                    }
                    // 코드 전송 실패
                }

            }.onFailure {
                // 코드 전송 실패
            }
        }
    }
}