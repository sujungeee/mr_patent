package com.ssafy.mr_patent_android.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.mr_patent_android.base.ApplicationClass.Companion.networkUtil
import com.ssafy.mr_patent_android.data.model.dto.PwdChangeRequest
import com.ssafy.mr_patent_android.data.remote.RetrofitUtil.Companion.authService
import kotlinx.coroutines.launch

class PwdChangeViewModel:ViewModel() {
    private val _changeState = MutableLiveData<Boolean>()
    val changeState: LiveData<Boolean>
        get() = _changeState




    fun changePwd(pwdChangeRequest: PwdChangeRequest) {
        // 로그인 요청
        viewModelScope.launch {
            runCatching {
                authService.changePassword(pwdChangeRequest)
            }.onSuccess {
                // 로그인 성공
                if (it.isSuccessful) {
                    // 로그인 성공
                    it.body()?.data?.let { res ->
                        // 토큰 저장
                        _changeState.value = true
                    }
                } else {
                    it.errorBody()?.let {
                        it1 -> networkUtil.getErrorResponse(it1)
                    }
                    // 로그인 실패
                }

            }.onFailure {
                // 로그인 실패
            }
        }
    }
}