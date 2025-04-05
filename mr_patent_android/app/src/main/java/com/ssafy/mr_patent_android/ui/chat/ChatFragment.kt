package com.ssafy.mr_patent_android.ui.chat

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBar.LayoutParams
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.base.ApplicationClass
import com.ssafy.mr_patent_android.base.ApplicationClass.Companion.sharedPreferences
import com.ssafy.mr_patent_android.base.BaseFragment
import com.ssafy.mr_patent_android.base.XAccessTokenInterceptor
import com.ssafy.mr_patent_android.data.model.dto.ChatMessageDto
import com.ssafy.mr_patent_android.data.model.dto.UserDto
import com.ssafy.mr_patent_android.databinding.DialogChatProfileBinding
import com.ssafy.mr_patent_android.databinding.FragmentChatBinding
import com.ssafy.mr_patent_android.databinding.FragmentChatBottomSheetBinding
import com.ssafy.mr_patent_android.databinding.ItemPhotoPreviewBinding
import com.ssafy.mr_patent_android.util.FileUtil
import gun0912.tedimagepicker.builder.TedImagePicker
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.OkHttpClient.*
import okhttp3.internal.http2.Header
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompHeader
import java.util.concurrent.TimeUnit


private const val TAG = "ChatFragment"

class ChatFragment :
    BaseFragment<FragmentChatBinding>(FragmentChatBinding::bind, R.layout.fragment_chat) {
//    private lateinit var messageListAdapter: MessageListAdapter
    val viewModel: ChatViewModel by viewModels()
    private val roomId: String by lazy {
        navArgs<ChatFragmentArgs>().value.roomId
    }
    private val userId: Int by lazy {
        navArgs<ChatFragmentArgs>().value.userId
    }
    private val expertId: Int by lazy {
        navArgs<ChatFragmentArgs>().value.expertId
    }
    private val userName: String by lazy {
        navArgs<ChatFragmentArgs>().value.userName
    }
    private val userImage: String by lazy {
        navArgs<ChatFragmentArgs>().value.userImage
    }
    lateinit var stompClient: StompClient

    val url = "wss://j12d208.p.ssafy.io/ws/chat"
    var headerList: MutableList<StompHeader> = mutableListOf()
    var modalBottomSheet: BottomSheetDialog? = null


    @SuppressLint("CheckResult")
    fun initStomp() {

        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, url)
        headerList.add(StompHeader("Authorization", "Bearer ${sharedPreferences.getAToken()}"))
        headerList.add(StompHeader("roomId", roomId))
        headerList.add(StompHeader("userId", sharedPreferences.getUser().userId.toString()))

        stompClient.lifecycle().subscribe { lifecycleEvent ->
            when (lifecycleEvent.type) {
                LifecycleEvent.Type.OPENED -> {
                    Log.i("OPEND", "스톰프 연결 성공")
                    stompClient.topic("/sub/chat/room/" + roomId, headerList)
                        .subscribe({ topicMessage ->
                            try {
                                Log.d(TAG,"구독스${topicMessage.getPayload()}")
                                val message = Gson().fromJson(
                                    topicMessage.getPayload(),
                                    ChatMessageDto::class.java
                                )

                                Log.d(TAG, "initStomp: $message")
                                if (message.type == "ENTER") {
                                    if (message.status==2){
                                        viewModel.setState(true)
                                    }
                                    else{
                                        viewModel.setState(false)
                                    }
                                }else if (message.type == "LEAVE") {
                                    viewModel.setState(false)
                                }else {
                                    viewModel.addMessage(message)
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to process message", e)
                            }
                        }, { error ->
                            Log.e(
                                "SUBSCRIPTION_ERROR",
                                "Topic subscription error: ${error.message}"
                            )
                        })

                }

                LifecycleEvent.Type.CLOSED -> {
                    Log.i("CLOSED", "스톰프 연결 종료")

                }

                LifecycleEvent.Type.ERROR -> {
                    Log.i("ERROR", "스톰프 연결 에러")

                    Log.e("CONNECT ERROR", lifecycleEvent.exception.toString())
                }

                else -> {
                    Log.i("ELSE", lifecycleEvent.message)
                }
            }
        }
        stompClient.withClientHeartbeat(10000) // 10초마다 heartbeat 보내기
        stompClient.withServerHeartbeat(10000) // 10초마다 서버 heartbeat 받기


        stompClient.connect(headerList)


    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initBottomSheet()
        initStomp()
        initObserver()
    }

    fun initObserver() {
        // 어댑터를 한 번만 생성
//        messageListAdapter = MessageListAdapter(
//            UserDto(userName, userImage),
//            listOf(),
//            object : MessageListAdapter.ItemClickListener {
//                override fun onItemClick() {
//                    Log.d(TAG, "onFileClick: $expertId")
//                    if (expertId != -1) {
//                        initUserDialog(expertId)
//                    }
//                }
//
//                override fun onFileClick(url: String) {
//
//
//                }
//
//                override fun onPhotoClick(url: String) {
//                    initDialog(url)
//                }
//            })

        // RecyclerView에 어댑터 설정 (한 번만)
//        binding.rvChat.adapter = messageListAdapter

        viewModel.messageList.observe(viewLifecycleOwner) {
            Log.d(TAG, "initObserver: $it")
//            messageListAdapter.updateMessages(it)
//            binding.rvChat.scrollToPosition(0)
            binding.rvChat.adapter = MessageListAdapter(
                UserDto(userName, userImage),
                it,
                object : MessageListAdapter.ItemClickListener {
                    override fun onItemClick() {
                        Log.d(TAG, "onFileClick: $expertId")
                        if (expertId != -1) {
                            initUserDialog(expertId)
                        }
                    }

                    override fun onFileClick(url: String) {


                    }

                    override fun onPhotoClick(url: String) {
                        initDialog(url)
                    }
                })
        }

    }

    fun initView() {
        viewModel.getMessageList(roomId)
        binding.btnSend.isEnabled = false
        binding.tvChatHeader.text = userName
        binding.etMessage.addTextChangedListener {
            if (it.toString().isNotEmpty()) {
                binding.btnSend.isEnabled = true
            } else {
                binding.btnSend.isEnabled = false
            }
        }

        binding.btnSend.setOnClickListener {
            viewModel.sendMessage(
                userId,
                roomId,
                binding.etMessage.text.toString(),
                null,
                requireContext(),
                stompClient
            )

            binding.etMessage.text.clear()
        }

        binding.rvChat.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (!binding.rvChat.canScrollVertically(-1)
                    && newState == RecyclerView.SCROLL_STATE_IDLE
                ) {
                    /**
                     * newState가 SCROLL_STATE_IDLE를 확인하여 중복 발생을 방지한다.
                     */
//                    viewModel.getMessageList(roomId, viewModel.messageList.value?.last()?.chatId)
                }
            }
        })


    }

    fun initDialog(urls: String) {
        val dialog = Dialog(requireContext())
        val dialogBinding = ItemPhotoPreviewBinding.inflate(layoutInflater)

        val wrapper = FrameLayout(requireContext())
        wrapper.setPadding(200, 200, 200, 200)
        wrapper.addView(dialogBinding.root)

        dialog.setContentView(wrapper)

        dialog.window?.setLayout(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        Glide.with(requireContext())
            .load(urls)
            .fallback(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .into(dialogBinding.previewImage)

//        dialogBinding.btnDelete.visibility = View.VISIBLE
        dialogBinding.previewImage.setOnClickListener {
            dialog.dismiss()
        }


        dialog.show()

    }

    fun initUserDialog(expertId: Int) {
        val dialog = Dialog(requireContext())

        val dialogBinding = DialogChatProfileBinding.inflate(layoutInflater)

        viewModel.user.observe(viewLifecycleOwner) {
            dialogBinding.tvName.text = it.userName
            dialogBinding.tvDescription.text = it.expertDescription
            dialogBinding.tvAddress.text = it.expertAddress
            dialogBinding.tvPhone.text = it.expertPhone

            Glide.with(requireContext())
                .load(it.userImage)
                .into(dialogBinding.ivProfile)


            if (it.expertCategory.isNotEmpty()) {
                it.expertCategory.forEach { category ->
                    when (category) {
                        "기계공학" -> dialogBinding.tvFieldMecha.visibility = View.VISIBLE
                        "전기/전자" -> dialogBinding.tvFieldElec.visibility = View.VISIBLE
                        "화학공학" -> dialogBinding.tvFieldChemi.visibility = View.VISIBLE
                        "생명공학" -> dialogBinding.tvFieldLife.visibility = View.VISIBLE
                    }
                }
            }
        }
        viewModel.getUser(expertId)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogBinding.btnClose.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()


    }

    fun initBottomSheet() {
        val bottomBinding = FragmentChatBottomSheetBinding.inflate(layoutInflater)
// modal bottom sheet 객체 생성
        modalBottomSheet = BottomSheetDialog(requireContext())
// layout 파일 설정
        modalBottomSheet?.setContentView(bottomBinding.root)


        bottomBinding.btnDown.setOnClickListener {
            modalBottomSheet?.dismiss()
        }

        bottomBinding.btnGallery.setOnClickListener {
        TedImagePicker.with(requireContext())
            .max(5, "사진은 최대 5장까지 선택 가능합니다.")
            .showCameraTile(false)
            .startMultiImage { uriList ->
                uriList.forEach { uri ->
                    if (FileUtil().isFileSizeValid(requireContext(), uri)) {
                        viewModel.addImage(ChatMessageDto.Files("", "", uri))
                    }
                }
            }

    }

        bottomBinding.btnFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                putExtra(
                    Intent.EXTRA_MIME_TYPES,
                    arrayOf(
                        "application/pdf",
                        "application/msword",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                    )
                )
            }
            startActivityForResult(intent, 1)
        }

        viewModel.file.observe(viewLifecycleOwner) {
            Log.d(TAG, "initBottomSheet: $it")
            if (it != null) {
                bottomBinding.btnSend.isEnabled = true

                bottomBinding.previewImage.btnDelete.visibility = View.VISIBLE
                bottomBinding.previewImage.btnDelete.setOnClickListener {
                    viewModel.setFile(null)
                }
                bottomBinding.chatBottomSheetMenu.visibility = View.GONE
                bottomBinding.previewImage.root.visibility = View.VISIBLE
                FileUtil().getFileExtension(requireContext(), it)?.let { it1 ->
                    if (it1 == "pdf") {
                        bottomBinding.previewImage.previewImage.setImageResource(R.drawable.pdf_icon)
                    } else if (it1 == "doc" || it1 == "docx") {
                        bottomBinding.previewImage.previewImage.setImageResource(R.drawable.word_icon)
                    }
                }
                bottomBinding.previewImage.tvFileSize.text =
                    FileUtil().formatFileSize(FileUtil().getFileSize(requireContext(), it))
                bottomBinding.previewImage.tvFileName.text =
                    FileUtil().getFileName(requireContext(), it)
            } else {

                bottomBinding.btnSend.isEnabled = false
                bottomBinding.chatBottomSheetMenu.visibility = View.VISIBLE
                bottomBinding.previewImage.root.visibility = View.GONE
            }
        }

        bottomBinding.btnSend.setOnClickListener {
            val file = viewModel.file.value
            val image = viewModel.image.value
            val files = mutableListOf<ChatMessageDto.Files>()
            if (file != null) {
                files.add(
                    ChatMessageDto.Files(
                        FileUtil().getFileName(requireContext(), file)!!,
                        "",
                        file
                    )
                )
            } else {
                if (image != null) {
                    files.addAll(image)
                }

            }
            viewModel.sendMessage(
                userId,
                roomId,
                binding.etMessage.text.toString(),
                files,
                requireContext(),
                stompClient
            )
            viewModel.setFile(null)
            viewModel.deleteImage()
        }



        viewModel.image.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                bottomBinding.btnSend.isEnabled = true
                bottomBinding.chatBottomSheetMenu.visibility = View.GONE
                bottomBinding.rvImage.visibility = View.VISIBLE
                bottomBinding.rvImage.adapter = it?.let { it1 ->
                    Log.d(TAG, "initBottomSheet: $it1")
                    PhotoAdapter(1, it1) {
                        viewModel.removeImage(it)
                    }
                }
            } else {
                bottomBinding.btnSend.isEnabled = false
                bottomBinding.chatBottomSheetMenu.visibility = View.VISIBLE
                bottomBinding.rvImage.visibility = View.GONE
            }
        }

        binding.btnAdd.setOnClickListener {
            modalBottomSheet?.show()
        }
    }

    private val activityResult: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val clipData = result.data?.clipData
            val currentCount = viewModel.image.value?.size ?: 0
            val remainingSlots = 5 - currentCount

            if (clipData != null) {
                var addedCount = 0
                for (i in 0 until clipData.itemCount) {
                    if (addedCount >= remainingSlots) break

                    val uri = clipData.getItemAt(i).uri
                    if (FileUtil().isFileSizeValid(requireContext(),uri)) {
                        viewModel.addImage(ChatMessageDto.Files("name", "", uri))
                        addedCount++
                    }
                }
            } else {
                val uri = result.data?.data
                if (uri != null && currentCount < 5 && FileUtil().isFileSizeValid(requireContext(),uri)) {
                    viewModel.addImage(ChatMessageDto.Files("name", "", uri))
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        // stompClient가 초기화되고 연결되어 있는지 확인 후 연결 해제
        if (::stompClient.isInitialized && stompClient.isConnected()) {
            stompClient.disconnect()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                if (FileUtil().isFileSizeValid(requireContext(),uri)) {
                    viewModel.setFile(uri)
                    Log.d("uri", "파일 선택됨: $uri")
                } else {
                    Log.d("uri", "파일 크기 초과")
                }
            }
        }
    }


}
