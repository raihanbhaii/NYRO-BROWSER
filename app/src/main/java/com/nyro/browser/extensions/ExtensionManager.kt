package com.nyro.browser.extensions

import android.content.Context
import com.nyro.browser.extensions.models.Extension
import com.nyro.browser.extensions.models.Manifest
import com.nyro.browser.utils.Logger
import com.nyro.browser.utils.ManifestParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExtensionManager @Inject constructor(
    private val context: Context,
    private val chromeWebStoreClient: ChromeWebStoreClient,
    private val manifestParser: ManifestParser
) {
    
    private val extensions = mutableMapOf<String, Extension>()
    private val _enabledExtensions = MutableStateFlow<List<Extension>>(emptyList())
    val enabledExtensions: StateFlow<List<Extension>> = _enabledExtensions.asStateFlow()
    
    private val extensionsDir: File by lazy {
        File(context.filesDir, "extensions").apply { mkdirs() }
    }
    
    suspend fun installExtension(extensionId: String, version: String): Boolean {
        return try {
            Logger.d("ExtensionManager", "Installing extension: $extensionId")
            
            val extensionData = chromeWebStoreClient.downloadExtension(extensionId)
            
            val extensionDir = File(extensionsDir, extensionId)
            extensionDir.mkdirs()
            
            val crxFile = File(extensionDir, "$extensionId.crx")
            crxFile.writeBytes(extensionData)
            
            val manifestFile = File(extensionDir, "manifest.json")
            val manifest = if (manifestFile.exists()) {
                manifestParser.parse(manifestFile)
            } else {
                createBasicManifest(extensionId, version)
            }
            
            if (manifest == null) {
                Logger.e("ExtensionManager", "Failed to parse manifest for: $extensionId")
                return false
            }
            
            val extension = Extension(
                id = extensionId,
                name = manifest.name,
                version = manifest.version,
                manifest = manifest,
                path = extensionDir,
                isEnabled = true,
                permissionsGranted = manifest.permissions ?: emptyList(),
                contentScripts = manifest.contentScripts ?: emptyList()
            )
            
            extensions[extensionId] = extension
            updateEnabledExtensions()
            
            Logger.d("ExtensionManager", "Successfully installed extension: ${manifest.name}")
            true
            
        } catch (e: Exception) {
            Logger.e("ExtensionManager", "Failed to install extension: $extensionId", e)
            false
        }
    }
    
    private fun createBasicManifest(extensionId: String, version: String): Manifest {
        return Manifest(
            manifestVersion = 3,
            name = "Extension $extensionId",
            version = version,
            description = "Auto-generated extension manifest",
            permissions = emptyList(),
            hostPermissions = emptyList(),
            contentScripts = emptyList()
        )
    }
    
    fun uninstallExtension(extensionId: String): Boolean {
        return try {
            val extension = extensions.remove(extensionId)
            if (extension != null) {
                extension.path.deleteRecursively()
                updateEnabledExtensions()
                Logger.d("ExtensionManager", "Uninstalled extension: ${extension.name}")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Logger.e("ExtensionManager", "Failed to uninstall extension: $extensionId", e)
            false
        }
    }
    
    fun enableExtension(extensionId: String): Boolean {
        val extension = extensions[extensionId]
        return if (extension != null) {
            extensions[extensionId] = extension.copy(isEnabled = true)
            updateEnabledExtensions()
            Logger.d("ExtensionManager", "Enabled extension: ${extension.name}")
            true
        } else {
            false
        }
    }
    
    fun disableExtension(extensionId: String): Boolean {
        val extension = extensions[extensionId]
        return if (extension != null) {
            extensions[extensionId] = extension.copy(isEnabled = false)
            updateEnabledExtensions()
            Logger.d("ExtensionManager", "Disabled extension: ${extension.name}")
            true
        } else {
            false
        }
    }
    
    private fun updateEnabledExtensions() {
        _enabledExtensions.value = extensions.values.filter { it.isEnabled }
    }
    
    fun getExtension(extensionId: String): Extension? = extensions[extensionId]
    
    fun getAllExtensions(): List<Extension> = extensions.values.toList()
    
    fun getEnabledExtensions(): List<Extension> = extensions.values.filter { it.isEnabled }
    
    fun isExtensionInstalled(extensionId: String): Boolean = extensions.containsKey(extensionId)
}
