package com.nyro.browser.browser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.nyro.browser.R
import com.nyro.browser.databinding.FragmentBrowserBinding
import com.nyro.browser.extensions.ExtensionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView
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
    
    private var _binding: FragmentBrowserBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var geckoView: GeckoView
    private lateinit var geckoSession: GeckoSession
    private var tabId: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tabId = arguments?.getString(ARG_TAB_ID)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBrowserBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeGeckoView()
        setupSessionDelegates()
        loadTab()
        observeExtensions()
    }
    
    private fun initializeGeckoView() {
        geckoView = binding.geckoView
        geckoSession = GeckoSession()
        
        // Configure session settings
        val settings = GeckoSession.Settings.Builder()
            .allowJavascript(true)
            .allowFileAccess(false)
            .allowContentAccess(true)
            .userAgentMode(GeckoSession.Settings.USER_AGENT_MODE_ANDROID)
            .trackingProtection(GeckoSession.Settings.TrackingProtection.STRICT)
            .build()
        
        geckoSession.settings = settings
        
        // Attach session to view
        geckoView.session = geckoSession
    }
    
    private fun setupSessionDelegates() {
        geckoSession.progressDelegate = object : GeckoSession.ProgressDelegate {
            override fun onLoadingChange(
                session: GeckoSession,
                isLoading: Boolean
            ) {
                tabId?.let { id ->
                    tabManager.updateTabLoading(id, isLoading)
                }
            }
            
            override fun onLocationChange(
                session: GeckoSession,
                url: String?
            ) {
                url?.let {
                    tabId?.let { id ->
                        tabManager.updateTabUrl(id, it)
                    }
                }
            }
            
            override fun onSecurityChange(
                session: GeckoSession,
                securityInfo: GeckoSession.SecurityInformation
            ) {
                tabId?.let { id ->
                    tabManager.updateTabSecurity(id, securityInfo.isSecure)
                }
            }
        }
        
        geckoSession.contentDelegate = object : GeckoSession.ContentDelegate {
            override fun onTitleChange(
                session: GeckoSession,
                title: String?
            ) {
                title?.let {
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
        
        geckoSession.navigationDelegate = object : GeckoSession.NavigationDelegate {
            override fun onCanGoBack(
                session: GeckoSession,
                canGoBack: Boolean
            ) {
                tabId?.let { id ->
                    tabManager.updateTabNavigation(id, canGoBack, session.canGoForward)
                }
            }
            
            override fun onCanGoForward(
                session: GeckoSession,
                canGoForward: Boolean
            ) {
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
                geckoSession.loadUri(url)
            }
        }
    }
    
    private fun observeExtensions() {
        viewLifecycleOwner.lifecycleScope.launch {
            extensionManager.enabledExtensions.collect { extensions ->
                // Inject content scripts for enabled extensions
                extensions.forEach { extension ->
                    extension.contentScripts.forEach { script ->
                        if (script.matches.any { pattern -> geckoSession.currentUri?.matchesHost(pattern) == true }) {
                            script.js?.forEach { jsFile ->
                                // Inject content script
                                geckoSession.evalJs(loadExtensionResource(extension, jsFile))
                            }
                        }
                    }
                }
            }
        }
    }
    
    private fun loadExtensionResource(extension: Extension, path: String): String {
        // Load JavaScript file from extension directory
        return try {
            extension.unpackedPath?.let { basePath ->
                val file = java.io.File(basePath, path)
                if (file.exists()) file.readText() else ""
            } ?: ""
        } catch (e: Exception) {
            ""
        }
    }
    
    fun navigate(url: String) {
        geckoSession.loadUri(url)
    }
    
    fun goBack() {
        geckoSession.goBack()
    }
    
    fun goForward() {
        geckoSession.goForward()
    }
    
    fun reload() {
        geckoSession.reload()
    }
    
    fun stop() {
        geckoSession.stop()
    }
    
    override fun onResume() {
        super.onResume()
        geckoView.onResume()
    }
    
    override fun onPause() {
        super.onPause()
        geckoView.onPause()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        geckoSession.close()
    }
}
