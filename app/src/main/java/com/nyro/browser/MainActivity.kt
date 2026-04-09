package com.nyro.browser.browser

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import com.nyro.browser.NyroApplication
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView

class BrowserFragment : Fragment() {

    companion object {
        private const val ARG_TAB_ID = "tab_id"
        fun newInstance(tabId: Int) = BrowserFragment().apply {
            arguments = Bundle().apply { putInt(ARG_TAB_ID, tabId) }
        }
    }

    private var geckoView: GeckoView? = null
    private var geckoSession: GeckoSession? = null
    private var runtime: GeckoRuntime? = null
    private var homeScreen: View? = null
    private var isDesktopSite = false
    private var isHomeVisible = true

    var currentUrl: String = ""
        private set
    var loading: Boolean = false
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

        geckoView = GeckoView(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            visibility = View.GONE
        }

        homeScreen = buildHomeScreen()

        root.addView(geckoView)
        root.addView(homeScreen)

        return root
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
                setPadding(32, 80, 32, 48)
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )

                // Google logo text
                addView(TextView(requireContext()).apply {
                    text = "Google"
                    textSize = 52f
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).also { it.bottomMargin = 32 }
                    // Colorful Google-style letters
                    val colored = android.text.SpannableString("Google")
                    colored.setSpan(android.text.style.ForegroundColorSpan(Color.parseColor("#4285F4")), 0, 1, 0)
                    colored.setSpan(android.text.style.ForegroundColorSpan(Color.parseColor("#EA4335")), 1, 2, 0)
                    colored.setSpan(android.text.style.ForegroundColorSpan(Color.parseColor("#FBBC05")), 2, 3, 0)
                    colored.setSpan(android.text.style.ForegroundColorSpan(Color.parseColor("#4285F4")), 3, 4, 0)
                    colored.setSpan(android.text.style.ForegroundColorSpan(Color.parseColor("#34A853")), 4, 5, 0)
                    colored.setSpan(android.text.style.ForegroundColorSpan(Color.parseColor("#EA4335")), 5, 6, 0)
                    text = colored
                })

                // Shortcuts label
                addView(TextView(requireContext()).apply {
                    text = "Shortcuts"
                    textSize = 11f
                    setTextColor(Color.parseColor("#9aa0a6"))
                    letterSpacing = 0.1f
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).also {
                        it.bottomMargin = 12
                        it.topMargin = 8
                    }
                })

                // Shortcut grid
                val shortcuts = listOf(
                    Triple("YT", "YouTube", "https://youtube.com"),
                    Triple("R", "Reddit", "https://reddit.com"),
                    Triple("GH", "GitHub", "https://github.com"),
                    Triple("X", "Twitter", "https://twitter.com"),
                    Triple("W", "Wikipedia", "https://wikipedia.org"),
                    Triple("A", "Amazon", "https://amazon.com"),
                    Triple("N", "Netflix", "https://netflix.com"),
                    Triple("M", "Maps", "https://maps.google.com")
                )

                val grid = GridLayout(requireContext()).apply {
                    columnCount = 4
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).also { it.bottomMargin = 36 }
                }

                shortcuts.forEach { (abbr, name, url) ->
                    val cell = LinearLayout(requireContext()).apply {
                        orientation = LinearLayout.VERTICAL
                        gravity = Gravity.CENTER
                        val spec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
                        layoutParams = GridLayout.LayoutParams().apply {
                            columnSpec = spec
                            width = 0
                            setMargins(8, 8, 8, 8)
                        }
                        setOnClickListener { navigate(url) }
                        isClickable = true
                        isFocusable = true

                        // Icon circle
                        addView(TextView(requireContext()).apply {
                            text = abbr
                            textSize = 16f
                            typeface = Typeface.DEFAULT_BOLD
                            setTextColor(Color.WHITE)
                            gravity = Gravity.CENTER
                            background = GradientDrawable().apply {
                                shape = GradientDrawable.OVAL
                                setColor(Color.parseColor("#303134"))
                            }
                            layoutParams = LinearLayout.LayoutParams(80, 80).also {
                                it.bottomMargin = 6
                            }
                        })

                        // Label
                        addView(TextView(requireContext()).apply {
                            text = name
                            textSize = 11f
                            setTextColor(Color.parseColor("#9aa0a6"))
                            gravity = Gravity.CENTER
                            maxLines = 1
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                        })
                    }
                    grid.addView(cell)
                }

                addView(grid)

                // Divider
                addView(View(requireContext()).apply {
                    setBackgroundColor(Color.parseColor("#3c3c3c"))
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1
                    ).also { it.bottomMargin = 24 }
                })

                // Discover / recent section label
                addView(TextView(requireContext()).apply {
                    text = "🔍  Start typing in the address bar to search Google"
                    textSize = 13f
                    setTextColor(Color.parseColor("#9aa0a6"))
                    gravity = Gravity.CENTER
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                })
            })
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
                activity?.runOnUiThread {
                    (activity as? com.nyro.browser.MainActivity)?.onUrlChanged(url)
                }
            }

            override fun onProgressChange(session: GeckoSession, progress: Int) {
                if (progress == 100) loading = false
            }

            override fun onPageStop(session: GeckoSession, success: Boolean) {
                loading = false
            }
        }

        geckoSession?.navigationDelegate = object : GeckoSession.NavigationDelegate {
            override fun onLocationChange(
                session: GeckoSession,
                url: String?,
                perms: List<GeckoSession.PermissionDelegate.ContentPermission>
            ) {
                url?.let {
                    currentUrl = it
                    activity?.runOnUiThread {
                        (activity as? com.nyro.browser.MainActivity)?.onUrlChanged(it)
                    }
                }
            }
        }

        geckoSession?.open(runtime!!)
        geckoView?.setSession(geckoSession!!)
    }

    fun navigate(url: String) {
        val finalUrl = when {
            url.startsWith("http://") || url.startsWith("https://") -> url
            url.contains(".") && !url.contains(" ") -> "https://$url"
            else -> "https://www.google.com/search?q=${url.replace(" ", "+")}"
        }
        showBrowser()
        geckoSession?.loadUri(finalUrl)
    }

    fun showHome() {
        isHomeVisible = true
        homeScreen?.visibility = View.VISIBLE
        geckoView?.visibility = View.GONE
        currentUrl = ""
    }

    private fun showBrowser() {
        isHomeVisible = false
        homeScreen?.visibility = View.GONE
        geckoView?.visibility = View.VISIBLE
    }

    fun goBack() {
        geckoSession?.goBack(false)
    }

    fun goForward() {
        geckoSession?.goForward(false)
    }

    fun toggleDesktopSite() {
        isDesktopSite = !isDesktopSite
        val ua = if (isDesktopSite)
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0.0.0 Safari/537.36"
        else ""
        geckoSession?.settings?.userAgentOverride = ua
        if (currentUrl.isNotEmpty()) navigate(currentUrl)
        Toast.makeText(
            requireContext(),
            if (isDesktopSite) "Desktop site" else "Mobile site",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun canGoBack(): Boolean = !isHomeVisible

    fun isLoading(): Boolean = loading

    override fun onDestroyView() {
        super.onDestroyView()
        geckoSession?.close()
        geckoView?.releaseSession()
        geckoView = null
        geckoSession = null
    }
}
