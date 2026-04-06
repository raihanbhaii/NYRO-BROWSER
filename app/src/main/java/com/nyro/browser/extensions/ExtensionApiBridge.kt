package com.nyro.browser.extensions

import android.content.Context
import com.nyro.browser.utils.Logger
import com.nyro.browser.extensions.models.Extension
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.mozilla.geckoview.WebExtension
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExtensionApiBridge @Inject constructor(
    @ApplicationContext private val context: Context,
    private val extensionManager: ExtensionManager
) {
    
    companion object {
        private const val TAG = "ExtensionApiBridge"
        private const val API_NAMESPACE = "nyro"
    }
    
    private val json = Json { ignoreUnknownKeys = true }
    
    suspend fun handleMessage(
        extension: Extension,
        message: JsonElement,
        sender: WebExtension.MessageSender,
        respond: (JsonElement) -> Unit
    ) {
        try {
            val obj = message.asJsonObject
            val namespace = obj["namespace"]?.jsonPrimitive?.content
            val method = obj["method"]?.jsonPrimitive?.content
            val args = obj["args"]
            
            when (namespace) {
                API_NAMESPACE -> {
                    handleNyroApi(extension, method, args, sender, respond)
                }
                "runtime" -> {
                    handleRuntimeApi(extension, method, args, sender, respond)
                }
                "tabs" -> {
                    handleTabsApi(extension, method, args, sender, respond)
                }
                "storage" -> {
                    handleStorageApi(extension, method, args, sender, respond)
                }
                "permissions" -> {
                    handlePermissionsApi(extension, method, args, sender, respond)
                }
                else -> {
                    Logger.w(TAG, "Unknown API namespace: $namespace")
                    respond(buildErrorResponse("Unknown API namespace"))
                }
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Error handling extension message", e)
            respond(buildErrorResponse(e.message ?: "Unknown error"))
        }
    }
    
    private suspend fun handleNyroApi(
        extension: Extension,
        method: String?,
        args: JsonElement?,
        sender: WebExtension.MessageSender,
        respond: (JsonElement) -> Unit
    ) {
        when (method) {
            "getPlatformInfo" -> {
                respond(buildJsonObject {
                    put("os", "android")
                    put("arch", "arm64")
                    put("nacl_arch", "arm")
                })
            }
            "requestPermissions" -> {
                val permissions = args?.asJsonObject?.get("permissions")?.jsonArray
                    ?.map { it.jsonPrimitive.content } ?: emptyList()
                
                // In a real implementation, show permission dialog to user
                extensionManager.requestPermissions(extension, permissions)
                
                respond(buildJsonObject { put("granted", true) })
            }
            "removePermissions" -> {
                val permissions = args?.asJsonObject?.get("permissions")?.jsonArray
                    ?.map { it.jsonPrimitive.content } ?: emptyList()
                
                extensionManager.revokePermissions(extension, permissions)
                respond(buildJsonObject { put("removed", true) })
            }
            "openOptionsPage" -> {
                // TODO: Open extension options page
                respond(buildJsonObject { put("success", true) })
            }
            else -> {
                respond(buildErrorResponse("Unknown Nyro API method: $method"))
            }
        }
    }
    
    private suspend fun handleRuntimeApi(
        extension: Extension,
        method: String?,
        args: JsonElement?,
        sender: WebExtension.MessageSender,
        respond: (JsonElement) -> Unit
    ) {
        when (method) {
            "getManifest" -> {
                // Return extension manifest
                respond(buildJsonObject { 
                    put("manifest", extension.toManifestJson()) 
                })
            }
            "getURL" -> {
                val path = args?.jsonPrimitive?.content ?: ""
                respond(buildJsonObject { 
                    put("url", "moz-extension://${extension.id}/$path") 
                })
            }
            "sendMessage" -> {
                // Handle extension-to-extension messaging
                respond(buildJsonObject { put("success", true) })
            }
            "connect" -> {
                // Handle port connection
                respond(buildJsonObject { put("portId", "port_${System.currentTimeMillis()}" ) })
            }
            "openOptionsPage" -> {
                // TODO: Implement
                respond(buildJsonObject { put("success", true) })
            }
            "reload" -> {
                // Reload extension
                extensionManager.disableExtension(extension.id)
                extensionManager.enableExtension(extension.id)
                respond(buildJsonObject { put("success", true) })
            }
            "setUninstallURL" -> {
                // Store uninstall URL for analytics
                respond(buildJsonObject { put("success", true) })
            }
            else -> {
                respond(buildErrorResponse("Unknown runtime API method: $method"))
            }
        }
    }
    
    private suspend fun handleTabsApi(
        extension: Extension,
        method: String?,
        args: JsonElement?,
        sender: WebExtension.MessageSender,
        respond: (JsonElement) -> Unit
    ) {
        when (method) {
            "query" -> {
                // Return list of tabs matching query
                respond(buildJsonObject { 
                    put("tabs", buildJsonArray {
                        // TODO: Query actual tabs from browser
                    }) 
                })
            }
            "create" -> {
                // Create new tab
                respond(buildJsonObject { 
                    put("tab", buildJsonObject {
                        put("id", System.currentTimeMillis())
                        put("url", args?.asJsonObject?.get("url")?.jsonPrimitive?.content ?: "about:blank")
                    }) 
                })
            }
            "update" -> {
                // Update tab properties
                respond(buildJsonObject { put("success", true) })
            }
            "remove" -> {
                // Close tab
                respond(buildJsonObject { put("success", true) })
            }
            "reload" -> {
                // Reload tab
                respond(buildJsonObject { put("success", true) })
            }
            "duplicate" -> {
                // Duplicate tab
                respond(buildJsonObject { put("success", true) })
            }
            "get" -> {
                // Get tab by ID
                respond(buildJsonObject { put("tab", buildJsonObject {}) })
            }
            "getCurrent" -> {
                // Get current tab
                respond(buildJsonObject { put("tab", buildJsonObject {}) })
            }
            "connect" -> {
                // Connect to tab
                respond(buildJsonObject { put("port", buildJsonObject {}) })
            }
            "sendMessage" -> {
                // Send message to tab
                respond(buildJsonObject { put("success", true) })
            }
            "highlight" -> {
                // Highlight tabs
                respond(buildJsonObject { put("window", buildJsonObject {}) })
            }
            "move" -> {
                // Move tab
                respond(buildJsonObject { put("success", true) })
            }
            "zoom" -> {
                // Zoom tab
                respond(buildJsonObject { put("success", true) })
            }
            "detectLanguage" -> {
                // Detect page language
                respond(buildJsonObject { put("languages", buildJsonArray {}) })
            }
            "captureVisibleTab" -> {
                // Capture tab screenshot
                respond(buildJsonObject { put("dataUrl", "") })
            }
            "executeScript" -> {
                // Execute script in tab
                respond(buildJsonObject { put("result", buildJsonArray {}) })
            }
            "insertCSS" -> {
                // Insert CSS into tab
                respond(buildJsonObject { put("success", true) })
            }
            "removeCSS" -> {
                // Remove CSS from tab
                respond(buildJsonObject { put("success", true) })
            }
            "setZoom" -> {
                // Set tab zoom
                respond(buildJsonObject { put("success", true) })
            }
            "getZoom" -> {
                // Get tab zoom
                respond(buildJsonObject { put("zoomFactor", 1.0) })
            }
            "getZoomSettings" -> {
                // Get tab zoom settings
                respond(buildJsonObject { 
                    put("mode", "automatic")
                    put("defaultZoomFactor", 1.0)
                    put("scope", "PER_TAB")
                })
            }
            "goBack" -> {
                // Navigate back
                respond(buildJsonObject { put("success", true) })
            }
            "goForward" -> {
                // Navigate forward
                respond(buildJsonObject { put("success", true) })
            }
            "print" -> {
                // Print tab
                respond(buildJsonObject { put("success", true) })
            }
            else -> {
                respond(buildErrorResponse("Unknown tabs API method: $method"))
            }
        }
    }
    
    private suspend fun handleStorageApi(
        extension: Extension,
        method: String?,
        args: JsonElement?,
        sender: WebExtension.MessageSender,
        respond: (JsonElement) -> Unit
    ) {
        // TODO: Implement storage API with SharedPreferences or Room
        when (method) {
            "get" -> {
                respond(buildJsonObject { put("data", buildJsonObject {}) })
            }
            "set" -> {
                respond(buildJsonObject { put("success", true) })
            }
            "remove" -> {
                respond(buildJsonObject { put("success", true) })
            }
            "clear" -> {
                respond(buildJsonObject { put("success", true) })
            }
            "getBytesInUse" -> {
                respond(buildJsonObject { put("bytesInUse", 0) })
            }
            else -> {
                respond(buildErrorResponse("Unknown storage API method: $method"))
            }
        }
    }
    
    private suspend fun handlePermissionsApi(
        extension: Extension,
        method: String?,
        args: JsonElement?,
        sender: WebExtension.MessageSender,
        respond: (JsonElement) -> Unit
    ) {
        when (method) {
            "getAll" -> {
                respond(buildJsonObject { 
                    put("permissions", extension.grantedPermissions ?: extension.permissions)
                    put("origins", extension.hostPermissions)
                })
            }
            "contains" -> {
                val permissions = args?.asJsonObject?.get("permissions")?.jsonArray
                    ?.map { it.jsonPrimitive.content } ?: emptyList()
                val hasAll = permissions.all { extension.hasPermission(it) }
                respond(buildJsonObject { put("result", hasAll) })
            }
            "request" -> {
                val permissions = args?.asJsonObject?.get("permissions")?.jsonArray
                    ?.map { it.jsonPrimitive.content } ?: emptyList()
                // Show permission dialog to user
                extensionManager.requestPermissions(extension, permissions)
                respond(buildJsonObject { put("granted", true) })
            }
            "remove" -> {
                val permissions = args?.asJsonObject?.get("permissions")?.jsonArray
                    ?.map { it.jsonPrimitive.content } ?: emptyList()
                extensionManager.revokePermissions(extension, permissions)
                respond(buildJsonObject { put("removed", true) })
            }
            else -> {
                respond(buildErrorResponse("Unknown permissions API method: $method"))
            }
        }
    }
    
    private fun buildErrorResponse(message: String): JsonElement {
        return buildJsonObject {
            put("error", buildJsonObject {
                put("message", message)
            })
        }
    }
    
    private fun Extension.toManifestJson(): JsonElement {
        return buildJsonObject {
            put("manifest_version", manifestVersion)
            put("name", name)
            put("version", version)
            description?.let { put("description", it) }
            author?.let { put("author", it) }
            homepageUrl?.let { put("homepage_url", it) }
            iconUrl?.let { put("icon", it) }
            put("permissions", permissions)
            put("host_permissions", hostPermissions)
        }
    }
}
