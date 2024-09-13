package com.zulfahmi.simukomperawat.utlis

import android.content.SharedPreferences
import com.zulfahmi.simukomperawat.model.User

class PreferencesManager {
    companion object{
        private lateinit var preferences: SharedPreferences

        fun initPreferences(): PreferencesManager {
            preferences = MyApplication.getInstance().getSharedPreferences()
            return PreferencesManager()
        }
    }

    fun setUserInfo(user: User) {
        val editor: SharedPreferences.Editor = preferences.edit()
        editor.apply {
            putString(Constants.PREF_USERNAME, user.username)
            putString(Constants.PREF_EMAIL, user.email)
            putString(Constants.PREF_USERID, user.userId)
            putString(Constants.PREF_LOGIN_TIME, user.loginTime)
        }
        editor.apply()
    }

    fun getUserInfo(): User {
        return User(
            username = preferences.getString(Constants.PREF_USERNAME, "") as String,
            email = preferences.getString(Constants.PREF_EMAIL, "") as String,
            userId = preferences.getString(Constants.PREF_USERID, "") as String,
            loginTime = preferences.getString(Constants.PREF_LOGIN_TIME, "") as String
        )
    }
}