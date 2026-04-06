package com.nyro.browser.utils

import java.io.File

object FileUtil {
    fun deleteRecursively(file: File): Boolean {
        return file.deleteRecursively()
    }
    
    fun ensureDirectoryExists(dir: File): Boolean {
        return dir.exists() || dir.mkdirs()
    }
    
    fun readTextFromFile(file: File): String? {
        return try {
            file.readText()
        } catch (e: Exception) {
            Logger.e("Error reading file: ${file.path}", e)
            null
        }
    }
    
    fun writeTextToFile(file: File, content: String): Boolean {
        return try {
            file.writeText(content)
            true
        } catch (e: Exception) {
            Logger.e("Error writing to file: ${file.path}", e)
            false
        }
    }
}
