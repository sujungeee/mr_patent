package com.ssafy.mr_patent_android.ui.mypage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.mr_patent_android.base.ApplicationClass.Companion.sharedPreferences
import com.ssafy.mr_patent_android.data.model.dto.PwdEditRequest
import com.ssafy.mr_patent_android.data.remote.RetrofitUtil.Companion.authService
import kotlinx.coroutines.launch

class PwdEditViewModel : ViewModel() {
    private val _toastMsg = MutableLiveData<String>()
    val toastMsg: LiveData<String>
        get() = _toastMsg

    fun pwdEdit(currentPwd: String, newPwd: String) {
        viewModelScope.launch {
            runCatching {
                authService.pwdEdit(PwdEditRequest(currentPwd, newPwd))
            }.onSuccess {
                if (it.isSuccessful) {
                    it.body()?.let { response ->
                        if (response.data != null) {
                            _toastMsg.value = "비밀번호가 변경되었습니다."
                        }
                    }
                }
            }.onFailure {

            }
        }
    }
}