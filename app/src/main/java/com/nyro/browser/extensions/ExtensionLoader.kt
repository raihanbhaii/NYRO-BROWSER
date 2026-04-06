package com.nyro.browser.extensions

import android.content.Context
import com.nyro.browser.utils.Logger
import com.nyro.browser.extensions.models.Extension
import com.nyro.browser.extensions.models.Manifest
import com.nyro.browser.utils.FileUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mozilla.geckoview.WebExtension
import org.mozilla.geckoview.WebExtensionController
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExtensionLoader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val manifestParser: ManifestParser,
    private val contentScriptInjector: ContentScriptInjector,
    private val backgroundScriptRunner: BackgroundScriptRunner,
    private val extensionApiBridge: ExtensionApiBridge
) {
    
    companion object {
        private const val TAG = "ExtensionLoader"
        private const val EXTENSION_VERSION = 1
    }
    
    private val installedExtensions = mutableMapOf<String, Extension>()
    private val webExtensions = mutableMapOf<String, WebExtension>()
    
    suspend fun loadAllExtensions(installedDir: File, unpackedDir: File): List<Extension> {
        return withContext(Dispatchers.IO) {
            val extensions = mutableListOf<Extension>()
            
            // Load installed (.crx) extensions
            installedDir.listFiles { file -> file.extension == "crx" }?.forEach { crxFile ->
                try {
                    val extension = loadInstalledExtension(crxFile)
                    if (extension != null) extensions.add(extension)
                } catch (e: Exception) {
                    Logger.e(TAG, "Failed to load installed extension: ${crxFile.name}", e)
                }
            }
            
            // Load unpacked extensions
            unpackedDir.listFiles { file -> file.isDirectory }?.forEach { extDir ->
                try {
                    val manifestFile = File(extDir, "manifest.json")
                    if (manifestFile.exists()) {
                        val manifest = manifestParser.parse(manifestFile)
                        val extension = loadUnpackedExtension(extDir, manifest, unpackedDir)
                        if (extension != null) extensions.add(extension)
                    }
                } catch (e: Exception) {
                    Logger.e(TAG, "Failed to load unpacked extension: ${extDir.name}", e)
                }
            }
            
            extensions
        }
    }
    
    suspend fun installFromCrx(crxData: ByteArray, installDir: File): Extension? {
        return withContext(Dispatchers.IO) {
            try {
                val extensionId = generateExtensionId(crxData)
                val extensionFile = File(installDir, "$extensionId.crx")
                
                // Save CRX file
                extensionFile.writeBytes(crxData)
                
                // Extract and parse manifest
                val manifest = manifestParser.parseFromCrx(crxData)
                
                val extension = Extension(
                    id = extensionId,
                    name = manifest.name,
                    version = manifest.version,
                    description = manifest.description,
                    author = manifest.author,
                    homepageUrl = manifest.homepageUrl,
                    iconUrl = manifest.icons?.values?.firstOrNull(),
                    permissions = manifest.permissions.orEmpty(),
                    hostPermissions = manifest.hostPermissions.orEmpty(),
                    contentScripts = manifest.contentScripts.orEmpty(),
                    backgroundScripts = manifest.backgroundScripts.orEmpty(),
                    enabled = true,
                    installedAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    source = Extension.Source.CHROME_WEB_STORE,
                    manifestVersion = manifest.manifestVersion
                )
                
                installedExtensions[extensionId] = extension
                registerWebExtension(extension, extensionFile)
                
                Logger.d(TAG, "Installed extension from CRX: ${extension.name}")
                extension
                
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to install from CRX", e)
                null
            }
        }
    }
    
    suspend fun loadUnpackedExtension(
        sourceDir: File,
        manifest: Manifest,
        unpackedDir: File
    ): Extension? {
        return withContext(Dispatchers.IO) {
            try {
                val extensionId = generateExtensionId(sourceDir)
                val targetDir = File(unpackedDir, extensionId)
                
                // Copy extension files to internal storage
                FileUtil.copyDirectory(sourceDir, targetDir)
                
                val extension = Extension(
                    id = extensionId,
                    name = manifest.name,
                    version = manifest.version,
                    description = manifest.description,
                    author = manifest.author,
                    homepageUrl = manifest.homepageUrl,
                    iconUrl = manifest.icons?.values?.firstOrNull(),
                    permissions = manifest.permissions.orEmpty(),
                    hostPermissions = manifest.hostPermissions.orEmpty(),
                    contentScripts = manifest.contentScripts.orEmpty(),
                    backgroundScripts = manifest.backgroundScripts.orEmpty(),
                    enabled = true,
                    installedAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    source = Extension.Source.UNPACKED,
                    manifestVersion = manifest.manifestVersion,
                    unpackedPath = targetDir.absolutePath
                )
                
                installedExtensions[extensionId] = extension
                registerWebExtension(extension, targetDir)
                
                Logger.d(TAG, "Loaded unpacked extension: ${extension.name}")
                extension
                
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to load unpacked extension", e)
                null
            }
        }
    }
    
    private suspend fun registerWebExtension(extension: Extension, source: File) {
        withContext(Dispatchers.Main) {
            try {
                val controller = WebExtensionController(
                    context,
                    extension.id,
                    source.absolutePath,
                    WebExtensionController.Flags.NONE
                )
                
                val webExtension = controller.createWebExtension()
                
                // Register message listener for extension API bridge
                webExtension.setMessageDelegate({ message, sender, respond ->
                    extensionApiBridge.handleMessage(extension, message, sender, respond)
                }, "NyroExtensionAPI")
                
                // Inject content scripts if enabled
                if (extension.enabled) {
                    contentScriptInjector.injectScripts(webExtension, extension)
                    backgroundScriptRunner.startBackgroundScripts(webExtension, extension)
                }
                
                webExtensions[extension.id] = webExtension
                Logger.d(TAG, "Registered WebExtension: ${extension.name}")
                
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to register WebExtension", e)
            }
        }
    }
    
    fun enableExtension(extension: Extension) {
        webExtensions[extension.id]?.let { webExt ->
            contentScriptInjector.injectScripts(webExt, extension)
            backgroundScriptRunner.startBackgroundScripts(webExt, extension)
        }
    }
    
    fun disableExtension(extension: Extension) {
        webExtensions[extension.id]?.let { webExt ->
            contentScriptInjector.removeScripts(webExt, extension)
            backgroundScriptRunner.stopBackgroundScripts(webExt, extension)
        }
    }
    
    fun uninstallExtension(extension: Extension) {
        // Stop background scripts
        webExtensions[extension.id]?.let { webExt ->
            backgroundScriptRunner.stopBackgroundScripts(webExt, extension)
        }
        
        // Remove from maps
        installedExtensions.remove(extension.id)
        webExtensions.remove(extension.id)
        
        // Delete files
        extension.unpackedPath?.let { path ->
            File(path).deleteRecursively()
        }
        
        val crxFile = File(context.filesDir, "extensions/${extension.id}.crx")
        if (crxFile.exists()) crxFile.delete()
        
        Logger.d(TAG, "Uninstalled extension: ${extension.name}")
    }
    
    fun grantPermissions(extension: Extension, permissions: List<String>) {
        // Update extension permissions and reload if needed
        val updated = extension.copy(
            grantedPermissions = (extension.grantedPermissions.orEmpty() + permissions).distinct()
        )
        installedExtensions[extension.id] = updated
        
        if (updated.enabled) {
            webExtensions[extension.id]?.let { webExt ->
                contentScriptInjector.injectScripts(webExt, updated)
            }
        }
    }
    
    fun revokePermissions(extension: Extension, permissions: List<String>) {
        val updated = extension.copy(
            grantedPermissions = extension.grantedPermissions?.filter { it !in permissions }
        )
        installedExtensions[extension.id] = updated
        
        if (updated.enabled) {
            webExtensions[extension.id]?.let { webExt ->
                contentScriptInjector.removeScripts(webExt, updated)
            }
        }
    }
    
    fun shutdown() {
        // Stop all background scripts
        installedExtensions.values.forEach { extension ->
            webExtensions[extension.id]?.let { webExt ->
                backgroundScriptRunner.stopBackgroundScripts(webExt, extension)
            }
        }
        webExtensions.clear()
        installedExtensions.clear()
        Logger.d(TAG, "ExtensionLoader shutdown complete")
    }
    
    private fun generateExtensionId(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(data)
        return hash.joinToString("") { "%02x".format(it) }.take(32)
    }
    
    private fun generateExtensionId(directory: File): String {
        val content = directory.walkTopDown()
            .filter { it.isFile }
            .map { it.readBytes() }
            .fold(ByteArray(0)) { acc, bytes -> acc + bytes }
        return generateExtensionId(content)
    }
}
