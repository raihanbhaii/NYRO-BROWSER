package com.nyro.browser

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.nyro.browser.browser.BrowserFragment
import com.nyro.browser.browser.TabManager
import com.nyro.browser.databinding.ActivityMainBinding
import com.nyro.browser.extensions.ExtensionManager
import com.nyro.browser.ui.dialogs.ExtensionInstallDialog
import com.nyro.browser.ui.dialogs.SettingsDialog
import com.nyro.browser.utils.PermissionUtils
import com.nyro.browser.utils.Theme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    
    @Inject
    lateinit var tabManager: TabManager
    
    @Inject
    lateinit var extensionManager: ExtensionManager
    
    private lateinit var viewPager: ViewPager2
    private lateinit var adapter: BrowserPagerAdapter
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        handlePermissionResults(permissions)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupViewPager()
        setupBottomNavigation()
        handleIntent(intent)
        applyTheme()
        
        if (savedInstanceState == null) {
            tabManager.createTab("https://www.google.com")
        }
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        
        binding.omniboxView.setOnNavigateListener { url ->
            tabManager.getCurrentTab()?.navigate(url)
        }
        
        binding.omniboxView.setOnSearchListener { query ->
            val searchUrl = "https://duckduckgo.com/?q=${Uri.encode(query)}"
            tabManager.getCurrentTab()?.navigate(searchUrl)
        }
    }
    
    private fun setupViewPager() {
        viewPager = binding.viewPager
        adapter = BrowserPagerAdapter(this)
        viewPager.adapter = adapter
        viewPager.isUserInputEnabled = false
        
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                tabManager.setCurrentTabIndex(position)
                updateToolbarForCurrentTab()
            }
        })
    }
    
    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_tabs -> {
                    showTabSwitcher()
                    true
                }
                R.id.nav_extensions -> {
                    showExtensionManager()
                    true
                }
                R.id.nav_bookmarks -> {
                    showBookmarks()
                    true
                }
                R.id.nav_settings -> {
                    showSettings()
                    true
                }
                else -> false
            }
        }
    }
    
    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_VIEW -> {
                intent.data?.let { uri ->
                    val url = uri.toString()
                    if (url.startsWith("nyro-extension://")) {
                        handleExtensionInstall(uri)
                    } else {
                        tabManager.getCurrentTab()?.navigate(url)
                    }
                }
            }
        }
    }
    
    private fun handleExtensionInstall(uri: Uri) {
        val extensionId = uri.getQueryParameter("id")
        val source = uri.getQueryParameter("source")
        
        if (extensionId != null) {
            ExtensionInstallDialog.newInstance(extensionId, source)
                .show(supportFragmentManager, "extension_install")
        }
    }
    
    private fun applyTheme() {
        val theme = ConfigService.getInstance().getCurrentTheme()
        Theme.applyTheme(this, theme)
    }
    
    private fun updateToolbarForCurrentTab() {
        tabManager.getCurrentTab()?.let { tab ->
            binding.omniboxView.setUrl(tab.url)
            binding.omniboxView.setTitle(tab.title)
            binding.omniboxView.setLoading(tab.isLoading)
            binding.omniboxView.setSecure(tab.isSecure)
        }
    }
    
    private fun showTabSwitcher() {
        // TODO: Implement tab switcher UI
        Toast.makeText(this, "Tab switcher", Toast.LENGTH_SHORT).show()
    }
    
    private fun showExtensionManager() {
        // TODO: Implement extension manager UI
        Toast.makeText(this, "Extension manager", Toast.LENGTH_SHORT).show()
    }
    
    private fun showBookmarks() {
        // TODO: Implement bookmarks UI
        Toast.makeText(this, "Bookmarks", Toast.LENGTH_SHORT).show()
    }
    
    private fun showSettings() {
        SettingsDialog.newInstance().show(supportFragmentManager, "settings")
    }
    
    private fun handlePermissionResults(permissions: Map<String, Boolean>) {
        val denied = permissions.filter { !it.value }.keys
        if (denied.isNotEmpty()) {
            Snackbar.make(
                binding.root,
                "Some permissions were denied. Extension features may be limited.",
                Snackbar.LENGTH_LONG
            ).setAction("Settings") {
                PermissionUtils.openAppSettings(this)
            }.show()
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_new_tab -> {
                tabManager.createTab()
                true
            }
            R.id.action_extensions -> {
                showExtensionManager()
                true
            }
            R.id.action_settings -> {
                showSettings()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK) {
            tabManager.getCurrentTab()?.let { tab ->
                if (tab.canGoBack()) {
                    tab.goBack()
                    true
                } else {
                    super.onKeyDown(keyCode, event)
                }
            } ?: super.onKeyDown(keyCode, event)
        } else {
            super.onKeyDown(keyCode, event)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        tabManager.closeAllTabs()
    }
    
    private inner class BrowserPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = tabManager.getTabCount()
        
        override fun createFragment(position: Int): Fragment {
            val tab = tabManager.getTabAt(position)
            return BrowserFragment.newInstance(tab.id)
        }
        
        override fun getItemId(position: Int): Long {
            return tabManager.getTabAt(position)?.id?.hashCode()?.toLong() ?: position.toLong()
        }
        
        override fun containsItem(itemId: Long): Boolean {
            return tabManager.getAllTabs().any { it.id.hashCode().toLong() == itemId }
        }
    }
}
