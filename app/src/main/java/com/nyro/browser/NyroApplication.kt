package com.nyro.browser

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings

@HiltAndroidApp
class NyroApplication : Application() {

    companion object {
        lateinit var runtime: GeckoRuntime
            private set
    }

    override fun onCreate() {
        super.onCreate()
        initializeGeckoRuntime()
    }

    private fun initializeGeckoRuntime() {
        val settings = GeckoRuntimeSettings.Builder()
            .consoleOutput(BuildConfig.DEBUG)
            .build()
            
        runtime = GeckoRuntime.create(this, settings)
    }
}
