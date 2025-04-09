package com.ssafy.mr_patent_android.ui.chat

import android.content.Context
import android.net.Uri
import android.util.Log
import android.util.TimeFormatException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.places.api.model.LocalDate
import com.google.android.libraries.places.api.model.kotlin.localDate
import com.google.android.libraries.places.api.model.kotlin.localTime
import com.google.gson.Gson
import com.ssafy.mr_patent_android.base.ApplicationClass.Companion.networkUtil
import com.ssafy.mr_patent_android.base.ApplicationClass.Companion.sharedPreferences
import com.ssafy.mr_patent_android.data.model.dto.ChatHeartBeat
import com.ssafy.mr_patent_android.data.model.dto.ChatMessageDto
import com.ssafy.mr_patent_android.data.model.dto.ChatRoomDto
import com.ssafy.mr_patent_android.data.model.dto.UserDto
import com.ssafy.mr_patent_android.data.remote.RetrofitUtil.Companion.chatService
import com.ssafy.mr_patent_android.data.remote.RetrofitUtil.Companion.fileService
import com.ssafy.mr_patent_android.data.remote.RetrofitUtil.Companion.userService
import com.ssafy.mr_patent_android.util.FileUtil
import com.ssafy.mr_patent_android.util.TimeUtil
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.StompHeader
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private const val TAG = "ChatViewModel"
class ChatViewModel : ViewModel() {
    private val _messageList = MutableLiveData<List<ChatMessageDto>>()
    val messageList: LiveData<List<ChatMessageDto>>
        get() = _messageList

    private val _dateChanged = MutableLiveData<Boolean>(false)
    val dateChanged: LiveData<Boolean>
        get() = _dateChanged

    fun setDateChanged(dateChanged: Boolean) {
        _dateChanged.value = dateChanged
    }

    private val _roomId = MutableLiveData<Int>()
    val roomId: LiveData<Int>
        get() = _roomId

    private val _user = MutableLiveData<UserDto>()
    val user: LiveData<UserDto>
        get() = _user

    private val _file = MutableLiveData<Uri?>()
    val file: LiveData<Uri?>
        get() = _file

    private val _image = MutableLiveData<List<ChatMessageDto.Files>?>()
    val image: LiveData<List<ChatMessageDto.Files>?>
        get() = _image


    private val _state = MutableLiveData<Boolean>(false)
    val state: LiveData<Boolean>
        get() = _state

    private val _isSend = MutableLiveData<Boolean>(false)
    val isSend: LiveData<Boolean>
        get() = _isSend

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean>
        get() = _isLoading


    fun setIsLoading(isLoading: Boolean) {
        _isLoading.value= isLoading
    }

    fun setIsSend(isSend: Boolean) {
        _isSend.postValue(isSend)
    }


    fun setState(newState: Boolean) {
        _state.postValue(newState)
    }

    fun setUser(user: UserDto){
        _user.value = user
    }

    fun addMessage(message: ChatMessageDto) {
        val currentList = _messageList.value?.toMutableList() ?: mutableListOf()
        Log.d(TAG, "addMessage: $message, ${currentList}")
        if (currentList.isNullOrEmpty()) {
            Log.d(TAG, "addMessage: 빈리스트")
            currentList.add(
                ChatMessageDto(
                    chatId = 0,
                    timestamp = TimeUtil().parseUtcWithJavaTime(message.timestamp!!).toLocalDate().toString(),
                    messageType = "DIVIDER"
                )
            )
        }else{
            Log.d(TAG, "addMessage: ${currentList.first()}")
            val lastTime = TimeUtil().parseUtcWithJavaTime(currentList.first().timestamp!!)
            val cur = TimeUtil().parseUtcWithJavaTime(message.timestamp!!)
            Log.d(TAG, "addMessage: $lastTime, $cur")


            if (cur != null && cur != lastTime) {
                _dateChanged.postValue(true)
                currentList.add(
                    ChatMessageDto(
                        chatId = 0,
                        timestamp = TimeUtil().parseUtcWithJavaTime(message.timestamp!!).toLocalDate().toString(),
                        messageType = "DIVIDER"
                    )
                )
            }
        }
        currentList.add(0, message)

        _messageList.postValue(currentList)
    }


