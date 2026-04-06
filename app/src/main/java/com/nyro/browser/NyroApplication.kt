package com.nyro.browser

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import androidx.work.WorkManager
import com.nyro.browser.extensions.ExtensionManager
import com.nyro.browser.services.ConfigService
import com.nyro.browser.utils.Logger
import dagger.hilt.android.HiltAndroidApp
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings

@HiltAndroidApp
class NyroApplication : Application(), Configuration.Provider {
    
    companion object {
        private const val TAG = "NyroApplication"
        lateinit var instance: NyroApplication
            private set
        
        fun getContext(): Context = instance.applicationContext
        
        private var geckoRuntime: GeckoRuntime? = null
        
        fun getGeckoRuntime(): GeckoRuntime {
            return geckoRuntime ?: throw IllegalStateException("GeckoRuntime not initialized")
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        Logger.init(BuildConfig.DEBUG)
        Logger.d(TAG, "Application initialized")
        
        initializeGeckoRuntime()
        initializeServices()
        initializeWorkManager()
    }
    
    private fun initializeGeckoRuntime() {
        val builder = GeckoRuntimeSettings.Builder()
            .consoleOutputEnabled(BuildConfig.DEBUG)
            .debuggingEnabled(BuildConfig.DEBUG)
            .crashReporterEnabled(true)
            .aboutConfigEnabled(BuildConfig.DEBUG)
            .webExtensionsEnabled(true)
            .remoteDebuggingEnabled(BuildConfig.DEBUG)
        
        // Configure extension support
        if (BuildConfig.SUPPORT_CHROME_EXTENSIONS) {
            builder.webExtensionsEnabled(true)
            builder.extensionsBaseUri("resource://nyro-extensions/")
        }
        
        val settings = builder.build()
        geckoRuntime = GeckoRuntime.create(this, settings)
        
        Logger.d(TAG, "GeckoRuntime initialized with WebExtensions support")
    }
    
    private fun initializeServices() {
        ConfigService.initialize(this)
        ExtensionManager.initialize(this)
        Logger.d(TAG, "Core services initialized")
    }
    
    private fun initializeWorkManager() {
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
        WorkManager.initialize(this, config)
        Logger.d(TAG, "WorkManager initialized")
    }
    
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) android.util.Log.DEBUG else android.util.Log.INFO)
            .build()
    }
    
    fun shutdown() {
        ExtensionManager.getInstance().shutdown()
        geckoRuntime?.shutdown()
        Logger.d(TAG, "Application shutdown complete")
    }
}
