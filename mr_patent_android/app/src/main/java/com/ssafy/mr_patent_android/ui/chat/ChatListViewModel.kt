package com.ssafy.mr_patent_android.ui.chat

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.mr_patent_android.base.ApplicationClass.Companion.networkUtil
import com.ssafy.mr_patent_android.base.ApplicationClass.Companion.sharedPreferences
import com.ssafy.mr_patent_android.data.model.dto.ChatRoomDto
import com.ssafy.mr_patent_android.data.model.dto.LoginRequest
import com.ssafy.mr_patent_android.data.model.dto.UserDto
import com.ssafy.mr_patent_android.data.remote.RetrofitUtil.Companion.authService
import com.ssafy.mr_patent_android.data.remote.RetrofitUtil.Companion.chatService
import com.ssafy.mr_patent_android.data.remote.RetrofitUtil.Companion.userService
import kotlinx.coroutines.launch

private const val TAG = "ChatListViewModel"
class ChatListViewModel : ViewModel() {
    private val _chatRoomList = MutableLiveData<List<ChatRoomDto>>()
    val chatRoomList: LiveData<List<ChatRoomDto>>
        get() = _chatRoomList


    fun getChatRoomList(){
        viewModelScope.launch {
            runCatching {
                chatService.getChatRoomList(sharedPreferences.getUser().userId)
            }.onSuccess {
                if (it.isSuccessful) {
                    it.body()?.data?.let { chatRoomList ->
                        _chatRoomList.value = chatRoomList
                    }
                }else{
                    it.errorBody()?.let { it1 ->
                        networkUtil.getErrorResponse(it1)?.let { errorResponse ->
                            // 에러 처리
                        }
                    }
                }
            }.onFailure {
                it.printStackTrace()
                _chatRoomList.value = listOf(ChatRoomDto(1,1,"d","da","da","d","da",1))
                Log.d(TAG, "getChatRoomList: ${_chatRoomList.value}")
            }
        }
    }



}