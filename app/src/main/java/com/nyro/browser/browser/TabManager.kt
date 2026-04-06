package com.nyro.browser.browser

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TabManager @Inject constructor() {
    // Add your tab management logic here
    fun getCurrentTab() = Unit
    fun switchToTab(tabId: String) = Unit
    fun createTab() = Unit
    fun closeTab(tabId: String) = Unit
}
