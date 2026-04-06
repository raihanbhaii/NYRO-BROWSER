package com.nyro.browser.extensions

import android.webkit.WebView
import com.nyro.browser.extensions.models.Manifest
import com.nyro.browser.utils.Logger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentScriptInjector @Inject constructor() {
    
    fun injectContentScripts(webView: WebView?, manifest: Manifest, url: String) {
        manifest.contentScripts?.forEach { script ->
            if (shouldInjectForUrl(script.matches, url)) {
                script.js?.forEach { jsFile ->
                    injectJavaScript(webView, jsFile)
                }
                script.css?.forEach { cssFile ->
                    injectCSS(webView, cssFile)
                }
            }
        }
    }
    
    private fun shouldInjectForUrl(matches: List<String>?, url: String): Boolean {
        if (matches.isNullOrEmpty()) return false
        
        return matches.any { pattern ->
            when {
                pattern.contains("*") -> {
                    val regex = pattern.replace("*", ".*").toRegex()
                    regex.matches(url)
                }
                else -> url.contains(pattern)
            }
        }
    }
    
    private fun injectJavaScript(webView: WebView?, jsCode: String) {
        webView?.evaluateJavascript(jsCode, null)
    }
    
    private fun injectCSS(webView: WebView?, cssContent: String) {
        val jsCode = """
            (function() {
                var style = document.createElement('style');
                style.type = 'text/css';
                style.innerHTML = `${cssContent}`;
                document.head.appendChild(style);
            })();
        """.trimIndent()
        
        webView?.evaluateJavascript(jsCode, null)
    }
}
