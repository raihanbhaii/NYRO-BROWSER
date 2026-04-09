package com.nyro.browser.browser

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.nyro.browser.extensions.ExtensionManager
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BrowserFragment : Fragment() {

    companion object {
        private const val ARG_TAB_ID = "tab_id"
        fun newInstance(tabId: Int) = BrowserFragment().apply {
            arguments = Bundle().apply { putInt(ARG_TAB_ID, tabId) }
        }
    }

    @Inject lateinit var extensionManager: ExtensionManager
    @Inject lateinit var adBlocker: AdBlocker

    private var webView: WebView? = null
    private var homeScreen: View? = null
    private var isHomeVisible = true
    private var isDesktopSite = false

    var currentUrl: String = ""
        private set

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = FrameLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#202124"))
        }

        webView = WebView(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            visibility = View.GONE
            setupWebView(this)
        }

        homeScreen = buildHomeScreen()

        root.addView(webView)
        root.addView(homeScreen)
        return root
    }

    private fun setupWebView(wv: WebView) {
        wv.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            loadWithOverviewMode = true
            useWideViewPort = true
            setSupportMultipleWindows(true)
            javaScriptCanOpenWindowsAutomatically = true
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            cacheMode = WebSettings.LOAD_DEFAULT
            mediaPlaybackRequiresUserGesture = false
            allowFileAccess = true
            allowContentAccess = true
            userAgentString = "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
        }

        wv.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest
            ): WebResourceResponse? {
                val url = request.url.toString()
                if (adBlocker.shouldBlock(url)) {
                    return WebResourceResponse("text/plain", "utf-8",
                        "".byteInputStream())
                }
                return super.shouldInterceptRequest(view, request)
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                currentUrl = url
                activity?.runOnUiThread {
                    (activity as? com.nyro.browser.MainActivity)?.onUrlChanged(url)
                }
                // Inject all enabled extensions
                extensionManager.getEnabledExtensions().forEach { ext ->
                    view.evaluateJavascript(ext.script, null)
                }
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                if (request.isForMainFrame) {
                    val errorHtml = """
                        <html><body style="background:#202124;color:#e8eaed;font-family:sans-serif;
                        display:flex;flex-direction:column;align-items:center;justify-content:center;
                        height:100vh;margin:0;text-align:center;">
                        <h2 style="color:#f28b82">Cannot connect</h2>
                        <p style="color:#9aa0a6">${request.url}</p>
                        <p style="color:#9aa0a6">Check your internet connection and try again.</p>
                        </body></html>
                    """.trimIndent()
                    view.loadDataWithBaseURL(null, errorHtml, "text/html", "utf-8", null)
                }
            }
        }

        wv.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                activity?.runOnUiThread {
                    (activity as? com.nyro.browser.MainActivity)?.onProgressChanged(newProgress)
                    val pb = (activity as? com.nyro.browser.MainActivity)
                        ?.omniboxView?.getProgressBar()
                    pb?.visibility = if (newProgress < 100) View.VISIBLE else View.GONE
                    pb?.progress = newProgress
                }
            }

            override fun onReceivedTitle(view: WebView, title: String) {
                currentUrl = view.url ?: currentUrl
            }

            override fun onGeolocationPermissionsShowPrompt(
                origin: String,
                callback: GeolocationPermissions.Callback
            ) {
                callback.invoke(origin, true, false)
            }
        }
    }

    private fun buildHomeScreen(): View {
        return ScrollView(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#202124"))

            addView(LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
                setPadding(32, 100, 32, 48)
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )

                // Google logo
                addView(TextView(requireContext()).apply {
                    textSize = 56f
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).also { it.bottomMargin = 48 }
                    val s = android.text.SpannableString("Google")
                    val colors = listOf("#4285F4","#EA4335","#FBBC05","#4285F4","#34A853","#EA4335")
                    colors.forEachIndexed { i, c ->
                        s.setSpan(android.text.style.ForegroundColorSpan(Color.parseColor(c)), i, i+1, 0)
                    }
                    text = s
                })

                // Shortcuts label
                addView(TextView(requireContext()).apply {
                    text = "SHORTCUTS"
                    textSize = 10f
                    setTextColor(Color.parseColor("#9aa0a6"))
                    letterSpacing = 0.15f
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).also { it.bottomMargin = 16 }
                })

                val shortcuts = listOf(
                    Triple("YT", "YouTube", "https://youtube.com"),
                    Triple("Re", "Reddit", "https://reddit.com"),
                    Triple("GH", "GitHub", "https://github.com"),
                    Triple("Tw", "Twitter", "https://twitter.com"),
                    Triple("Wi", "Wikipedia", "https://wikipedia.org"),
                    Triple("Am", "Amazon", "https://amazon.com"),
                    Triple("Nf", "Netflix", "https://netflix.com"),
                    Triple("Ma", "Maps", "https://maps.google.com")
                )

                val grid = GridLayout(requireContext()).apply {
                    columnCount = 4
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).also { it.bottomMargin = 40 }
                }

                shortcuts.forEach { (abbr, name, url) ->
                    grid.addView(LinearLayout(requireContext()).apply {
                        orientation = LinearLayout.VERTICAL
                        gravity = Gravity.CENTER
                        layoutParams = GridLayout.LayoutParams().apply {
                            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
                            width = 0
                            setMargins(8, 8, 8, 8)
                        }
                        isClickable = true
                        isFocusable = true
                        setOnClickListener { navigate(url) }

                        addView(TextView(requireContext()).apply {
                            text = abbr
                            textSize = 14f
                            typeface = Typeface.DEFAULT_BOLD
                            setTextColor(Color.parseColor("#e8eaed"))
                            gravity = Gravity.CENTER
                            background = GradientDrawable().apply {
                                shape = GradientDrawable.OVAL
                                setColor(Color.parseColor("#303134"))
                            }
                            layoutParams = LinearLayout.LayoutParams(72, 72).also { it.bottomMargin = 8 }
                        })

                        addView(TextView(requireContext()).apply {
                            text = name
                            textSize = 11f
                            setTextColor(Color.parseColor("#9aa0a6"))
                            gravity = Gravity.CENTER
                            maxLines = 1
                        })
                    })
                }
                addView(grid)

                addView(View(requireContext()).apply {
                    setBackgroundColor(Color.parseColor("#3c3c3c"))
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1
                    ).also { it.bottomMargin = 24 }
                })

                addView(TextView(requireContext()).apply {
                    text = "Type in the address bar to search or navigate"
                    textSize = 12f
                    setTextColor(Color.parseColor("#5f6368"))
                    gravity = Gravity.CENTER
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                })
            })
        }
    }

    fun navigate(url: String) {
        val finalUrl = when {
            url.startsWith("http://") || url.startsWith("https://") -> url
            url.contains(".") && !url.contains(" ") -> "https://$url"
            else -> "https://www.google.com/search?q=${url.replace(" ", "+")}"
        }
        showBrowser()
        webView?.loadUrl(finalUrl)
    }

    fun showHome() {
        isHomeVisible = true
        homeScreen?.visibility = View.VISIBLE
        webView?.visibility = View.GONE
        currentUrl = ""
    }

    private fun showBrowser() {
        isHomeVisible = false
        homeScreen?.visibility = View.GONE
        webView?.visibility = View.VISIBLE
    }

    fun goBack() { webView?.goBack() }
    fun goForward() { webView?.goForward() }
    fun canGoBack(): Boolean = if (isHomeVisible) false else webView?.canGoBack() == true

    fun toggleDesktopSite() {
        isDesktopSite = !isDesktopSite
        webView?.settings?.userAgentString = if (isDesktopSite)
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0.0.0 Safari/537.36"
        else
            "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
        if (currentUrl.isNotEmpty()) navigate(currentUrl)
    }

    fun isLoading(): Boolean = webView?.progress != 100

    override fun onDestroyView() {
        super.onDestroyView()
        webView?.destroy()
        webView = null
    }
}
