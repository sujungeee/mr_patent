package com.ssafy.mr_patent_android

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.mr_patent_android.base.ApplicationClass.Companion.networkUtil
import com.ssafy.mr_patent_android.base.ApplicationClass.Companion.sharedPreferences
import com.ssafy.mr_patent_android.data.model.dto.ChatRoomDto
import com.ssafy.mr_patent_android.data.model.dto.FcmRequest
import com.ssafy.mr_patent_android.data.remote.RetrofitUtil.Companion.authService
import kotlinx.coroutines.launch

private const val TAG = "MainViewModel"
class MainViewModel: ViewModel() {
    private val _networkState = MutableLiveData<Boolean>()
    val networkState: LiveData<Boolean>
        get() = _networkState

    fun setNetworkState(state: Boolean) {
        _networkState.postValue(state)
    }

    fun sendFcmToken(token: String) {
        viewModelScope.launch {
            runCatching {
                authService.sendFcmToken(FcmRequest(token, sharedPreferences.getUser().userId))
            }.onSuccess {
                if (it.isSuccessful) {
                } else {
                    it.errorBody()?.let {
                        networkUtil.getErrorResponse(it)
                    }
                }
            }.onFailure {
                Log.d(TAG, "sendFcmToken: $it")
            }
        }
    }

}