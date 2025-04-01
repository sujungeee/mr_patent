package com.ssafy.mr_patent_android.data.remote

import com.ssafy.mr_patent_android.base.ApplicationClass


class RetrofitUtil {
    companion object{
        val authService = ApplicationClass.retrofit.create(AuthService::class.java)
        val userService = ApplicationClass.retrofit.create(UserService::class.java)
        val chatService = ApplicationClass.retrofit.create(ChatService::class.java)
        val patentService = ApplicationClass.retrofit.create(PatentService::class.java)
        val fileService = ApplicationClass.retrofit.create(FileService::class.java)
    }
}