package com.nyro.browser.browser

import android.webkit.WebView
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TabManager @Inject constructor() {
    
    private val tabs = mutableMapOf<String, WebView>()
    private var currentTabId: String? = null
    
    fun createTab(tabId: String, webView: WebView) {
        tabs[tabId] = webView
        currentTabId = tabId
    }
    
    fun getCurrentTab(): WebView? {
        return currentTabId?.let { tabs[it] }
    }
    
    fun getTab(tabId: String): WebView? {
        return tabs[tabId]
    }
    
    fun closeTab(tabId: String) {
        tabs.remove(tabId)
        if (currentTabId == tabId) {
            currentTabId = tabs.keys.firstOrNull()
        }
    }
    
    fun switchToTab(tabId: String) {
        if (tabs.containsKey(tabId)) {
            currentTabId = tabId
        }
    }
    
    fun getAllTabs(): List<WebView> {
        return tabs.values.toList()
    }
    
    fun getTabCount(): Int = tabs.size
}
