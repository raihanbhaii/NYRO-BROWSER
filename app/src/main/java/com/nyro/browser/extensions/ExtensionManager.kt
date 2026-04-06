package com.nyro.browser.extensions

import android.content.Context
import android.content.pm.PackageManager
import com.nyro.browser.utils.Logger
import com.nyro.browser.extensions.models.Extension
import com.nyro.browser.extensions.models.Manifest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExtensionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val extensionLoader: ExtensionLoader,
    private val chromeWebStoreClient: ChromeWebStoreClient
) {
    
    companion object {
        private const val TAG = "ExtensionManager"
        private var instance: ExtensionManager? = null
        
        fun initialize(context: Context) {
            // Hilt handles initialization, this is for legacy compatibility
        }
        
        fun getInstance(): ExtensionManager {
            return instance ?: throw IllegalStateException("ExtensionManager not initialized")
        }
    }
    
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val _extensions = MutableStateFlow<List<Extension>>(emptyList())
    val extensions: StateFlow<List<Extension>> = _extensions.asStateFlow()
    
    private val _enabledExtensions = MutableStateFlow<List<Extension>>(emptyList())
    val enabledExtensions: StateFlow<List<Extension>> = _enabledExtensions.asStateFlow()
    
    private val extensionDir: File by lazy {
        File(context.filesDir, "extensions").apply { mkdirs() }
    }
    
    private val unpackedDir: File by lazy {
        File(context.filesDir, "extensions/unpacked").apply { mkdirs() }
    }
    
    init {
        instance = this
        loadExtensions()
    }
    
    private fun loadExtensions() {
        coroutineScope.launch {
            try {
                val loaded = extensionLoader.loadAllExtensions(extensionDir, unpackedDir)
                _extensions.value = loaded
                _enabledExtensions.value = loaded.filter { it.enabled }
                Logger.d(TAG, "Loaded ${loaded.size} extensions")
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to load extensions", e)
            }
        }
    }
    
    suspend fun installFromChromeWebStore(extensionId: String): Extension? {
        return try {
            val crxData = chromeWebStoreClient.downloadExtension(extensionId)
            val extension = extensionLoader.installFromCrx(crxData, extensionDir)
            if (extension != null) {
                _extensions.value = _extensions.value + extension
                if (extension.enabled) {
                    _enabledExtensions.value = _enabledExtensions.value + extension
                }
                Logger.d(TAG, "Installed extension from Chrome Web Store: ${extension.name}")
            }
            extension
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to install extension from Chrome Web Store", e)
            null
        }
    }
    
    suspend fun installUnpackedExtension(directory: File): Extension? {
        return try {
            val manifest = ManifestParser.parse(File(directory, "manifest.json"))
            val extension = extensionLoader.loadUnpackedExtension(directory, manifest, unpackedDir)
            if (extension != null) {
                _extensions.value = _extensions.value + extension
                if (extension.enabled) {
                    _enabledExtensions.value = _enabledExtensions.value + extension
                }
                Logger.d(TAG, "Installed unpacked extension: ${extension.name}")
            }
            extension
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to install unpacked extension", e)
            null
        }
    }
    
    fun enableExtension(extensionId: String): Boolean {
        val extension = _extensions.value.find { it.id == extensionId } ?: return false
        val updated = extension.copy(enabled = true)
        extensionLoader.enableExtension(updated)
        _extensions.value = _extensions.value.map { if (it.id == extensionId) updated else it }
        _enabledExtensions.value = _enabledExtensions.value + updated
        Logger.d(TAG, "Enabled extension: ${updated.name}")
        return true
    }
    
    fun disableExtension(extensionId: String): Boolean {
        val extension = _extensions.value.find { it.id == extensionId } ?: return false
        val updated = extension.copy(enabled = false)
        extensionLoader.disableExtension(updated)
        _extensions.value = _extensions.value.map { if (it.id == extensionId) updated else it }
        _enabledExtensions.value = _enabledExtensions.value.filter { it.id != extensionId }
        Logger.d(TAG, "Disabled extension: ${updated.name}")
        return true
    }
    
    fun uninstallExtension(extensionId: String): Boolean {
        val extension = _extensions.value.find { it.id == extensionId } ?: return false
        extensionLoader.uninstallExtension(extension)
        _extensions.value = _extensions.value.filter { it.id != extensionId }
        _enabledExtensions.value = _enabledExtensions.value.filter { it.id != extensionId }
        Logger.d(TAG, "Uninstalled extension: ${extension.name}")
        return true
    }
    
    fun getExtension(extensionId: String): Extension? {
        return _extensions.value.find { it.id == extensionId }
    }
    
    fun searchExtensions(query: String): List<Extension> {
        val lowerQuery = query.lowercase()
        return _extensions.value.filter { 
            it.name.lowercase().contains(lowerQuery) || 
            it.description?.lowercase()?.contains(lowerQuery) == true
        }
    }
    
    fun requestPermissions(extension: Extension, granted: List<String>) {
        extensionLoader.grantPermissions(extension, granted)
    }
    
    fun revokePermissions(extension: Extension, revoked: List<String>) {
        extensionLoader.revokePermissions(extension, revoked)
    }
    
    fun shutdown() {
        coroutineScope.cancel()
        extensionLoader.shutdown()
        Logger.d(TAG, "ExtensionManager shutdown complete")
    }
}
