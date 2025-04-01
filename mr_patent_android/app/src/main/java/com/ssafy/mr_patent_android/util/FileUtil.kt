package com.ssafy.mr_patent_android.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import java.io.File

class FileUtil {
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
}