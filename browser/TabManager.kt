package com.nyro.browser.browser

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TabManager @Inject constructor() {

    data class Tab(
        val id: String,
        var url: String = "",
        var title: String = "New Tab",
        var isLoading: Boolean = false,
        var canGoBack: Boolean = false,
        var canGoForward: Boolean = false,
        var isSecure: Boolean = false
    )

    private val _tabs = mutableMapOf<String, Tab>()
    val tabs: Map<String, Tab> get() = _tabs.toMap()

    fun createTab(id: String): Tab {
        val tab = Tab(id = id)
        _tabs[id] = tab
        return tab
    }

    fun getTab(id: String): Tab? = _tabs[id]

    fun updateTab(id: String, block: Tab.() -> Unit) {
        _tabs[id]?.apply(block)
    }

    fun closeTab(id: String) {
        _tabs.remove(id)
    }

    fun getAllTabs(): List<Tab> = _tabs.values.toList()

    fun getTabCount(): Int = _tabs.size
}