    fun addMessageFront(message: ChatMessageDto) {
        val currentList = _messageList.value?.toMutableList() ?: mutableListOf()

        currentList.add(0, message)

        _messageList.value = currentList
    }


    fun setFile(file:Uri?){
        _file.value = file
    }
    fun addImage(image:ChatMessageDto.Files){
        _image.value = (_image.value?: listOf()).plus(image)
    }

    fun deleteImage(){
        _image.value = null
    }

    fun removeImage(image:Int){
        _image.value = _image.value?.filterIndexed { index, _ -> index != image }
    }

    fun sendMessage(receiverId:Int,roomId: String, message: String?, files: List<ChatMessageDto.Files>?, context: Context, stompClient: StompClient) {
       var fileType = "TEXT"
        viewModelScope.launch {
            val uploadedFiles = mutableListOf<String>() // 업로드된 파일 URL 저장
            if (!files.isNullOrEmpty()) {
                // 파일 업로드 처리
                files.forEach { file ->
                    fileType = FileUtil().getFileExtension(context, file.fileUri) ?: return@forEach
                    val fileExtension = FileUtil().getMimeType(fileType)
                    val fileName = FileUtil().getFileName(context, file.fileUri) ?: "unknown_file"
                    try {
                        val response = fileService.getPreSignedUrl(fileName, fileExtension.toString())
                        val presignedUrl = response.body()?.data

                        Log.d("Presigned URL", "URL: $presignedUrl")

                        val success =
                            presignedUrl?.let {
                                uploadFileToS3(context, file.fileUri, it, fileType, fileName) }

                        if (success == true) {
                            Log.d("S3 Upload", "File uploaded successfully!")
                            file.fileUrl = presignedUrl

                            file.fileName= fileName

                            sendStompMessage(receiverId,stompClient, roomId, null, file, FileUtil().formatType(fileType))

                        } else {
                            Log.e("S3 Upload", "File upload failed!")
                        }

                    } catch (e: Exception) {
                        Log.e("Presigned URL Error", "Failed to get presigned URL", e)
                    }
                }


            } else if (!message.isNullOrBlank()) {
                // STOMP를 통해 메시지만 전송
                sendStompMessage(receiverId,stompClient, roomId, message, null, fileType)
            }
        }
    }


    suspend fun uploadFileToS3(context: Context, fileUri: Uri, presignedUrl: String, fileType: String, fileName:String): Boolean {
        return suspendCoroutine { continuation ->
            viewModelScope.launch {
                runCatching {
                    val file = FileUtil().getFileFromUri(context, fileUri, fileName, fileType)
                    var requestBody = file.asRequestBody(FileUtil().getMimeType(fileType))

                    fileService.uploadFile(presignedUrl, requestBody)
                }.onSuccess {
                    if (it.isSuccessful) {
                        Log.d(TAG, "uploadImgS3: 성공><")
                        continuation.resume(true)
                    } else {
                        Log.d(TAG, "uploadImgS3: 실패><")
                        continuation.resumeWithException(Exception("업로드 실패"))
                    }
                }.onFailure {
                    it.printStackTrace()
                    continuation.resumeWithException(it)
                }
            }
        }
    }

    fun sendStompMessage(receiverId: Int,stompClient: StompClient, roomId: String, message: String?, file: ChatMessageDto.Files?, fileType:String) {
        val chatMessage = ChatMessageDto(
            status = 0,
            chatId = null,
            isRead = state.value?: false,
            message = message,
            receiverId = receiverId,
            roomId = roomId,
            timestamp = null,
            type = "CHAT",
            userId = sharedPreferences.getUser().userId,
            fileName = file?.fileName,
            fileUrl = file?.fileUrl,
            fileUri = file?.fileUri,
            messageType = fileType
        )


        val jsonMessage = Gson().toJson(chatMessage)
        Log.d(TAG, "sendStompMessage: $jsonMessage")
        try {
            stompClient.send("/pub/chat/message", jsonMessage).subscribe()
        } catch (e: Exception) {
        }
    }

