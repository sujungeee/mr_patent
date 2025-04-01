package com.ssafy.mr_patent_android.ui.chat

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ssafy.mr_patent_android.base.ApplicationClass.Companion.networkUtil
import com.ssafy.mr_patent_android.base.ApplicationClass.Companion.sharedPreferences
import com.ssafy.mr_patent_android.data.model.dto.ChatMessageDto
import com.ssafy.mr_patent_android.data.model.dto.ChatRoomDto
import com.ssafy.mr_patent_android.data.model.dto.UserDto
import com.ssafy.mr_patent_android.data.remote.RetrofitUtil.Companion.chatService
import com.ssafy.mr_patent_android.data.remote.RetrofitUtil.Companion.fileService
import com.ssafy.mr_patent_android.data.remote.RetrofitUtil.Companion.userService
import com.ssafy.mr_patent_android.util.FileUtil
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ua.naiksoftware.stomp.StompClient
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private const val TAG = "ChatViewModel"
class ChatViewModel : ViewModel() {
    private val _messageList = MutableLiveData<List<ChatMessageDto>>()
    val messageList: LiveData<List<ChatMessageDto>>
        get() = _messageList

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


    private val _sendState = MutableLiveData<Boolean>()
    val sendState: LiveData<Boolean>
        get() = _sendState

    fun setSendState(state:Boolean){
        _sendState.value = state
    }

    fun setUser(user: UserDto){
        _user.value = user
    }

    fun addMessage(message: ChatMessageDto) {
        val currentList = _messageList.value?.toMutableList() ?: mutableListOf()

        currentList.add(message)

        _messageList.value = currentList
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

    fun sendMessage(roomId: String, message: String?, files: List<ChatMessageDto.Files>?, context: Context, stompClient: StompClient) {
        var fileExtension="TEXT"
        viewModelScope.launch {
            val uploadedFiles = mutableListOf<String>() // 업로드된 파일 URL 저장

            if (!files.isNullOrEmpty()) {
                // 파일 업로드 처리
                files.forEach { file ->
                    fileExtension = FileUtil().getFileExtension(context, file.fileUri) ?: return@forEach
                    val fileName = FileUtil().getFileName(context, file.fileUri) ?: "unknown_file"

                    try {
                        val response = fileService.getPreSignedUrl(fileName, fileExtension)
                        val presignedUrl = response.body()?.data

                        Log.d("Presigned URL", "URL: $presignedUrl")

                        val success =
                            presignedUrl?.let {
                                uploadFileToS3(context, file.fileUri, it, fileExtension, fileName) }

                        if (success == true) {
                            Log.d("S3 Upload", "File uploaded successfully!")
                            file.fileUrl = presignedUrl // 업로드된 파일 경로 저장

                            if (fileExtension == "pdf") {
                                // STOMP를 통해 업로드된 파일 정보 전송
                                fileExtension="PDF"
                            } else if (fileExtension == "doc" || fileExtension == "docx") {
                                // STOMP를 통해 메시지만 전송
                                fileExtension="WORD"
                            }

                            // STOMP를 통해 업로드된 파일 정보 전송
                            sendStompMessage(stompClient, roomId, null, file, fileExtension)

                        } else {
                            Log.e("S3 Upload", "File upload failed!")
                        }

                    } catch (e: Exception) {
                        Log.e("Presigned URL Error", "Failed to get presigned URL", e)
                    }
                }


            } else if (!message.isNullOrBlank()) {
                // STOMP를 통해 메시지만 전송
                sendStompMessage(stompClient, roomId, message, null, fileExtension)
            }
        }
    }


    suspend fun uploadFileToS3(context: Context, fileUri: Uri, presignedUrl: String, fileType: String, fileName:String): Boolean {
        return suspendCoroutine { continuation ->
            viewModelScope.launch {
                runCatching {
                    val file = FileUtil().getFileFromUri(context, fileUri, fileName, fileType)
                    var requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    when(fileType){
                        "image" -> {
                            requestBody = file.asRequestBody("image/jpeg".toMediaType())
                        }
                        "pdf" -> {
                            requestBody = file.asRequestBody("application/pdf".toMediaType())
                        }
                        "doc" -> {
                            requestBody = file.asRequestBody("application/msword".toMediaType())
                        }
                        "docx" -> {
                            requestBody = file.asRequestBody("application/vnd.openxmlformats-officedocument.wordprocessingml.document".toMediaType())
                        }
                    }

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

    fun sendStompMessage(stompClient: StompClient, roomId: String, message: String?, file: ChatMessageDto.Files?, fileType:String) {
        val chatMessage=ChatMessageDto(
            chatId = null,
            isRead = false,
            message = message,
            receiverId = null,
            roomId = roomId,
            timestamp = null,
            type = "CHAT",
            userId = sharedPreferences.getUser().userId,
            fileName = file?.fileName,
            fileUrl = file?.fileUrl,
            fileUri = Uri.EMPTY,
            messageType = fileType
        )


        val jsonMessage = Gson().toJson(chatMessage)
        try {
            stompClient.send("/chat/$roomId/send", jsonMessage).subscribe()
            Log.d(TAG, "sendStompMessage: $jsonMessage")
//            _messageList.value = _messageList.value?.plus(chatMessage)
            _sendState.value = true
            }catch (e:Exception){
                Log.d("ChatViewModel", "sendStompMessage: ${e.message}")
            }
    }




    fun getMessageList(roomId: String, lastId: Int? = null) {
        viewModelScope.launch {
            runCatching {
                if (lastId == null) {
                    chatService.getFirstChatMessageList(roomId)
                } else {
                    chatService.getChatMessageList(roomId, lastId)
                }
            }.onSuccess {
                if (it.isSuccessful) {
                    it.body()?.data?.let { messageList ->
                        val currentMessages = _messageList.value?.toMutableList() ?: mutableListOf()

                        messageList.forEach {
                            // 타임스탬프가 다르면 구분선 추가
                            val lastTime = currentMessages.lastOrNull()?.timestamp
                            if (lastTime != it.timestamp) {
                                currentMessages.add(ChatMessageDto(messageType = "DIVIDER"))
                            }

                            // 메시지를 리스트에 추가
                            currentMessages.add(it)
                        }

                        // 변경된 리스트로 LiveData 갱신
                        _messageList.value = currentMessages
                    }
                } else {
                    it.errorBody()?.let { error ->
                        networkUtil.getErrorResponse(error)?.let { errorResponse ->
                            Log.d("ChatViewModel", "getMessageList: $errorResponse")
                        }
                    }
                }
            }.onFailure {
                it.printStackTrace()
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
                        setUser(user)
                    }
                }
            }
        }

    }
}