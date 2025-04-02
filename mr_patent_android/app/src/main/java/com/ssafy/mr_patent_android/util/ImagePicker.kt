package com.ssafy.mr_patent_android.util

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

private const val TAG = "ImagePicker_Mr_Patent"
class ImagePicker(
    private val fragment: Fragment,
    private val onImageSelected: (Uri) -> Unit
) {
    private val requestPermissionLauncher = fragment.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openGallery()
        } else {
            Toast.makeText(fragment.requireContext(), "권한을 허용해주세요.", Toast.LENGTH_SHORT).show()
        }
    }

    private val pickImageLauncher = fragment.registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let(onImageSelected)
    }

    fun checkPermissionAndOpenGallery() {
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(fragment.requireContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }
        }else{
            if (ContextCompat.checkSelfPermission(fragment.requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun openGallery() {
        pickImageLauncher.launch(arrayOf("image/jpeg", "image/png"))
    }
}