package com.ssafy.mr_patent_android.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.content.ContentProviderCompat.requireContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

private const val TAG = "FileUtil_Mr_Patent"
class FileUtil {
    fun formatFileSize(sizeInBytes: Long): String {
        Log.d(TAG, "formatFileSize: ${sizeInBytes}")
        val df = DecimalFormat("#.##")

        return when {
            sizeInBytes >= 1024 * 1024 -> {
                val sizeInMB = BigDecimal(sizeInBytes).divide(BigDecimal(1024 * 1024), 2, RoundingMode.FLOOR)
                "${df.format(sizeInMB)} MB"
            }
            sizeInBytes >= 1024 -> {
                val sizeInKB = BigDecimal(sizeInBytes).divide(BigDecimal(1024), 2, RoundingMode.FLOOR)
                "${df.format(sizeInKB)} KB"
            }
            else -> "$sizeInBytes Bytes"
        }
    }

    fun getFileExtension(context: Context, uri: Uri): String? {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri)

        // MIME 타입으로 확장자 추출 시도
        if (mimeType != null) {
            return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
        }

        if (uri.scheme == "file" || uri.scheme == "content") {
            val fileName = uri.lastPathSegment ?: return null
            val dotIndex = fileName.lastIndexOf('.')
            if (dotIndex != -1 && dotIndex < fileName.length - 1) {
                return fileName.substring(dotIndex + 1)
            }
        }

        return null
    }

    fun getMimeType(fileType: String): MediaType {
        return when (fileType.lowercase()) {
            "image", "jpg", "jpeg" -> "image/jpeg".toMediaType()
            "png" -> "image/png".toMediaType()
            "pdf" -> "application/pdf".toMediaType()
            "doc" -> "application/msword".toMediaType()
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document".toMediaType()
            else -> "application/octet-stream".toMediaType() // default fallback
        }
    }

    fun formatType(fileType: String) : String {
        return when (fileType.lowercase()) {
            "image", "jpg", "jpeg","png"-> "IMAGE"
            "doc","docx" -> "WORD"
            "pdf" -> "PDF"
            else -> "TEXT" // default fallback
        }
    }


    fun getFileName(context: Context, uri: Uri): String? {
        return when (uri.scheme) {
            "content" -> {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            return it.getString(nameIndex)
                        }
                    }
                }
                null
            }

            "file" -> {
                File(uri.path!!).name
            }

            else -> null
        }
    }


    fun getFileSize(context: Context, uri: Uri): Long {
        var size = 0L
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (sizeIndex != -1 && cursor.moveToFirst()) {
                size = cursor.getLong(sizeIndex)
            }
        }

        return size
    }

    fun getFileFromUri(context: Context, uri: Uri?, name:String, type:String): File {
        val inputStream = uri?.let { context.contentResolver.openInputStream(it) }
        val file = File(context.cacheDir, "$name.$type")
        inputStream.use { input ->
            file.outputStream().use { output ->
                input?.copyTo(output)
            }
        }
        return file
    }

    fun getFilePathFromUri(context: Context, uri: Uri, extension: String): String {
        val inputStream = context.contentResolver.openInputStream(uri)
        val fileName = "selected_${System.currentTimeMillis()}.$extension"
        val file = File(context.cacheDir, fileName)

        inputStream.use { input ->
            file.outputStream().use { output ->
                input?.copyTo(output)
            }
        }

        return file.absolutePath
    }

    fun isFileSizeValid(context: Context,uri: Uri): Boolean {
        return try {
            val fileSize = context.contentResolver.openInputStream(uri)?.available()?.toLong() ?: 0
            val formattedSize = formatFileSize(fileSize)
            Log.d(TAG, "파일 크기: $formattedSize")

            fileSize <= 5 * 1024 * 1024
        } catch (e: Exception) {
            Log.e(TAG, "파일 크기 확인 중 오류 발생", e)
            false
        }
    }
}