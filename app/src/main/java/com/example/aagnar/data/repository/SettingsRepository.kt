// app/src/main/java/com/example/aagnar/data/repository/SettingsRepository.kt
package com.example.aagnar.data.repository

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("aagnar_settings", Context.MODE_PRIVATE)

    fun getHomeServer(): String {
        return prefs.getString("home_server", "https://matrix.org") ?: "https://matrix.org"
    }

    fun setHomeServer(url: String) {
        prefs.edit().putString("home_server", url).apply()
    }
}