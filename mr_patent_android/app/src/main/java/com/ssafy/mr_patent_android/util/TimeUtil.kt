package com.ssafy.mr_patent_android.util

import android.util.Log
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private const val TAG = "TimeUtil"
class TimeUtil {
    fun parseUtcWithJavaTime(utcTimestamp: String) :LocalDateTime {
        if (utcTimestamp.isEmpty()) {
            Log.d(TAG, "parseUtcWithJavaTime: utcTimestamp is empty")
            return LocalDateTime.now()
        }
        val instant = Instant.parse(utcTimestamp)
        val systemZoneId = ZoneId.systemDefault()
        val localDateTime = LocalDateTime.ofInstant(instant, systemZoneId)

        return localDateTime
    }
    fun formatLocalDateTimeToString(localDateTime: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return localDateTime.format(formatter)
    }

    fun formatLocalDateTimeToDateString(localDateTime: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return localDateTime.format(formatter)
    }
}