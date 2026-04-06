package com.nyro.browser.browser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.nyro.browser.extensions.ExtensionManager
import com.nyro.browser.extensions.models.Extension
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class BrowserFragment : Fragment() {
    
    companion object {
        private const val ARG_TAB_ID = "tab_id"
        
        fun newInstance(tabId: String): BrowserFragment {
            return BrowserFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TAB_ID, tabId)
                }
            }
        }
    }
    
    @Inject
    lateinit var tabManager: TabManager
    
    @Inject
    lateinit var extensionManager: ExtensionManager
    
    private var geckoView: GeckoView? = null
    private var geckoSession: GeckoSession? = null
    private var tabId: String? = null
    
    // Observable state for UI updates
    private val _currentUrl = MutableStateFlow("")
    val currentUrl = _currentUrl.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    
    private val _canGoBack = MutableStateFlow(false)
    val canGoBack = _canGoBack.asStateFlow()
    
    private val _canGoForward = MutableStateFlow(false)
    val canGoForward = _canGoForward.asStateFlow()
    
    private val _title = MutableStateFlow("")
    val title = _title.asStateFlow()
    
    private val _isSecure = MutableStateFlow(false)
    val isSecure = _isSecure.asStateFlow()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tabId = arguments?.getString(ARG_TAB_ID)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Create container
        val containerView = FrameLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            id = ViewCompat.generateViewId()
        }
        
        // Create GeckoView
        geckoView = GeckoView(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        
        containerView.addView(geckoView)
        
        return containerView
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeGeckoView()
        setupSessionDelegates()
        loadTab()
        observeExtensions()
    }
    
    private fun initializeGeckoView() {
        geckoSession = GeckoSession()
        
        // Get or create GeckoRuntime
        val runtime = GeckoRuntime.create(requireContext())
        
        // Configure session settings
        val settings = GeckoSession.Settings.Builder()
            .allowJavascript(true)
            .allowFileAccess(false)
            .allowContentAccess(true)
            .userAgentMode(GeckoSession.Settings.USER_AGENT_MODE_ANDROID)
            .trackingProtection(GeckoSession.Settings.TrackingProtection.STRICT)
            .build()
        
        geckoSession?.settings = settings
        
        // Attach session to view
        geckoView?.setSession(geckoSession)
    }
    
    private fun setupSessionDelegates() {
        geckoSession?.progressDelegate = object : GeckoSession.ProgressDelegate {
            override fun onLoadingChange(
                session: GeckoSession,
                isLoading: Boolean
            ) {
                _isLoading.value = isLoading
                tabId?.let { id ->
                    tabManager.updateTabLoading(id, isLoading)
                }
            }
            
            override fun onLocationChange(
                session: GeckoSession,
                url: String?
            ) {
                url?.let {
                    _currentUrl.value = it
                    tabId?.let { id ->
                        tabManager.updateTabUrl(id, it)
                    }
                }
            }
            
            override fun onSecurityChange(
                session: GeckoSession,
                securityInfo: GeckoSession.SecurityInformation
            ) {
                val isSecure = securityInfo.isSecure
                _isSecure.value = isSecure
                tabId?.let { id ->
                    tabManager.updateTabSecurity(id, isSecure)
                }
            }
        }
        
        geckoSession?.contentDelegate = object : GeckoSession.ContentDelegate {
            override fun onTitleChange(
                session: GeckoSession,
                title: String?
            ) {
                title?.let {
                    _title.value = it
                    tabId?.let { id ->
                        tabManager.updateTabTitle(id, it)
                    }
                }
            }
            
            override fun onCrash(session: GeckoSession) {
                // Handle crash - reload page
                session.reload()
            }
        }
        
        geckoSession?.navigationDelegate = object : GeckoSession.NavigationDelegate {
            override fun onCanGoBack(
                session: GeckoSession,
                canGoBack: Boolean
            ) {
                _canGoBack.value = canGoBack
                tabId?.let { id ->
                    tabManager.updateTabNavigation(id, canGoBack, session.canGoForward)
                }
            }
            
            override fun onCanGoForward(
                session: GeckoSession,
                canGoForward: Boolean
            ) {
                _canGoForward.value = canGoForward
                tabId?.let { id ->
                    tabManager.updateTabNavigation(id, session.canGoBack, canGoForward)
                }
            }
        }
    }
    
    private fun loadTab() {
        tabId?.let { id ->
            val tab = tabManager.getTab(id)
            tab?.url?.let { url ->
                geckoSession?.loadUri(url)
            }
        }
    }
    
    private fun observeExtensions() {
        viewLifecycleOwner.lifecycleScope.launch {
            extensionManager.enabledExtensions.collect { extensions ->
                extensions.forEach { extension ->
                    injectContentScripts(extension)
                }
            }
        }
    }
    
    private fun injectContentScripts(extension: Extension) {
        extension.contentScripts.forEach { script ->
            val currentUri = geckoSession?.currentUri ?: return
            if (script.matches.any { pattern -> currentUri.toString().matches(pattern.toRegex()) }) {
                script.js?.forEach { jsFile ->
                    val scriptContent = loadExtensionResource(extension, jsFile)
                    if (scriptContent.isNotEmpty()) {
                        geckoSession?.evaluateJS(scriptContent)
                    }
                }
            }
        }
    }
    
    private fun loadExtensionResource(extension: Extension, path: String): String {
        return try {
            val file = File(extension.unpackedPath ?: return "", path)
            if (file.exists()) file.readText() else ""
        } catch (e: Exception) {
            ""
        }
    }
    
    fun navigate(url: String) {
        geckoSession?.loadUri(url)
    }
    
    fun goBack() {
        geckoSession?.goBack()
    }
    
    fun goForward() {
        geckoSession?.goForward()
    }
    
    fun reload() {
        geckoSession?.reload()
    }
    
    fun stop() {
        geckoSession?.stop()
    }
    
    fun getCurrentUrl(): String = _currentUrl.value
    
    fun isLoading(): Boolean = _isLoading.value
    
    fun canGoBack(): Boolean = _canGoBack.value
    
    fun canGoForward(): Boolean = _canGoForward.value
    
    override fun onResume() {
        super.onResume()
        geckoView?.onResume()
        geckoSession?.open()
    }
    
    override fun onPause() {
        super.onPause()
        geckoView?.onPause()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        geckoSession?.close()
        geckoView?.setSession(null)
        geckoView = null
        geckoSession = null
    }
}
