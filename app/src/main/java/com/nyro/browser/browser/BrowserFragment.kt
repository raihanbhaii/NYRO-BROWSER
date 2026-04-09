package com.nyro.browser.browser

import android.animation.ValueAnimator
import android.graphics.Color
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
    private var progressBar: ProgressBar? = null
    private var homeScreen: View? = null
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
            setBackgroundColor(Color.parseColor("#0f0f0f"))
        }

        // GeckoView (browser)
        geckoView = GeckoView(requireContext()).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            visibility = View.GONE
        }

        // Progress bar at top
        progressBar = ProgressBar(
            requireContext(), null,
            android.R.attr.progressBarStyleHorizontal
        ).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, 6
            ).also { it.gravity = Gravity.TOP }
            progressDrawable = android.graphics.drawable.ClipDrawable(
                android.graphics.drawable.ColorDrawable(Color.parseColor("#4f8ef7")),
                Gravity.START,
                android.graphics.drawable.ClipDrawable.HORIZONTAL
            )
            max = 100
            progress = 0
            visibility = View.GONE
        }

        // Home screen
        homeScreen = buildHomeScreen()

        root.addView(geckoView)
        root.addView(homeScreen)
        root.addView(progressBar)

        return root
    }

    private fun buildHomeScreen(): View {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setBackgroundColor(Color.parseColor("#0f0f0f"))
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            setPadding(48, 120, 48, 48)

            // Logo / greeting
            addView(TextView(requireContext()).apply {
                text = "NYRO"
                textSize = 42f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#4f8ef7"))
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = 8 }
            })

            addView(TextView(requireContext()).apply {
                text = "Your private browser"
                textSize = 14f
                setTextColor(Color.parseColor("#888888"))
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = 48 }
            })

            // Quick access shortcuts
            addView(TextView(requireContext()).apply {
                text = "Quick Access"
                textSize = 12f
                setTextColor(Color.parseColor("#555555"))
                letterSpacing = 0.15f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = 16 }
            })

            val shortcuts = listOf(
                "Google" to "https://google.com",
                "YouTube" to "https://youtube.com",
                "Reddit" to "https://reddit.com",
                "GitHub" to "https://github.com"
            )

            val grid = GridLayout(requireContext()).apply {
                columnCount = 2
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = 40 }
            }

            shortcuts.forEach { (name, url) ->
                val btn = TextView(requireContext()).apply {
                    text = name
                    textSize = 14f
                    setTextColor(Color.WHITE)
                    gravity = Gravity.CENTER
                    setPadding(0, 24, 0, 24)
                    background = android.graphics.drawable.GradientDrawable().apply {
                        setColor(Color.parseColor("#1e1e1e"))
                        cornerRadius = 16f
                    }
                    val params = GridLayout.LayoutParams().apply {
                        width = 0
                        height = GridLayout.LayoutParams.WRAP_CONTENT
                        columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
                        setMargins(8, 8, 8, 8)
                    }
                    layoutParams = params
                    setOnClickListener { navigate(url) }
                }
                grid.addView(btn)
            }

            addView(grid)

            // Privacy note
            addView(TextView(requireContext()).apply {
                text = "🔒  No tracking. No ads. Just browsing."
                textSize = 12f
                setTextColor(Color.parseColor("#444444"))
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
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
                    progressBar?.visibility = View.VISIBLE
                    progressBar?.progress = 10
                }
            }

            override fun onProgressChange(session: GeckoSession, progress: Int) {
                activity?.runOnUiThread {
                    progressBar?.progress = progress
                    if (progress == 100) {
                        loading = false
                        progressBar?.postDelayed({
                            progressBar?.visibility = View.GONE
                            progressBar?.progress = 0
                        }, 300)
                    }
                }
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
                    // Notify MainActivity to update omnibox
                    activity?.runOnUiThread {
                        (activity as? com.nyro.browser.MainActivity)
                            ?.onUrlChanged(it)
                    }
                }
            }
        }

        geckoSession?.open(runtime!!)
        geckoView?.setSession(geckoSession!!)
        // Don't auto-load — show home screen instead
    }

    fun navigate(url: String) {
        val finalUrl = when {
            url.startsWith("http://") || url.startsWith("https://") -> url
            url.contains(".") -> "https://$url"
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
        if (isHomeVisible) return
        geckoSession?.goBack(false)
    }

    fun canGoBack(): Boolean {
        return !isHomeVisible
    }

    fun isLoading(): Boolean = loading

    override fun onDestroyView() {
        super.onDestroyView()
        geckoSession?.close()
        geckoView?.releaseSession()
        geckoView = null
        geckoSession = null
    }
}
