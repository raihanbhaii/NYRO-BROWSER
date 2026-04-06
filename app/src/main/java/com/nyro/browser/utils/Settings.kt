package com.nyro.browser.utils

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Settings @Inject constructor(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("browser_settings", Context.MODE_PRIVATE)
    
    fun isJavaScriptEnabled(): Boolean = prefs.getBoolean("javascript_enabled", true)
    fun setJavaScriptEnabled(enabled: Boolean) = prefs.edit().putBoolean("javascript_enabled", enabled).apply()
    
    fun getHomePage(): String = prefs.getString("home_page", "https://www.google.com") ?: "https://www.google.com"
    fun setHomePage(url: String) = prefs.edit().putString("home_page", url).apply()
}
