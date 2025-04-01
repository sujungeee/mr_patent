package com.ssafy.mr_patent_android.util

import android.util.Log
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
}