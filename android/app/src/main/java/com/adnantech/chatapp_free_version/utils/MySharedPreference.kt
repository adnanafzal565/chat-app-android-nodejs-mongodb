package com.adnantech.chatapp_free_version.utils

import android.content.Context
import android.content.SharedPreferences

class MySharedPreference {
    private val fileName: String = "MY_SHARED_PREFERENCE"

    fun getAPIURL(context: Context): String {
        val preference: SharedPreferences =
            context.getSharedPreferences(this.fileName, Context.MODE_PRIVATE)
        return preference.getString("API_URL", "http://192.168.8.100:3000").toString()
    }

    fun setAPIURL(context: Context, apiUrl: String) {
        val preference: SharedPreferences =
            context.getSharedPreferences(this.fileName, Context.MODE_PRIVATE)
        preference.edit().putString("API_URL", apiUrl).apply()
    }

    fun isFirstTime(context: Context): Boolean {
        val preference: SharedPreferences =
            context.getSharedPreferences(this.fileName, Context.MODE_PRIVATE)
        return preference.getBoolean("isFirstTime", true)
    }

    fun setIsFirstTime(context: Context) {
        val preference: SharedPreferences =
            context.getSharedPreferences(this.fileName, Context.MODE_PRIVATE)
        preference.edit().putBoolean("isFirstTime", false).apply()
    }

    fun removeContactsSaved(context: Context) {
        val preference: SharedPreferences =
            context.getSharedPreferences(this.fileName, Context.MODE_PRIVATE)
        preference.edit().remove("contactsSaved").apply()
    }

    fun getContactsSaved(context: Context): Boolean {
        val preference: SharedPreferences =
            context.getSharedPreferences(this.fileName, Context.MODE_PRIVATE)
        return preference.getBoolean("contactsSaved", false)
    }

    fun setContactsSaved(context: Context) {
        val preference: SharedPreferences =
            context.getSharedPreferences(this.fileName, Context.MODE_PRIVATE)
        preference.edit().putBoolean("contactsSaved", true).apply()
    }

    fun removeAccessToken(context: Context) {
        val preference: SharedPreferences =
            context.getSharedPreferences(this.fileName, Context.MODE_PRIVATE)
        preference.edit().remove("accessToken").apply()
    }

    fun getAccessToken(context: Context): String {
        val preference: SharedPreferences =
            context.getSharedPreferences(this.fileName, Context.MODE_PRIVATE)
        return preference.getString("accessToken", "").toString()
    }

    fun setAccessToken(context: Context, token: String) {
        val preference: SharedPreferences =
            context.getSharedPreferences(this.fileName, Context.MODE_PRIVATE)
        preference.edit().putString("accessToken", token).apply()
    }
}
