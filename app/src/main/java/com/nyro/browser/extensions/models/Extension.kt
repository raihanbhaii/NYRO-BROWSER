package com.nyro.browser.extensions.models

import java.io.File

data class Extension(
    val id: String,
    val name: String,
    val version: String,
    val manifest: Manifest,
    val path: File,
    val isEnabled: Boolean = true,
    val permissionsGranted: List<String> = emptyList()
)
