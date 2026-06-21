package com.goesin.app.util

import android.content.Context
import android.content.SharedPreferences
import com.goesin.app.model.User
import com.google.gson.Gson

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("GowesInPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val IS_LOGGED_IN = "isLoggedIn"
        private const val USER_DATA = "userData"
        private const val AUTH_TOKEN = "authToken"
    }

    fun saveUser(user: User, token: String) {
        val editor = prefs.edit()
        editor.putBoolean(IS_LOGGED_IN, true)
        editor.putString(USER_DATA, gson.toJson(user))
        editor.putString(AUTH_TOKEN, token)
        editor.apply()
    }

    fun getUser(): User? {
        val userData = prefs.getString(USER_DATA, null)
        return if (userData != null) {
            gson.fromJson(userData, User::class.java)
        } else {
            null
        }
    }

    fun getToken(): String? {
        return prefs.getString(AUTH_TOKEN, null)
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(IS_LOGGED_IN, false) && getToken() != null
    }

    fun logout() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}