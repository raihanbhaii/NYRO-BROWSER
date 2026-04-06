package com.nyro.browser.di

import android.content.Context
import com.nyro.browser.browser.TabManager
import com.nyro.browser.extensions.*
import com.nyro.browser.utils.ManifestParser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideTabManager(): TabManager = TabManager()
    
    @Provides
    @Singleton
    fun provideManifestParser(): ManifestParser = ManifestParser()
    
    @Provides
    @Singleton
    fun provideContentScriptInjector(): ContentScriptInjector = ContentScriptInjector()
    
    @Provides
    @Singleton
    fun provideBackgroundScriptRunner(@ApplicationContext context: Context): BackgroundScriptRunner = 
        BackgroundScriptRunner(context)
    
    @Provides
    @Singleton
    fun provideExtensionApiBridge(): ExtensionApiBridge = ExtensionApiBridge()
    
    @Provides
    @Singleton
    fun provideChromeWebStoreClient(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient
    ): ChromeWebStoreClient = ChromeWebStoreClient(context, okHttpClient)
    
    @Provides
    @Singleton
    fun provideExtensionManager(
        @ApplicationContext context: Context,
        chromeWebStoreClient: ChromeWebStoreClient,
        manifestParser: ManifestParser
    ): ExtensionManager = ExtensionManager(context, chromeWebStoreClient, manifestParser)
    
    @Provides
    @Singleton
    fun provideExtensionLoader(
        @ApplicationContext context: Context,
        manifestParser: ManifestParser,
        contentScriptInjector: ContentScriptInjector,
        backgroundScriptRunner: BackgroundScriptRunner,
        extensionApiBridge: ExtensionApiBridge
    ): ExtensionLoader = ExtensionLoader(
        context,
        manifestParser,
        contentScriptInjector,
        backgroundScriptRunner,
        extensionApiBridge
    )
}
