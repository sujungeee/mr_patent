package com.ssafy.mr_patent_android.ui.patent_attorney

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.mr_patent_android.base.ApplicationClass.Companion.networkUtil
import com.ssafy.mr_patent_android.base.ApplicationClass.Companion.sharedPreferences
import com.ssafy.mr_patent_android.data.model.dto.LoginRequest
import com.ssafy.mr_patent_android.data.model.dto.UserDto
import com.ssafy.mr_patent_android.data.remote.RetrofitUtil.Companion.authService
import com.ssafy.mr_patent_android.data.remote.RetrofitUtil.Companion.userService
import kotlinx.coroutines.launch

class ExpertViewModel:ViewModel() {
    private val _expert = MutableLiveData<UserDto>()
    val expert: LiveData<UserDto>
        get() = _expert


    fun getExpert(id: Int){
        viewModelScope.launch {
            runCatching {
                userService.getExpert(id)
            }.onSuccess {
                if(it.isSuccessful){
                    it.body()?.data?.let { expert ->
                        _expert.value = expert
                    }
                }
            }.onFailure {
                it.printStackTrace()
            }
        }
    }


}