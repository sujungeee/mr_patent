package com.ssafy.mr_patent_android.ui.chat

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.mr_patent_android.base.ApplicationClass.Companion.networkUtil
import com.ssafy.mr_patent_android.data.model.dto.ChatMessageDto
import com.ssafy.mr_patent_android.data.model.dto.ChatRoomDto
import com.ssafy.mr_patent_android.data.remote.RetrofitUtil.Companion.chatService
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val _messageList = MutableLiveData<List<ChatMessageDto>>()
    val messageList: LiveData<List<ChatMessageDto>>
        get() = _messageList

    private val _roomId = MutableLiveData<Int>()
    val roomId: LiveData<Int>
        get() = _roomId


    fun getMessageList(roomId:Int, lastId:Int? = null){
        viewModelScope.launch {
            runCatching {
                chatService.getChatMessageList(roomId, lastId)
            }.onSuccess {
                var lastTime= ""
                if (it.isSuccessful) {
                    it.body()?.data?.let { messageList ->
                        messageList.forEach {
                            if(it.timestamp != lastTime){
                                lastTime = it.timestamp
                                _messageList.value?.plus(
                                    ChatMessageDto(messageType = "DIVIDER")
                                )
                            }
                            _messageList.value = _messageList.value?.plus(it)
                        }
                    }
                }else{
                    it.errorBody()?.let { error ->
                        networkUtil.getErrorResponse(error)?.let { errorResponse ->
                            Log.d("ChatViewModel", "getMessageList: ${errorResponse}")
                    }

                }
            }
            }.onFailure {
                it.printStackTrace()
                Log.d("ChatViewModel", "getMessageList: ${it.message}")
            }
        }
    }


}