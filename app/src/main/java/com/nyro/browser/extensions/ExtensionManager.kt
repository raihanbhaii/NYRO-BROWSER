package com.nyro.browser.extensions

import android.content.Context
import com.nyro.browser.extensions.models.Extension
import com.nyro.browser.utils.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.mozilla.geckoview.WebExtension
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExtensionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val chromeWebStoreClient: ChromeWebStoreClient,
    private val manifestParser: ManifestParser
) {
    companion object {
        private const val TAG = "ExtensionManager"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _extensions = MutableStateFlow<List<Extension>>(emptyList())
    val extensions: StateFlow<List<Extension>> = _extensions.asStateFlow()

    private val extensionDir = File(context.filesDir, "extensions").apply { mkdirs() }
    private val unpackedDir = File(context.filesDir, "extensions/unpacked").apply { mkdirs() }

    init { loadExtensions() }

    private fun loadExtensions() {
        scope.launch {
            val installed = extensionDir.listFiles()?.filter { it.extension == "crx" } ?: emptyList()
            val unpacked = unpackedDir.listFiles()?.filter { it.isDirectory } ?: emptyList()
            val loaded = (installed + unpacked).mapNotNull { parseExtension(it) }
            _extensions.value = loaded
        }
    }

    private suspend fun parseExtension(file: File): Extension? {
        return try {
            if (file.extension == "crx") {
                val manifest = manifestParser.parseFromCrx(file.readBytes())
                Extension.fromManifest(manifest, file.absolutePath)
            } else {
                val manifestFile = File(file, "manifest.json")
                if (manifestFile.exists()) {
                    val manifest = manifestParser.parse(manifestFile)
                    Extension.fromManifest(manifest, file.absolutePath)
                } else null
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to parse extension: ${file.name}", e)
            null
        }
    }

    fun installFromChromeStore(extensionId: String) {
        scope.launch {
            try {
                val crx = chromeWebStoreClient.downloadCrx(extensionId)
                val dest = File(extensionDir, "$extensionId.crx")
                dest.writeBytes(crx)
                loadExtensions()
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to download extension", e)
            }
        }
    }

    fun registerWithGecko(extension: Extension): WebExtension? {
        return try {
            val controller = org.mozilla.geckoview.WebExtensionController(
                context, extension.id, extension.sourcePath
            )
            controller.createWebExtension()
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to register WebExtension", e)
            null
        }
    }
}
