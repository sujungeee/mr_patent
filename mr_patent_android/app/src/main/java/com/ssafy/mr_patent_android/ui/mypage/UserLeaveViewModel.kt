package com.ssafy.mr_patent_android.ui.mypage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.mr_patent_android.base.ApplicationClass.Companion.sharedPreferences
import com.ssafy.mr_patent_android.data.model.dto.LogoutRequest
import com.ssafy.mr_patent_android.data.remote.RetrofitUtil.Companion.authService
import kotlinx.coroutines.launch

class UserLeaveViewModel : ViewModel() {
    private val _toastMsg = MutableLiveData<String>()
    val toastMsg: LiveData<String>
        get() = _toastMsg

    fun deleteUser() {
        viewModelScope.launch {
            runCatching {
                authService.deleteUser(sharedPreferences.getUser().userId)
            }.onSuccess {
                if (it.isSuccessful) {
                    it.body()?.let { response ->
                        if (response.data != null) {
                            _toastMsg.value = "탈퇴가 완료되었습니다."
                        }
                    }
                }
            }.onFailure {

            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            runCatching {
                authService.logout(
                    LogoutRequest(sharedPreferences.getUser().userId
                    , sharedPreferences.getRToken().toString())
                )
            }.onSuccess {
                if (it.isSuccessful) {
                    it.body()?.let { response ->
                        if (response.data != null) {
                            _toastMsg.value = "로그아웃 되었습니다."
                        }
                    }
                }
            }.onFailure {

            }
        }
    }
}