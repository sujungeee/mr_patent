package com.ssafy.mr_patent_android.ui.mypage

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.mr_patent_android.base.ApplicationClass.Companion.networkUtil
import com.ssafy.mr_patent_android.base.ApplicationClass.Companion.sharedPreferences
import com.ssafy.mr_patent_android.data.model.dto.ProfileEditRequest
import com.ssafy.mr_patent_android.data.model.dto.UserDto
import com.ssafy.mr_patent_android.data.remote.RetrofitUtil.Companion.authService
import com.ssafy.mr_patent_android.data.remote.RetrofitUtil.Companion.userService
import kotlinx.coroutines.launch

private const val TAG = "ProfileEditViewModel_Mr_Patent"
class ProfileEditViewModel : ViewModel() {
    private val _toastMsg = MutableLiveData<String?>()
    val toastMsg: LiveData<String?>
        get() = _toastMsg

    private val _profileImage = MutableLiveData<String>()
    val profileImage: LiveData<String>
        get() = _profileImage

    private val _currentImage = MutableLiveData<String>()
    val currentImage: LiveData<String>
        get() = _currentImage

    private val _id = MutableLiveData<Int>()
    val id: LiveData<Int>
        get() = _id

    private val _expertInfo = MutableLiveData<UserDto>()
    val expertInfo: LiveData<UserDto>
        get() = _expertInfo

    private val _memberInfo = MutableLiveData<UserDto>()
    val memberInfo: LiveData<UserDto>
        get() = _memberInfo

    private val _editState = MutableLiveData<Boolean>()
    val editState: LiveData<Boolean>
        get() = _editState

    fun setProfileImage(uri: String) {
        _profileImage.value = uri
    }

    fun setCurrentImage(uri: String) {
        _currentImage.value = uri
    }

    fun clearToastMsg() {
        _toastMsg.value = null
    }

    fun getExpertInfo(id: Int) {
        viewModelScope.launch {
            runCatching {
                userService.getExpert(id)
            }.onSuccess {
                if (it.isSuccessful) {
                    it.body()?.data?.let { response ->
                        _expertInfo.value = response
                        _profileImage.value = response.userImage
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

    fun getMemberInfo() {
        viewModelScope.launch {
            runCatching {
                userService.getMember()
            }.onSuccess {
                if (it.isSuccessful) {
                    it.body()?.data?.let { response ->
                        _memberInfo.value = response
                        _profileImage.value = response.userImage
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

    fun editUserInfo(profileEditRequest: ProfileEditRequest) {
        viewModelScope.launch {
            runCatching {
                userService.editUserInfo(profileEditRequest)
            }.onSuccess {
                if (it.isSuccessful) {
                    it.body()?.data?.let { response ->
                        _toastMsg.value = response.message
                        val user = sharedPreferences.getUser()
                        sharedPreferences.addUser(
                            UserDto(
                                user.userId, profileEditRequest.userName ?: user.userName , user.userRole
                            )
                        )
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