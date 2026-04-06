package com.nyro.browser

import org.cef.CefApp
import org.cef.CefClient
import org.cef.CefSettings
import org.cef.browser.CefBrowser
import org.cef.handler.CefAppHandlerAdapter
import java.io.File
import javax.swing.JFrame
import javax.swing.WindowConstants

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        // 1. Setup JCEF
        val cefSettings = CefSettings()
        cefSettings.cache_path = File(System.getProperty("user.home"), ".nyro_cache").path
        cefSettings.windowless_rendering_enabled = false

        val appHandler = object : CefAppHandlerAdapter(args) {
            override fun stateHasChanged(state: CefApp.CefAppState) {
                if (state == CefApp.CefAppState.TERMINATED) System.exit(0)
            }
        }

        // 2. Initialize App
        val cefApp = CefApp.getInstance(args, cefSettings, appHandler)
        val cefClient = cefApp.createClient()

        // 3. Create Main Window
        val frame = JFrame("NyroBrowser").apply {
            defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            size = java.awt.Dimension(1280, 800)
            locationRelativeTo = null
            isVisible = true
        }

        // 4. Create Browser
        val browser: CefBrowser = cefClient.createBrowser(
            "https://nyro.local/ui/index.html", // Loads local UI
            false, // Windowless
            false // Transparent
        )

        // 5. Add to Window
        frame.add(browser.uiComponent)
        frame.isVisible = true
    }
}
