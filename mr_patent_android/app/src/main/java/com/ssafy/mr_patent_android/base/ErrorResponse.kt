package com.ssafy.mr_patent_android.base

import com.google.gson.annotations.SerializedName

class ErrorResponse(
    @SerializedName("error") val error: Error? = null
){
    data class Error(
        @SerializedName("code") val code: Int,
        @SerializedName("message") val message: String
    )
}
