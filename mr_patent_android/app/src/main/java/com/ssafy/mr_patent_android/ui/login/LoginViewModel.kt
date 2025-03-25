package com.ssafy.mr_patent_android.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.mr_patent_android.base.ApplicationClass.Companion.networkUtil
import com.ssafy.mr_patent_android.base.ApplicationClass.Companion.sharedPreferences
import com.ssafy.mr_patent_android.data.model.dto.LoginRequest
import com.ssafy.mr_patent_android.data.model.dto.UserDto
import com.ssafy.mr_patent_android.data.remote.RetrofitUtil.Companion.authService
import kotlinx.coroutines.launch

class LoginViewModel:ViewModel() {
    private val _loginState = MutableLiveData<Boolean>()
    val loginState: LiveData<Boolean>
        get() = _loginState




    fun login(email: String, pwd: String) {
        // 로그인 요청
        viewModelScope.launch {
            runCatching {
                authService.login(LoginRequest(email, pwd))
            }.onSuccess {
                // 로그인 성공
                if (it.isSuccessful) {
                    // 로그인 성공
                    it.body()?.data?.let { res ->
                        // 토큰 저장
                        sharedPreferences.addAToken(res.accessToken)
                        sharedPreferences.addRToken(res.refreshToken)
                        // 사용자 정보 저장
                        sharedPreferences.addUser(UserDto(res.userId,res.userName,res.userRole))
                        _loginState.value = true
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