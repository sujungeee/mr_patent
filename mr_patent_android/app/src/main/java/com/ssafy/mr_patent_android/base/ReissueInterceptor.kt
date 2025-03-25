package com.ssafy.mr_patent_android.base

import android.content.Intent
import android.util.Log
import com.ssafy.mr_patent_android.data.remote.RetrofitUtil.Companion.authService
import com.ssafy.mr_patent_android.ui.login.LoginActivity
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

private const val TAG = "ReissueInterceptor"


class ReissueInterceptor : Authenticator {
    val context = ApplicationClass.instance.applicationContext
    val intent= Intent(context, LoginActivity::class.java)

    override fun authenticate(route: Route?, response: Response): Request? {
        Log.i(TAG, "토큰 재발급 시도")

        return runBlocking {
            try {
                val res = authService.reissue(
                    ApplicationClass.sharedPreferences.getRToken() ?: ""
                )
                if (res.isSuccessful) {
                    val newToken = res.body()?.data.let {
                        it?.accessToken
                    }
                    val newRToken = res.body()?.data.let {
                        it?.refreshToken
                    }
                    ApplicationClass.sharedPreferences.addRToken(newRToken?:"")
                    if (!newToken.isNullOrBlank()) {
                        Log.i(TAG, "새로운 토큰 발급 성공: $newToken")
                        return@runBlocking response.request.newBuilder()
                            .header("ACCESS-TOKEN", "Bearer $newToken")
                            .build()
                    }
                } else {
                    Log.e(TAG, "토큰 재발급 실패: ${res.errorBody()?.string()}")
                    // 토큰 재발급 실패 시 로그인 화면으로 이동
                    goLoginActivity()
                }
            } catch (e: Exception) {
                Log.e(TAG, "토큰 재발급 중 오류 발생", e)
                // 토큰 재발급 실패 시 로그인 화면으로 이동
                goLoginActivity()
            }
            // 토큰 재발급 실패 시 로그인 화면으로 이동
            goLoginActivity()

            null // 토큰을 가져오지 못하면 인증을 실패로 처리
        }
    }

    fun goLoginActivity() {
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }
}
