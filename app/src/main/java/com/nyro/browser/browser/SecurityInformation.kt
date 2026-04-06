package com.nyro.browser.browser

data class SecurityInformation(
    val isSecure: Boolean = false,
    val certificateIssuer: String? = null,
    val certificateSubject: String? = null
)
