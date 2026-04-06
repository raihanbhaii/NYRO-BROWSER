package com.nyro.browser.browser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.nyro.browser.utils.Settings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.mozilla.geckoview.GeckoRuntime
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
    lateinit var settings: Settings
    
    private var geckoView: GeckoView? = null
    private var geckoSession: GeckoSession? = null
    private var tabId: String? = null
    private var runtime: GeckoRuntime? = null
    
    var currentUrl: String = ""
        private set
    var isLoading: Boolean = false
        private set
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tabId = arguments?.getString(ARG_TAB_ID)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FrameLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            id = ViewCompat.generateViewId()
            
            geckoView = GeckoView(requireContext()).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            }
            addView(geckoView)
        }
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupGeckoView()
    }
    
    private fun setupGeckoView() {
        runtime = GeckoRuntime.create(requireContext())
        geckoSession = GeckoSession()
        
        val sessionSettings = GeckoSession.Settings.Builder()
            .allowJavascript(settings.isJavaScriptEnabled())
            .build()
        geckoSession?.settings = sessionSettings
        
        setupSessionDelegates()
        geckoSession?.open(runtime)
        geckoView?.setSession(geckoSession)
        
        // Load default page
        geckoSession?.loadUri("https://www.google.com")
    }
    
    private fun setupSessionDelegates() {
        geckoSession?.progressDelegate = object : GeckoSession.ProgressDelegate {
            override fun onLoadingChange(session: GeckoSession, isLoading: Boolean) {
                this@BrowserFragment.isLoading = isLoading
            }
            
            override fun onLocationChange(session: GeckoSession, url: String?) {
                url?.let { currentUrl = it }
            }
        }
    }
    
    fun navigate(url: String) {
        val finalUrl = if (url.startsWith("http")) url else "https://$url"
        geckoSession?.loadUri(finalUrl)
    }
    
    fun goBack() {
        geckoSession?.goBack()
    }
    
    fun canGoBack(): Boolean = geckoSession?.canGoBack ?: false
    
    fun getCurrentUrl(): String = currentUrl
    
    fun isLoading(): Boolean = isLoading
    
    override fun onResume() {
        super.onResume()
        geckoView?.onResume()
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
