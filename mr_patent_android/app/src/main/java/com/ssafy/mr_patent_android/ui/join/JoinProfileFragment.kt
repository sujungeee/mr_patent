package com.ssafy.mr_patent_android.ui.join

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.addCallback
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.ssafy.mr_patent_android.R
import com.ssafy.mr_patent_android.base.BaseFragment
import com.ssafy.mr_patent_android.databinding.FragmentJoinProfileBinding
import com.ssafy.mr_patent_android.util.FileUtil
import com.ssafy.mr_patent_android.util.ImageCompressor
import com.ssafy.mr_patent_android.util.ImagePicker
import com.ssafy.mr_patent_android.util.ImageUtil
import com.ssafy.mr_patent_android.util.LoadingDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

private const val TAG = "JoinProfileFragment_Mr_Patent"
class JoinProfileFragment : BaseFragment<FragmentJoinProfileBinding>(
    FragmentJoinProfileBinding::bind, R.layout.fragment_join_profile
) {

    private val joinViewModel: JoinViewModel by activityViewModels()
    private lateinit var imagePickerUtil: ImagePicker
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initObserver()
    }

    private fun initView() {
        loadingDialog = LoadingDialog(requireContext())
        imagePickerUtil = ImagePicker(this) { uri ->
            val extension = FileUtil().getFileExtension(requireContext(), uri)
            val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver,  uri)

            when (extension) {
                "jpg", "jpeg" -> {
                    loadingDialog.show()
                    val rotatedBitmap = ImageUtil().rotateBitmapIfNeeded(requireContext(), bitmap, uri)
                    val rotatedUri = ImageUtil().bitmapToUri(requireContext(), rotatedBitmap, extension)
                    handleImageJpgSelected(rotatedUri, extension)
                }
                "png" -> {
                    handleImagePngSelected(uri, bitmap, extension)
                }
            }
        }

        binding.tvBefore.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnPass.setOnClickListener {
            if (joinViewModel.userRole.value == 0)
                findNavController().navigate(R.id.nav_joinMemberFragment)
            else {
                findNavController().navigate(R.id.nav_joinExpertFragment)
            }
            joinViewModel.setUserImage("")
        }

        binding.btnNext.setOnClickListener {
            if (joinViewModel.userRole.value == 0) {
                findNavController().navigate(R.id.nav_joinMemberFragment)
            } else {
                findNavController().navigate(R.id.nav_joinExpertFragment)
            }
            if (joinViewModel.userImage.value == requireContext().resources.getString(R.string.default_image)
                || joinViewModel.userImage.value == null) {
                joinViewModel.setUserImage("")
            }
        }

        binding.ivProfile.setOnClickListener {
            imagePickerUtil.checkPermissionAndOpenGallery()
        }

        binding.tvProfileRegister.setOnClickListener {
            imagePickerUtil.checkPermissionAndOpenGallery()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack()
        }
    }

    private fun initObserver() {
        joinViewModel.userImage.observe(viewLifecycleOwner, {
            loadingDialog.dismiss()
            Glide.with(requireContext())
                .load(it)
                .fallback(R.drawable.user_profile)
                .error(R.drawable.image_load_error_icon)
                .centerInside()
                .into(binding.ivProfile)
        })
    }

    private fun handleImageJpgSelected(uri: Uri, extension: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            ImageCompressor(requireContext()).compressImage(uri, 500 * 1024)?.let { bytes ->
                val compressedUri = byteArrayToUri(bytes, extension)
                joinViewModel.setUserImage(compressedUri.toString())
            } ?: run {
                showCustomToast("이미지 처리 실패")
            }
        }
    }

    private fun handleImagePngSelected(uri: Uri, bitmap: Bitmap, extension: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val resizedBitmap = ImageUtil().resizeBitmap(bitmap, 600, 600)
            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            val bytes = outputStream.toByteArray()
            val compressedUri = byteArrayToUri(bytes, extension)
            joinViewModel.setUserImage(compressedUri.toString())
        }
    }

    private suspend fun byteArrayToUri(byteArray: ByteArray, extension: String): Uri {
        return withContext(Dispatchers.IO) {
            val fileName = "compressed_${System.currentTimeMillis()}.$extension"
            val file = File(requireContext().cacheDir, fileName)
            file.writeBytes(byteArray)
            FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(key: String, value: String) =
            JoinProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(key, value)
                }
            }
    }
}