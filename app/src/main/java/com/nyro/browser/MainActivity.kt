package com.nyro.browser

import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.nyro.browser.browser.BrowserFragment
import com.nyro.browser.views.OmniboxView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    private lateinit var toolbar: Toolbar
    private lateinit var viewPager: ViewPager2
    private lateinit var omniboxView: OmniboxView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var mainContainer: LinearLayout
    private var tabCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUI()
        setupToolbar()
        setupViewPager()
    }

    private fun setupUI() {
        mainContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        
        toolbar = Toolbar(this).apply {
            id = ViewCompat.generateViewId()
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            title = "NYRO Browser"
            setTitleTextColor(android.graphics.Color.WHITE)
            setBackgroundColor(android.graphics.Color.parseColor("#2196F3"))
        }
        
        omniboxView = OmniboxView(this).apply {
            id = ViewCompat.generateViewId()
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        
        viewPager = ViewPager2(this).apply {
            id = ViewCompat.generateViewId()
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }
        
        bottomNavigation = BottomNavigationView(this).apply {
            id = ViewCompat.generateViewId()
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setupBottomMenu()
        }
        
        mainContainer.addView(toolbar)
        mainContainer.addView(omniboxView)
        mainContainer.addView(viewPager)
        mainContainer.addView(bottomNavigation)
        
        setContentView(mainContainer)
        
        ViewCompat.setOnApplyWindowInsetsListener(mainContainer) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun BottomNavigationView.setupBottomMenu() {
        val menu = this.menu
        menu.add(0, 1, 0, "New Tab").apply {
            setIcon(android.R.drawable.ic_menu_add)
        }
        menu.add(0, 2, 1, "Tabs").apply {
            setIcon(android.R.drawable.ic_menu_edit)
        }
        menu.add(0, 3, 2, "Settings").apply {
            setIcon(android.R.drawable.ic_menu_preferences)
        }
        
        setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                1 -> createNewTab()
                2 -> showTabManager()
                3 -> showSettings()
            }
            true
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        
        omniboxView.setOnNavigateListener { url ->
            getActiveFragment()?.navigate(url)
        }
    }

    private fun setupViewPager() {
        viewPager.adapter = BrowserAdapter(this)
        viewPager.isUserInputEnabled = false
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateToolbarForCurrentTab()
            }
        })
    }

    private fun createNewTab() {
        tabCount++
        viewPager.adapter?.notifyItemInserted(tabCount - 1)
        viewPager.currentItem = tabCount - 1
    }

    private fun getActiveFragment(): BrowserFragment? {
        val pos = viewPager.currentItem
        return supportFragmentManager.findFragmentByTag("f$pos") as? BrowserFragment
    }

    private fun updateToolbarForCurrentTab() {
       getActiveFragment()?.let { fragment ->
    omniboxView.setUrl(fragment.currentUrl)        // ← fixed
    omniboxView.setLoading(fragment.isLoading())
}

    private fun showTabManager() {
        // TODO: implement tab switcher
    }

    private fun showSettings() {
        // TODO: implement settings
    }

    override fun onBackPressed() {
        val fragment = getActiveFragment()
        if (fragment?.canGoBack() == true) {
            fragment.goBack()
        } else {
            super.onBackPressed()
        }
    }

    private inner class BrowserAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount() = tabCount
        override fun createFragment(position: Int): Fragment {
            return BrowserFragment.newInstance(position)
        }
        override fun getItemId(position: Int) = position.toLong()
        override fun containsItem(itemId: Long) = itemId < tabCount.toLong()
    }
}
