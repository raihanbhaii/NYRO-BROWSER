package com.nyro.browser.extensions

import android.content.Context
import com.nyro.browser.extensions.models.Extension
import com.nyro.browser.utils.Logger
import com.nyro.browser.utils.ManifestParser
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
    private val extensionsDir: File by lazy {
        File(context.filesDir, "extensions").apply { mkdirs() }
    }
    
    suspend fun installExtension(extensionId: String, version: String): Boolean {
        return try {
            Logger.d("ExtensionManager", "Installing extension: $extensionId")
            
            // Download the extension
            val extensionData = chromeWebStoreClient.downloadExtension(extensionId)
            
            // Save extension files
            val extensionDir = File(extensionsDir, extensionId)
            extensionDir.mkdirs()
            
            // Parse manifest
            val manifestFile = File(extensionDir, "manifest.json")
            manifestFile.writeBytes(extensionData)
            
            val manifest = manifestParser.parse(manifestFile)
            if (manifest == null) {
                Logger.e("ExtensionManager", "Failed to parse manifest for: $extensionId")
                return false
            }
            
            // Create extension object
            val extension = Extension(
                id = extensionId,
                name = manifest.name,
                version = manifest.version,
                manifest = manifest,
                path = extensionDir,
                isEnabled = true,
                permissionsGranted = manifest.permissions ?: emptyList()
            )
            
            extensions[extensionId] = extension
            Logger.d("ExtensionManager", "Successfully installed extension: ${manifest.name}")
            true
            
        } catch (e: Exception) {
            Logger.e("ExtensionManager", "Failed to install extension: $extensionId", e)
            false
        }
    }
    
    fun uninstallExtension(extensionId: String): Boolean {
        return try {
            val extension = extensions.remove(extensionId)
            if (extension != null) {
                extension.path.deleteRecursively()
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
            Logger.d("ExtensionManager", "Disabled extension: ${extension.name}")
            true
        } else {
            false
        }
    }
    
    fun getExtension(extensionId: String): Extension? {
        return extensions[extensionId]
    }
    
    fun getAllExtensions(): List<Extension> {
        return extensions.values.toList()
    }
    
    fun getEnabledExtensions(): List<Extension> {
        return extensions.values.filter { it.isEnabled }
    }
    
    fun isExtensionInstalled(extensionId: String): Boolean {
        return extensions.containsKey(extensionId)
    }
}
