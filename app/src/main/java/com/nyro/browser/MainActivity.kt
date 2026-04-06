package com.nyro.browser

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.nyro.browser.browser.BrowserFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewPager: ViewPager2
    private var tabCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupViewPager()
        createNewTab()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbarLayout.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        
        binding.omniboxView.setOnNavigateListener { url ->
            getActiveFragment()?.navigate(url)
        }
        
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_new_tab -> createNewTab()
                R.id.nav_tabs -> showTabManager()
                R.id.nav_settings -> showSettings()
            }
            true
        }
    }

    private fun setupViewPager() {
        viewPager = binding.viewPager
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
            binding.omniboxView.setUrl(fragment.currentUrl)
            binding.omniboxView.setLoading(fragment.isLoading)
        }
    }

    private fun showTabManager() { /* Implement tab switcher */ }
    private fun showSettings() { /* Implement settings */ }

    private inner class BrowserAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount() = tabCount
        override fun createFragment(position: Int): Fragment {
            return BrowserFragment.newInstance(position)
        }
        override fun getItemId(position: Int) = position.toLong()
        override fun containsItem(itemId: Long) = itemId < tabCount.toLong()
    }
}
