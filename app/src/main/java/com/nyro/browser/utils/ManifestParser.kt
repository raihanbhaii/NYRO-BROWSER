package com.nyro.browser.utils

import com.google.gson.Gson
import com.nyro.browser.extensions.models.Manifest
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ManifestParser @Inject constructor() {
    
    private val gson = Gson()
    
    fun parse(file: File): Manifest? {
        return try {
            val jsonContent = file.readText()
            gson.fromJson(jsonContent, Manifest::class.java)
        } catch (e: Exception) {
            Logger.e("ManifestParser", "Error parsing manifest file: ${file.path}", e)
            null
        }
    }
    
    fun parse(jsonString: String): Manifest? {
        return try {
            gson.fromJson(jsonString, Manifest::class.java)
        } catch (e: Exception) {
            Logger.e("ManifestParser", "Error parsing manifest JSON", e)
            null
        }
    }
}
