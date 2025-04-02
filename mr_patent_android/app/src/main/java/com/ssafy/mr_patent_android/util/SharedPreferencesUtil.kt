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

    fun clearToken() {
        val editor = preferences.edit()
        editor.apply {
            remove(ApplicationClass.ACCESS_TOKEN)
            remove(ApplicationClass.REFRESH_TOKEN)
            apply()
        }
    }


    fun addUser(userDto: UserDto){
        val editor = preferences.edit()
        editor.putInt("user_id", userDto.userId)
        editor.putString("user_nickname", userDto.userName)
        editor.putInt("user_role", userDto.userRole)
        editor.apply()
    }

    fun getUser(): UserDto {
        val user_id = preferences.getInt("user_id", -1)
        val user_nickname = preferences.getString("user_nickname", "")
        val user_role = preferences.getInt("user_role", -1)
        return UserDto(user_id, user_nickname!!, user_role)
    }

    fun getString(key:String): String? {
        return preferences.getString(key, null)
    }

}
