package com.nyro.browser

import org.cef.CefApp
import org.cef.CefClient
import org.cef.CefSettings
import org.cef.browser.CefBrowser
import org.cef.handler.CefAppHandlerAdapter
import java.io.File
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.WindowConstants

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        // Set system properties for JCEF
        System.setProperty("jcef.debug", "false")
        
        // Setup CEF settings
        val cefSettings = CefSettings().apply {
            cache_path = File(System.getProperty("user.home"), ".nyro_cache").absolutePath
            windowless_rendering_enabled = false
            locale = "en-US"
            log_file = File(System.getProperty("user.home"), ".nyro_browser.log").absolutePath
            log_severity = CefSettings.LogSeverity.LOGSEVERITY_DISABLE
        }

        // App handler for lifecycle
        val appHandler = object : CefAppHandlerAdapter(arrayOf()) {
            override fun stateHasChanged(state: CefApp.CefAppState) {
                if (state == CefApp.CefAppState.TERMINATED) {
                    System.exit(0)
                }
            }
        }

        // Initialize CEF
        val cefApp = CefApp.getInstance(arrayOf(), cefSettings, appHandler)
        val client = cefApp.createClient()

        // Create browser on EDT (Swing thread)
        SwingUtilities.invokeLater {
            val frame = JFrame("NyroBrowser").apply {
                defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
                setSize(1280, 800)
                minimumSize = java.awt.Dimension(800, 600)
                locationRelativeTo = null
                isVisible = true
            }

            // Load local UI file
            val uiPath = File("ui/index.html").absoluteFile.toURI().toString()
            val browser = client.createBrowser(uiPath, false, false)

            // Add browser to frame
            frame.add(browser.uiComponent)
            frame.isVisible = true
        }
    }
}
