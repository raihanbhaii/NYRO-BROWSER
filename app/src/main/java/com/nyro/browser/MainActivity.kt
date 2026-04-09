package com.nyro.browser

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.nyro.browser.browser.BrowserFragment
import com.nyro.browser.views.OmniboxView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var omniboxView: OmniboxView
    private lateinit var mainContainer: LinearLayout
    private lateinit var tabCountBadge: TextView
    private lateinit var bottomBar: LinearLayout
    private var tabCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.parseColor("#202124")
        window.navigationBarColor = Color.parseColor("#202124")
        setupUI()
        setupViewPager()
    }

    private fun setupUI() {
        mainContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#202124"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        omniboxView = OmniboxView(this).apply {
            id = ViewCompat.generateViewId()
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setOnNavigateListener { input ->
                getActiveFragment()?.navigate(input)
            }
        }

        val progressBar = omniboxView.getProgressBar().apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 6
            )
        }

        viewPager = ViewPager2(this).apply {
            id = ViewCompat.generateViewId()
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
            )
        }

        bottomBar = buildBottomBar()

        mainContainer.addView(omniboxView)
        mainContainer.addView(progressBar)
        mainContainer.addView(viewPager)
        mainContainer.addView(bottomBar)

        setContentView(mainContainer)

        ViewCompat.setOnApplyWindowInsetsListener(mainContainer) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun buildBottomBar(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(Color.parseColor("#202124"))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 8, 0, 8)

            addView(buildIconButton("<", "Back") {
                val fragment = getActiveFragment()
                if (fragment?.canGoBack() == true) fragment.goBack()
            })

            addView(buildIconButton(">", "Forward") {
                getActiveFragment()?.goForward()
            })

            addView(buildIconButton("[ ]", "Home") {
                getActiveFragment()?.showHome()
                omniboxView.setUrl("")
            })

            val tabsBtn = FrameLayout(this@MainActivity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
                )
                isClickable = true
                isFocusable = true
                setOnClickListener { showTabManager() }

                val tabBox = TextView(this@MainActivity).apply {
                    text = "1"
                    textSize = 12f
                    setTextColor(Color.parseColor("#e8eaed"))
                    gravity = Gravity.CENTER
                    typeface = Typeface.DEFAULT_BOLD
                    background = GradientDrawable().apply {
                        setColor(Color.TRANSPARENT)
                        setStroke(2, Color.parseColor("#9aa0a6"))
                        cornerRadius = 6f
                    }
                    layoutParams = FrameLayout.LayoutParams(48, 48).also {
                        it.gravity = Gravity.CENTER
                    }
                }
                tabCountBadge = tabBox
                addView(tabBox)
            }
            addView(tabsBtn)

            addView(buildIconButton("...", "Menu") {
                showMenu()
            })
        }
    }

    private fun buildIconButton(
        label: String,
        contentDesc: String,
        onClick: () -> Unit
    ): TextView {
        return TextView(this).apply {
            text = label
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#e8eaed"))
            gravity = Gravity.CENTER
            contentDescription = contentDesc
            layoutParams = LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f
            )
            setPadding(0, 20, 0, 20)
            setOnClickListener { onClick() }
            isClickable = true
            isFocusable = true
        }
    }

    private fun setupViewPager() {
        viewPager.adapter = BrowserAdapter(this)
        viewPager.isUserInputEnabled = false
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateOmniboxForCurrentTab()
            }
        })
    }

    fun onUrlChanged(url: String) {
        runOnUiThread {
            omniboxView.setUrl(url)
        }
    }

    private fun createNewTab() {
        tabCount++
        tabCountBadge.text = tabCount.toString()
        viewPager.adapter?.notifyItemInserted(tabCount - 1)
        viewPager.currentItem = tabCount - 1
    }

    private fun getActiveFragment(): BrowserFragment? {
        val pos = viewPager.currentItem
        return supportFragmentManager.findFragmentByTag("f$pos") as? BrowserFragment
    }

    private fun updateOmniboxForCurrentTab() {
        getActiveFragment()?.let { fragment ->
            omniboxView.setUrl(fragment.currentUrl)
            omniboxView.setLoading(fragment.isLoading())
        }
    }

    private fun showTabManager() {
        Toast.makeText(this, "$tabCount tab(s) open", Toast.LENGTH_SHORT).show()
    }

    private fun showMenu() {
        val popup = PopupMenu(this, mainContainer)
        popup.menu.add(0, 1, 0, "New Tab")
        popup.menu.add(0, 2, 1, "Bookmarks")
        popup.menu.add(0, 3, 2, "History")
        popup.menu.add(0, 4, 3, "Settings")
        popup.menu.add(0, 5, 4, "Desktop Site")
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> createNewTab()
                2 -> Toast.makeText(this, "Bookmarks coming soon", Toast.LENGTH_SHORT).show()
                3 -> Toast.makeText(this, "History coming soon", Toast.LENGTH_SHORT).show()
                4 -> Toast.makeText(this, "Settings coming soon", Toast.LENGTH_SHORT).show()
                5 -> getActiveFragment()?.toggleDesktopSite()
            }
            true
        }
        popup.show()
    }

    override fun onBackPressed() {
        val fragment = getActiveFragment()
        if (fragment?.canGoBack() == true) {
            fragment.goBack()
        } else {
            super.onBackPressed()
        }
    }

    private inner class BrowserAdapter(activity: AppCompatActivity) :
        FragmentStateAdapter(activity) {
        override fun getItemCount() = tabCount
        override fun createFragment(position: Int) = BrowserFragment.newInstance(position)
        override fun getItemId(position: Int) = position.toLong()
        override fun containsItem(itemId: Long) = itemId < tabCount.toLong()
    }
}
