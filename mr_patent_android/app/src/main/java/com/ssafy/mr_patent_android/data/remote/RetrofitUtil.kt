package com.ssafy.mr_patent_android.data.remote

import com.ssafy.mr_patent_android.base.ApplicationClass


class RetrofitUtil {
    companion object{
        val authService = ApplicationClass.retrofit.create(AuthService::class.java)

    }
}