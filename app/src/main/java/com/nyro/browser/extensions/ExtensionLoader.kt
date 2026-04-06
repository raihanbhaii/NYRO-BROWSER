package com.nyro.browser.extensions

import com.nyro.browser.utils.Logger
import android.content.Context
import com.nyro.browser.extensions.models.Extension
import com.nyro.browser.utils.Logger
import com.nyro.browser.utils.ManifestParser
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExtensionLoader @Inject constructor(
    private val context: Context,
    private val manifestParser: ManifestParser,
    private val contentScriptInjector: ContentScriptInjector,
    private val backgroundScriptRunner: BackgroundScriptRunner,
    private val extensionApiBridge: ExtensionApiBridge
) {
    
    private val loadedExtensions = mutableMapOf<String, Extension>()
    
    fun loadExtension(extensionPath: String): Extension? {
        return try {
            Logger.d("ExtensionLoader", "Loading extension from: $extensionPath")
            
            val extensionDir = File(extensionPath)
            if (!extensionDir.exists()) {
                Logger.e("ExtensionLoader", "Extension directory not found: $extensionPath")
                return null
            }
            
            val manifestFile = File(extensionDir, "manifest.json")
            if (!manifestFile.exists()) {
                Logger.e("ExtensionLoader", "Manifest file not found: $extensionPath")
                return null
            }
            
            val manifest = manifestParser.parse(manifestFile)
            if (manifest == null) {
                Logger.e("ExtensionLoader", "Failed to parse manifest: $extensionPath")
                return null
            }
            
            val extension = Extension(
                id = extensionDir.name,
                name = manifest.name,
                version = manifest.version,
                manifest = manifest,
                path = extensionDir,
                isEnabled = true,
                permissionsGranted = manifest.permissions ?: emptyList(),
                contentScripts = manifest.contentScripts ?: emptyList()
            )
            
            backgroundScriptRunner.runBackgroundScripts(manifest, extensionPath)
            
            loadedExtensions[extension.id] = extension
            Logger.d("ExtensionLoader", "Successfully loaded extension: ${manifest.name}")
            extension
            
        } catch (e: Exception) {
            Logger.e("ExtensionLoader", "Error loading extension from: $extensionPath", e)
            null
        }
    }
    
    fun unloadExtension(extensionId: String): Boolean {
        return try {
            val extension = loadedExtensions.remove(extensionId)
            if (extension != null) {
                Logger.d("ExtensionLoader", "Unloaded extension: ${extension.name}")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Logger.e("ExtensionLoader", "Error unloading extension: $extensionId", e)
            false
        }
    }
    
    fun getLoadedExtension(extensionId: String): Extension? = loadedExtensions[extensionId]
    
    fun getAllLoadedExtensions(): List<Extension> = loadedExtensions.values.toList()
    
    fun reloadExtension(extensionId: String): Extension? {
        unloadExtension(extensionId)
        val extension = loadedExtensions[extensionId]
        return extension?.let { loadExtension(it.path.absolutePath) }
    }
}
