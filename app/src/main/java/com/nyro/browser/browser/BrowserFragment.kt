package com.nyro.browser.browser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import com.nyro.browser.NyroApplication
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView

class BrowserFragment : Fragment() {

    companion object {
        private const val ARG_TAB_ID = "tab_id"

        fun newInstance(tabId: Int): BrowserFragment {
            return BrowserFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_TAB_ID, tabId)
                }
            }
        }
    }

    private var geckoView: GeckoView? = null
    private var geckoSession: GeckoSession? = null
    private var runtime: GeckoRuntime? = null

    var currentUrl: String = ""
        private set
    var loading: Boolean = false
        private set

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
        runtime = NyroApplication.runtime

        geckoSession = GeckoSession()

        geckoSession?.progressDelegate = object : GeckoSession.ProgressDelegate {
            override fun onPageStart(session: GeckoSession, url: String) {
                loading = true
                currentUrl = url
            }

            // Updated to handle nullable success parameter
            override fun onPageStop(session: GeckoSession, success: Boolean?) {
                loading = false
            }
        }

        geckoSession?.navigationDelegate = object : GeckoSession.NavigationDelegate {
            override fun onLocationChange(
                session: GeckoSession,
                url: String?,
                perms: MutableList<GeckoSession.PermissionDelegate.ContentPermission>?
            ) {
                url?.let { currentUrl = it }
            }
        }

        runtime?.let {
            geckoSession?.open(it)
            geckoView?.setSession(geckoSession!!)
            geckoSession?.loadUri("https://www.google.com")
        }
    }

    fun navigate(url: String) {
        val finalUrl = if (url.startsWith("http")) url else "https://$url"
        geckoSession?.loadUri(finalUrl)
    }

    fun goBack() {
        geckoSession?.goBack()
    }

    fun canGoBack(): Boolean = currentUrl.isNotEmpty()

    fun getCurrentUrl(): String = currentUrl

    fun isLoading(): Boolean = loading

    override fun onDestroyView() {
        super.onDestroyView()
        geckoSession?.close()
        geckoView?.releaseSession()
        geckoView = null
        geckoSession = null
    }
}