    fun sendStompHeartBeat(stompClient: StompClient, roomId: String) {
       val heartBeat = ChatHeartBeat(
            roomId = roomId,
            type = "PING",
            userId = sharedPreferences.getUser().userId
        )

        val jsonMessage = Gson().toJson(heartBeat)
        Log.d(TAG, "sendStompMessage: $jsonMessage")
        try {
            stompClient.send("/pub/chat/heartbeat", jsonMessage).subscribe()
        } catch (e: Exception) {
        }
    }




    fun getMessageList(roomId: String, lastId: Int? = null) {
        if(_isLoading.value == true) return
        _isLoading.value = true
        viewModelScope.launch {
            runCatching {
                if (lastId == null) {
                    chatService.getFirstChatMessageList(roomId)
                } else {
                    chatService.getChatMessageList(roomId, lastId)
                }
            }.onSuccess {
                if (it.isSuccessful) {
                    it.body()?.data?.let { messageLists ->
                        val currentMessages = _messageList.value?.toMutableList() ?: mutableListOf()
                        if(messageList.value?.lastOrNull()?.messageType == "DIVIDER"){
                            return@launch
                        }else if (messageLists.isEmpty()) {
                            Log.d(TAG, "getMessageList: 리스트 비어있음")
                            messageList.value?.lastOrNull()?.let { lastMessage ->
                                currentMessages.add(
                                    ChatMessageDto(
                                        chatId = lastMessage.chatId,
                                        timestamp = TimeUtil().parseUtcWithJavaTime(lastMessage.timestamp ?: "2001-08-23T00:00:00Z").toLocalDate().toString(),
                                        messageType = "DIVIDER"
                                    )
                                )
                            }
                        }else{

                        var lastTime = TimeUtil().parseUtcWithJavaTime(messageLists.first()?.timestamp?:"2001-08-23T00:00:00Z").toLocalDate()

                        messageLists.forEach {message->
                            // 타임스탬프가 다르면 구분선 추가

                            val cur = TimeUtil().parseUtcWithJavaTime(message.timestamp ?: return@forEach)?.toLocalDate()

                            // 날짜가 바뀌었으면 구분선 추가
                            if (cur != null && cur != lastTime) {
                                currentMessages.add(
                                    ChatMessageDto(
                                        timestamp = lastTime.toString(),
                                        messageType = "DIVIDER"
                                    )
                                )
                                lastTime = cur
                            }
                            currentMessages.add(message)
                            Log.d(TAG, "getMessageList: ${message}")
                        }
                            }


                        // 변경된 리스트로 LiveData 갱신
                        _messageList.value = currentMessages
                        _isLoading.value = false
                    }
                    _isLoading.value = false
                } else {
                    it.errorBody()?.let { error ->
                        networkUtil.getErrorResponse(error)?.let { errorResponse ->
                            Log.d("ChatViewModel", "gwetMessageList: $errorResponse")
                        }
                    }
                    _isLoading.value = false
                }
            }.onFailure {
                it.printStackTrace()
                _isLoading.value = false
                Log.d("ChatViewModel", "getMessageList: ${it.message}")
            }
        }
    }

    fun getUser(expertId:Int){
        viewModelScope.launch {
            runCatching {
                userService.getExpert(expertId)
            }.onSuccess {
                if(it.isSuccessful){
                    it.body()?.data?.let { user ->
                        user.expertCategory.forEach { category ->
                            user.category.add(category.categoryName)
                        }
                        setUser(user)
                    }
                }
            }
        }

    }
    fun setMessageList(message: List<ChatMessageDto>){
        _messageList.value = message
    }
}