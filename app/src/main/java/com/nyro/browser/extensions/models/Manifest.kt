package com.nyro.browser.extensions.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Manifest(
    val manifestVersion: Int,
    val name: String,
    val version: String,
    val description: String? = null,
    val author: String? = null,
    val homepageUrl: String? = null,
    val icons: Map<String, String>? = null,
    val action: Action? = null,
    val browserAction: Action? = null,
    val permissions: List<String>? = null,
    val hostPermissions: List<String>? = null,
    val contentScripts: List<ContentScript>? = null,
    val background: Background? = null,
    val contentSecurityPolicy: ContentSecurityPolicy? = null,
    val webAccessibleResources: List<WebAccessibleResource>? = null,
    val optionsPage: String? = null,
    val optionsUI: OptionsUI? = null,
    val commands: Map<String, Command>? = null,
    val externallyConnectable: ExternallyConnectable? = null,
    val minimumChromeVersion: String? = null,
    val updateUrl: String? = null,
    val key: String? = null,
    val oauth2: Oauth2? = null,
    val optionalPermissions: List<String>? = null
) {
    @Serializable
    data class Action(
        val defaultIcon: Map<String, String>? = null,
        val defaultTitle: String? = null,
        val defaultPopup: String? = null
    )
    
    @Serializable
    data class Background(
        val scripts: List<String>? = null,
        val serviceWorker: String? = null,
        val type: String? = null,
        val persistent: Boolean? = null
    )
    
    @Serializable
    data class ContentSecurityPolicy(
        val extensionPages: String? = null,
        val sandbox: String? = null
    )
    
    @Serializable
    data class WebAccessibleResource(
        val resources: List<String>,
        val matches: List<String>? = null,
        val extensionIds: List<String>? = null
    )
    
    @Serializable
    data class OptionsUI(
        val page: String,
        val chromeStyle: Boolean? = null,
        val openInTab: Boolean? = null
    )
    
    @Serializable
    data class Command(
        val suggestedKey: SuggestedKey? = null,
        val description: String? = null
    ) {
        @Serializable
        data class SuggestedKey(
            val default: String? = null,
            val windows: String? = null,
            val mac: String? = null,
            val chromeos: String? = null,
            val linux: String? = null
        )
    }
    
    @Serializable
    data class ExternallyConnectable(
        val matches: List<String>? = null,
        val ids: List<String>? = null,
        val acceptsTlsChannelId: Boolean? = null
    )
    
    @Serializable
    data class Oauth2(
        val clientId: String,
        val scopes: List<String>
    )
}

@Serializable
data class ContentScript(
    val matches: List<String>? = null,
    val js: List<String>? = null,
    val css: List<String>? = null,
    @SerialName("run_at")
    val runAt: String? = null,
    @SerialName("all_frames")
    val allFrames: Boolean? = null,
    @SerialName("exclude_matches")
    val excludeMatches: List<String>? = null,
    @SerialName("include_globs")
    val includeGlobs: List<String>? = null,
    @SerialName("exclude_globs")
    val excludeGlobs: List<String>? = null
)
