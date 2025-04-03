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
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import com.ssafy.mr_patent_android.base.ApplicationClass.Companion.sharedPreferences
import com.ssafy.mr_patent_android.base.BaseFragment
import com.ssafy.mr_patent_android.data.model.dto.ChatMessageDto
import com.ssafy.mr_patent_android.data.model.dto.UserDto
import com.ssafy.mr_patent_android.databinding.DialogChatProfileBinding
import com.ssafy.mr_patent_android.databinding.FragmentChatBinding
import com.ssafy.mr_patent_android.databinding.FragmentChatBottomSheetBinding
import com.ssafy.mr_patent_android.databinding.ItemPhotoPreviewBinding
import com.ssafy.mr_patent_android.util.FileUtil
import kotlinx.coroutines.launch
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompHeader


private const val TAG = "ChatFragment"

class ChatFragment :
    BaseFragment<FragmentChatBinding>(FragmentChatBinding::bind, R.layout.fragment_chat) {
        private lateinit var messageListAdapter: MessageListAdapter
        val viewModel: ChatViewModel by viewModels()
        private val roomId: String  by lazy {
        navArgs<ChatFragmentArgs>().value.roomId }
        private val userId: Int by lazy {
            navArgs<ChatFragmentArgs>().value.userId }
        private val expertId: Int by lazy {
            navArgs<ChatFragmentArgs>().value.expertId }
        private val userName: String by lazy {
            navArgs<ChatFragmentArgs>().value.userName }
        private val userImage: String by lazy {
            navArgs<ChatFragmentArgs>().value.userImage }
    lateinit var stompClient: StompClient

    val url = "wss://j12d208.p.ssafy.io/"
    var headerList: MutableList<StompHeader> = mutableListOf()
    var modalBottomSheet: BottomSheetDialog? = null


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
        messageListAdapter = MessageListAdapter(UserDto(userName, userImage),listOf(), object : MessageListAdapter.ItemClickListener {
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

        // RecyclerView에 어댑터 설정 (한 번만)
        binding.rvChat.adapter = messageListAdapter

        viewModel.messageList.observe(viewLifecycleOwner){
            messageListAdapter.updateMessages(viewModel.messageList.value?: listOf())
        }

    }

    @SuppressLint("CheckResult")
    fun initStomp() {
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, url)

        stompClient.lifecycle().subscribe { lifecycleEvent ->
            when (lifecycleEvent.type) {
                LifecycleEvent.Type.OPENED -> {
                    Log.i("OPEND", "스톰프 연결 성공")
                    stompClient.topic("/chat" + roomId)
                        .subscribe({ topicMessage ->
                            try {
                                Log.d(TAG, topicMessage.getPayload())
                                val message = Gson().fromJson(topicMessage.getPayload(), ChatMessageDto::class.java)
                                Log.d(TAG, "initStomp: $message")
                                viewModel.addMessage(message)
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to process message", e)
                            }
                        }, { error ->
                            Log.e("SUBSCRIPTION_ERROR", "Topic subscription error: ${error.message}")
                        })

                }
                LifecycleEvent.Type.CLOSED -> {
                    Log.i("CLOSED", "스톰프 연결 종료")

                }
                LifecycleEvent.Type.ERROR -> {
                    Log.i("ERROR", "스톰프 연결 에러")
                    Log.e("CONNECT ERROR", lifecycleEvent.exception.toString())
                }
                else ->{
                    Log.i("ELSE", lifecycleEvent.message)
                }
            }
        }

        headerList.add(StompHeader("roomId",  roomId.toString()))
        headerList.add(StompHeader("userId", sharedPreferences.getUser().userId.toString()))
        stompClient.connect(headerList)


    }
    fun initView(){
        viewModel.getMessageList(roomId)
        binding.btnSend.isEnabled = false
        binding.etMessage.addTextChangedListener{
            if(it.toString().isNotEmpty()){
                binding.btnSend.isEnabled = true
            }else{
                binding.btnSend.isEnabled = false
            }
        }

        binding.btnSend.setOnClickListener {
            Log.d(TAG, "initView: asdasdhaskjdh")
            viewModel.sendMessage(roomId, binding.etMessage.text.toString(), null, requireContext(), stompClient)
            viewModel.addMessageFront(ChatMessageDto(
                chatId = null,
                isRead = false,
                message = binding.etMessage.text.toString(),
                receiverId = null,
                roomId = roomId,
                timestamp = null,
                type = "CHAT",
                userId = sharedPreferences.getUser().userId+1,
                fileName = null,
                fileUrl = null,
                fileUri = Uri.EMPTY,
                messageType = "TEXT"
            ))
            Log.d(TAG, "initView: ${viewModel.messageList.value}")
//            viewModel.setSendState(true)
//            viewModel.setSendState(false)

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
                    viewModel.getMessageList(roomId, viewModel.messageList.value?.last()?.chatId)
                }
            }
        })


    }

    fun initDialog(urls: String){
        val dialog = Dialog(requireContext())

        val dialogBinding= ItemPhotoPreviewBinding.inflate(layoutInflater)

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialogBinding.btnDelete.visibility = View.VISIBLE
        dialogBinding.previewImage.setOnClickListener {
            dialog.dismiss()
        }

        Glide.with(requireContext())
            .load(urls)
            .into(dialogBinding.previewImage)

        dialog.show()

    }
    fun initUserDialog(expertId: Int){
        val dialog = Dialog(requireContext())

        val dialogBinding= DialogChatProfileBinding.inflate(layoutInflater)

        viewModel.user.observe(viewLifecycleOwner){
            dialogBinding.tvName.text = it.userName
            dialogBinding.tvDescription.text = it.expertDescription
            dialogBinding.tvAddress.text = it.expertAddress
            dialogBinding.tvPhone.text = it.expertPhone

            Glide.with(requireContext())
                .load(it.userImage)
                .into(dialogBinding.ivProfile)


            if (it.expertCategory.isNotEmpty()) {
                it.expertCategory.forEach { category ->
                    when(category){
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
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            activityResult.launch(intent)
        }

        bottomBinding.btnFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
            }
            startActivityForResult(intent, 1)
        }

        viewModel.file.observe(viewLifecycleOwner){
            Log.d(TAG, "initBottomSheet: $it")
            if (it != null) {
                bottomBinding.btnSend.isEnabled = true

                bottomBinding.previewImage.btnDelete.visibility = View.VISIBLE
                bottomBinding.previewImage.btnDelete.setOnClickListener {
                    viewModel.setFile(null)
                }
                bottomBinding.chatBottomSheetMenu.visibility = View.GONE
                bottomBinding.previewImage.root.visibility = View.VISIBLE
                FileUtil().getFileExtension(requireContext(),it)?.let { it1 ->
                    if (it1 == "pdf") {
                        bottomBinding.previewImage.previewImage.setImageResource(R.drawable.pdf_icon)
                    } else if (it1 == "doc" || it1 == "docx") {
                        bottomBinding.previewImage.previewImage.setImageResource(R.drawable.word_icon)
                    }
                }
                bottomBinding.previewImage.tvFileName.text = FileUtil().getFileName(requireContext(),it)
            } else{

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
                files.add(ChatMessageDto.Files(FileUtil().getFileName(requireContext(),file)!!, "", file))
            }else {
                if (image != null) {
                    files.addAll(image)
                }

            }

            viewModel.sendMessage(roomId, binding.etMessage.text.toString(), files, requireContext(), stompClient)
            viewModel.setFile(null)
            viewModel.deleteImage()
        }



        viewModel.image.observe(viewLifecycleOwner){
            if (!it.isNullOrEmpty()) {
                bottomBinding.btnSend.isEnabled = true
                bottomBinding.chatBottomSheetMenu.visibility = View.GONE
                bottomBinding.rvImage.visibility = View.VISIBLE
                bottomBinding.rvImage.adapter = it?.let { it1 ->
                    Log.d(TAG, "initBottomSheet: $it1")
                    PhotoAdapter(1, it1){
                        viewModel.removeImage(it)
                    }
                }
            } else{
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
            if (clipData != null) {
                for (i in 0 until clipData.itemCount) {
                    val uri = clipData.getItemAt(i).uri
                    viewModel.addImage(ChatMessageDto.Files("name","", uri))
                }
            } else {
                val uri = result.data?.data
                viewModel.addImage(ChatMessageDto.Files("name","", uri!!))
                Log.d(TAG, "activityResult: $uri")
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
            if (data != null) {
                val uri = data.data
                viewModel.setFile(uri)
                Log.e("uri", uri.toString())
            }
        }
    }


}
