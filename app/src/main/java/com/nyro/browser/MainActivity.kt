package com.nyro.browser

import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
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
import com.google.android.material.bottomnavigation.BottomNavigationView.OnNavigationItemSelectedListener
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
        // Create main container
        mainContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        
        // Create toolbar
        toolbar = Toolbar(this).apply {
            id = ViewCompat.generateViewId()
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                android.R.attr.actionBarSize
            )
            title = "NYRO Browser"
            setTitleTextColor(android.graphics.Color.WHITE)
            setBackgroundColor(android.graphics.Color.parseColor("#2196F3"))
        }
        
        // Create OmniboxView
        omniboxView = OmniboxView(this).apply {
            id = ViewCompat.generateViewId()
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        
        // Create ViewPager2
        viewPager = ViewPager2(this).apply {
            id = ViewCompat.generateViewId()
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }
        
        // Create BottomNavigationView
        bottomNavigation = BottomNavigationView(this).apply {
            id = ViewCompat.generateViewId()
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setupBottomMenu()
        }
        
        // Add all views to container
        mainContainer.addView(toolbar)
        mainContainer.addView(omniboxView)
        mainContainer.addView(viewPager)
        mainContainer.addView(bottomNavigation)
        
        setContentView(mainContainer)
        
        // Handle window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(mainContainer) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun BottomNavigationView.setupBottomMenu() {
        val menu = this.menu
        menu.add(0, R.id.nav_new_tab, 0, "New Tab").apply {
            setIcon(android.R.drawable.ic_menu_add)
            setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        }
        menu.add(0, R.id.nav_tabs, 1, "Tabs").apply {
            setIcon(android.R.drawable.ic_menu_edit)
            setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        }
        menu.add(0, R.id.nav_settings, 2, "Settings").apply {
            setIcon(android.R.drawable.ic_menu_preferences)
            setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        }
        
        setOnNavigationItemSelectedListener(OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_new_tab -> createNewTab()
                R.id.nav_tabs -> showTabManager()
                R.id.nav_settings -> showSettings()
            }
            true
        })
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
            omniboxView.setUrl(fragment.getCurrentUrl())
            omniboxView.setLoading(fragment.isLoading())
        }
    }

    private fun showTabManager() {
        // Implement tab switcher
    }

    private fun showSettings() {
        // Implement settings
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
