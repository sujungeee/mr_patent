package com.ssafy.mr_patent_android.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.content.ContentProviderCompat.requireContext
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
        val mimeType = contentResolver.getType(uri) ?: return null

        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
    }

    fun getFileName(context: Context, uri: Uri): String? {
        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(uri, null, null, null, null)

        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    return it.getString(nameIndex)
                }
            }
        }
        return null
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