package com.nyro.browser.extensions

import android.content.Context
import com.nyro.browser.BuildConfig
import com.nyro.browser.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChromeWebStoreClient @Inject constructor(
    private val context: Context,
    private val httpClient: OkHttpClient
) {
    
    companion object {
        private const val TAG = "ChromeWebStoreClient"
        private const val BASE_URL = "https://clients2.google.com/service/update2/crx"
        private const val USER_AGENT = "Chrome/121.0.0.0 Safari/537.36"
    }
    
    suspend fun downloadExtension(extensionId: String): ByteArray {
        return withContext(Dispatchers.IO) {
            try {
                val url = buildDownloadUrl(extensionId)
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "*/*")
                    .build()
                
                val response = httpClient.newCall(request).execute()
                
                if (!response.isSuccessful) {
                    throw IOException("Failed to download extension: ${response.code}")
                }
                
                response.body?.bytes() ?: throw IOException("Empty response body")
                
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to download extension $extensionId", e)
                throw e
            }
        }
    }
    
    suspend fun downloadExtensionToFile(extensionId: String, destinationDir: File): File? {
        return withContext(Dispatchers.IO) {
            try {
                val extensionData = downloadExtension(extensionId)
                val extensionFile = File(destinationDir, "$extensionId.crx")
                
                FileOutputStream(extensionFile).use { output ->
                    output.write(extensionData)
                }
                
                Logger.d(TAG, "Successfully downloaded extension to: ${extensionFile.absolutePath}")
                extensionFile
                
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to download extension to file: $extensionId", e)
                null
            }
        }
    }
    
    suspend fun getExtensionInfo(extensionId: String): ExtensionInfo? {
        return withContext(Dispatchers.IO) {
            try {
                Logger.d(TAG, "Getting info for extension: $extensionId")
                null
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to get extension info", e)
                null
            }
        }
    }
    
    suspend fun searchExtensions(query: String, limit: Int = 20): List<ExtensionSearchResult> {
        return withContext(Dispatchers.IO) {
            try {
                Logger.d(TAG, "Searching extensions with query: $query")
                emptyList()
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to search extensions", e)
                emptyList()
            }
        }
    }
    
    private fun buildDownloadUrl(extensionId: String): String {
        return "$BASE_URL?response=redirect&prodversion=${getChromeVersion()}&acceptformat=crx2,crx3&x=id%3D$extensionId%26uc"
    }
    
    private fun getChromeVersion(): String {
        return if (BuildConfig.DEBUG) "121.0.6167.85" else "121.0.6167.85"
    }
    
    data class ExtensionInfo(
        val id: String,
        val name: String,
        val version: String,
        val description: String,
        val rating: Float,
        val userCount: Long,
        val iconUrl: String,
        val screenshots: List<String>,
        val permissions: List<String>,
        val lastUpdated: Long
    )
    
    data class ExtensionSearchResult(
        val id: String,
        val name: String,
        val shortDescription: String,
        val rating: Float,
        val userCount: Long,
        val iconUrl: String,
        val category: String
    )
}
