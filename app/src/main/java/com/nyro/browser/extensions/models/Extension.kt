package com.nyro.browser.extensions.models

import kotlinx.serialization.Serializable

@Serializable
data class Extension(
    val id: String,
    val name: String,
    val version: String,
    val description: String? = null,
    val author: String? = null,
    val homepageUrl: String? = null,
    val iconUrl: String? = null,
    val permissions: List<String> = emptyList(),
    val hostPermissions: List<String> = emptyList(),
    val contentScripts: List<ContentScript> = emptyList(),
    val backgroundScripts: List<BackgroundScript> = emptyList(),
    val enabled: Boolean = true,
    val installedAt: Long,
    val updatedAt: Long,
    val source: Source,
    val manifestVersion: Int = 3,
    val unpackedPath: String? = null,
    val grantedPermissions: List<String>? = null
) {
    enum class Source {
        CHROME_WEB_STORE,
        UNPACKED,
        SIDELoaded
    }
    
    fun hasPermission(permission: String): Boolean {
        return (grantedPermissions ?: permissions).contains(permission)
    }
    
    fun matchesHost(url: String): Boolean {
        return hostPermissions.any { pattern ->
            when {
                pattern == "<all_urls>" -> true
                pattern.startsWith("*://") -> {
                    val hostPattern = pattern.removePrefix("*://")
                    url.startsWith(hostPattern) || url.matches(Regex("https?://.*\\.$hostPattern.*"))
                }
                pattern.startsWith("https://") || pattern.startsWith("http://") -> {
                    url.startsWith(pattern)
                }
                pattern.contains("*") -> {
                    val regexPattern = Regex.escape(pattern)
                        .replace("\\*", ".*")
                    url.matches(Regex(regexPattern))
                }
                else -> url.contains(pattern)
            }
        }
    }
}

@Serializable
data class ContentScript(
    val matches: List<String>,
    val js: List<String>? = null,
    val css: List<String>? = null,
    val runAt: RunAt = RunAt.DOCUMENT_IDLE,
    val allFrames: Boolean = false,
    val matchAboutBlank: Boolean = false
) {
    enum class RunAt {
        DOCUMENT_START,
        DOCUMENT_END,
        DOCUMENT_IDLE
    }
}

@Serializable
data class BackgroundScript(
    val scripts: List<String>? = null,
    val serviceWorker: String? = null,
    val type: BackgroundType = BackgroundType.MODULE,
    val persistent: Boolean = false
) {
    enum class BackgroundType {
        MODULE,
        SCRIPT
    }
}
