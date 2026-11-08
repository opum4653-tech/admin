package com.andorid.ninmods.admin.data

import android.content.Context
import android.content.SharedPreferences

class AdminSessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ninmods_admin_prefs", Context.MODE_PRIVATE)

    var apiKey: String?
        get() = prefs.getString("KEY_ADMIN_API_KEY", null)
        set(value) = prefs.edit().putString("KEY_ADMIN_API_KEY", value).apply()

    var username: String?
        get() = prefs.getString("KEY_ADMIN_USERNAME", null)
        set(value) = prefs.edit().putString("KEY_ADMIN_USERNAME", value).apply()

    var email: String?
        get() = prefs.getString("KEY_ADMIN_EMAIL", null)
        set(value) = prefs.edit().putString("KEY_ADMIN_EMAIL", value).apply()

    var role: String?
        get() = prefs.getString("KEY_ADMIN_ROLE", "admin")
        set(value) = prefs.edit().putString("KEY_ADMIN_ROLE", value).apply()

    val isLoggedIn: Boolean
        get() = !apiKey.isNullOrBlank()

    fun saveSession(key: String, user: String, mail: String, r: String) {
        prefs.edit()
            .putString("KEY_ADMIN_API_KEY", key)
            .putString("KEY_ADMIN_USERNAME", user)
            .putString("KEY_ADMIN_EMAIL", mail)
            .putString("KEY_ADMIN_ROLE", r)
            .apply()
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}
