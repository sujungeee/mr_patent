package com.ssafy.mr_patent_android.util


import android.content.Context
import android.content.SharedPreferences
import com.ssafy.mr_patent_android.base.ApplicationClass
import com.ssafy.mr_patent_android.data.model.dto.UserDto

class SharedPreferencesUtil(context: Context) {
    private var preferences: SharedPreferences =
        context.getSharedPreferences(ApplicationClass.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun addAToken(token: String) {
        val editor = preferences.edit()
        editor.putString(ApplicationClass.ACCESS_TOKEN, token)
        editor.apply()
    }

    fun getAToken(): String? {
        return preferences.getString(ApplicationClass.ACCESS_TOKEN, null)
    }
    fun addRToken(token: String) {
        val editor = preferences.edit()
        editor.putString(ApplicationClass.ACCESS_TOKEN, token)
        editor.apply()
    }

    fun getRToken(): String? {
        return preferences.getString(ApplicationClass.ACCESS_TOKEN, null)
    }


//    fun addCookie(cookies: HashSet<String>) {
//        val editor = preferences.edit()
//        editor.putStringSet(ApplicationClass.COOKIES_KEY_NAME, cookies)
//        editor.apply()
//    }
//
//    fun getCookie(): MutableSet<String>? {
//        return preferences.getStringSet(ApplicationClass.COOKIES_KEY_NAME, HashSet())
//    }

    fun addUser(userDto: UserDto){
        val editor = preferences.edit()
        editor.putInt("user_id", userDto.user_id)
        editor.putString("user_nickname", userDto.user_nickname)
        editor.putInt("user_role", userDto.user_role)
        editor.apply()
    }

    fun getUser(): UserDto {
        val user_id = preferences.getInt("user_id", -1)
        val user_nickname = preferences.getString("user_nickname", null)
        val user_role = preferences.getInt("user_role", -1)
        return UserDto(user_id, user_nickname!!, user_role)
    }

    fun getString(key:String): String? {
        return preferences.getString(key, null)
    }

}
