package com.nyro.browser.browser

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TabManager @Inject constructor() {
    
    data class Tab(
        val id: String,
        var url: String = "",
        var title: String = "",
        var isLoading: Boolean = false,
        var canGoBack: Boolean = false,
        var canGoForward: Boolean = false,
        var isSecure: Boolean = false
    )
    
    private val tabs = mutableMapOf<String, Tab>()
    
    fun getTab(id: String): Tab? = tabs[id]
    
    fun createTab(id: String): Tab {
        val tab = Tab(id = id)
        tabs[id] = tab
        return tab
    }
    
    fun updateTabLoading(id: String, isLoading: Boolean) {
        tabs[id]?.isLoading = isLoading
    }
    
    fun updateTabUrl(id: String, url: String) {
        tabs[id]?.url = url
    }
    
    fun updateTabTitle(id: String, title: String) {
        tabs[id]?.title = title
    }
    
    fun updateTabSecurity(id: String, isSecure: Boolean) {
        tabs[id]?.isSecure = isSecure
    }
    
    fun updateTabNavigation(id: String, canGoBack: Boolean, canGoForward: Boolean) {
        tabs[id]?.apply {
            this.canGoBack = canGoBack
            this.canGoForward = canGoForward
        }
    }
    
    fun closeTab(id: String) {
        tabs.remove(id)
    }
    
    fun getAllTabs(): List<Tab> = tabs.values.toList()
}
