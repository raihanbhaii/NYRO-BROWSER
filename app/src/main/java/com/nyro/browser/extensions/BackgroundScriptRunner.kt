package com.nyro.browser.extensions

import android.content.Context
import com.nyro.browser.extensions.models.Manifest
import com.nyro.browser.utils.Logger
import kotlinx.coroutines.*
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackgroundScriptRunner @Inject constructor(
    private val context: Context
) {
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    fun runBackgroundScripts(manifest: Manifest, extensionPath: String) {
        val backgroundScripts = manifest.background?.scripts ?: return
        
        backgroundScripts.forEach { script ->
            runScript(extensionPath, script)
        }
    }
    
    private fun runScript(extensionPath: String, scriptPath: String) {
        scope.launch {
            try {
                val scriptContent = loadScriptContent(extensionPath, scriptPath)
                if (scriptContent != null) {
                    executeBackgroundScript(scriptContent)
                }
            } catch (e: Exception) {
                Logger.e("BackgroundScriptRunner", "Error running background script: $scriptPath", e)
            }
        }
    }
    
    private fun loadScriptContent(extensionPath: String, scriptPath: String): String? {
        return try {
            val file = File(extensionPath, scriptPath)
            if (file.exists()) {
                file.readText()
            } else {
                Logger.e("BackgroundScriptRunner", "Background script not found: $scriptPath")
                null
            }
        } catch (e: Exception) {
            Logger.e("BackgroundScriptRunner", "Error loading background script: $scriptPath", e)
            null
        }
    }
    
    private fun executeBackgroundScript(scriptContent: String) {
        // In a real implementation, you would execute this in a JavaScript engine
        // For now, we just log it
        Logger.d("BackgroundScriptRunner", "Executing background script of length: ${scriptContent.length}")
    }
    
    fun stop() {
        scope.cancel()
    }
}
