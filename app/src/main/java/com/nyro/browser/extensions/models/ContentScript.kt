package com.nyro.browser.extensions.models

import kotlinx.serialization.Serializable

@Serializable
data class ContentScript(
    val matches: List<String> = emptyList(),
    val js: List<String> = emptyList(),
    val css: List<String> = emptyList(),
    val runAt: String = "document_idle",
    val allFrames: Boolean = false
)
