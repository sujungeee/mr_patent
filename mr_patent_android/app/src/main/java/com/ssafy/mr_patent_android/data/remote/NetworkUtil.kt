package com.ssafy.mr_patent_android.data.remote

import com.ssafy.mr_patent_android.base.ApplicationClass
import com.ssafy.mr_patent_android.base.ErrorResponse
import okhttp3.ResponseBody

object NetworkUtil {
    fun getErrorResponse(errorBody: ResponseBody): ErrorResponse? {
        return ApplicationClass.retrofit.responseBodyConverter<ErrorResponse>(
            ErrorResponse::class.java,
            ErrorResponse::class.java.annotations
        ).convert(errorBody)
    }
}