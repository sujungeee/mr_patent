package com.ssafy.mr_patent_android.base

import android.app.Application
import android.content.ContentValues.TAG
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ssafy.mr_patent_android.base.ApplicationClass.Companion.X_ACCESS_TOKEN
import com.ssafy.mr_patent_android.base.ApplicationClass.Companion.sharedPreferences
import com.ssafy.mr_patent_android.util.SharedPreferencesUtil
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

// 앱이 실행될때 1번만 실행이 됩니다.
class ApplicationClass : Application() {
    val API_URL = ""

    companion object {
        lateinit var sharedPreferences: SharedPreferencesUtil

        const val X_ACCESS_TOKEN = "X-ACCESS-TOKEN"
        const val SHARED_PREFERENCES_NAME = "SSAFY_TEMPLATE_APP"
        const val COOKIES_KEY_NAME = "cookies"

        lateinit var retrofit: Retrofit
    }

    override fun onCreate() {
        super.onCreate()

        sharedPreferences = SharedPreferencesUtil(applicationContext)

        initRetrofitInstance()
    }

    private fun initRetrofitInstance() {
        val client: OkHttpClient = OkHttpClient.Builder()
            .readTimeout(5000, TimeUnit.MILLISECONDS)
            .connectTimeout(5000, TimeUnit.MILLISECONDS)
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .addNetworkInterceptor(XAccessTokenInterceptor())
            .addInterceptor(AddCookiesInterceptor())
            .addInterceptor(ReceivedCookiesInterceptor())
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(API_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val gson : Gson = GsonBuilder()
        .setLenient()
        .create()
}

class XAccessTokenInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder: Request.Builder = chain.request().newBuilder()
        val jwtToken: String? = sharedPreferences.getString(X_ACCESS_TOKEN)
        if (jwtToken != null) {
            builder.addHeader("ACCESS-TOKEN", jwtToken)
        }
        return chain.proceed(builder.build())
    }
}

class AddCookiesInterceptor : Interceptor{

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder: Request.Builder = chain.request().newBuilder()


        // cookie 가져오기
        val getCookies = sharedPreferences.getUserCookie()
        for (cookie in getCookies!!) {
            builder.addHeader("Cookie", cookie)
            Log.d(TAG,"Adding Header: $cookie")
        }
        return chain.proceed(builder.build())
    }
}

class ReceivedCookiesInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain):Response{
        val originalResponse: Response = chain.proceed(chain.request())

        if (originalResponse.headers("Set-Cookie").isNotEmpty()) {

            val cookies = HashSet<String>()
            for (cookie in originalResponse.headers("Set-Cookie")) {
                cookies.add(cookie)
                Log.d(TAG, "intercept: $cookie")
            }

            // cookie 내부 데이터에 저장
            sharedPreferences.addUserCookie(cookies)
        }
        return originalResponse
    }
}


