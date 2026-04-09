package com.nyro.browser.settings

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.parseColor("#202124")

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#202124"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // Toolbar
        root.addView(LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(16, 48, 16, 16)

            addView(TextView(this@SettingsActivity).apply {
                text = "<"
                textSize = 18f
                setTextColor(Color.parseColor("#e8eaed"))
                setPadding(8, 8, 24, 8)
                setOnClickListener { finish() }
            })

            addView(TextView(this@SettingsActivity).apply {
                text = "Settings"
                textSize = 18f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#e8eaed"))
            })
        })

        val scroll = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
            )
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 8, 24, 48)
        }

        fun sectionLabel(text: String) = TextView(this).apply {
            this.text = text
            textSize = 11f
            setTextColor(Color.parseColor("#4f8ef7"))
            letterSpacing = 0.1f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 32, 0, 12)
        }

        fun settingRow(title: String, subtitle: String, widget: android.view.View? = null): LinearLayout {
            return LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, 20, 0, 20)

                val texts = LinearLayout(this@SettingsActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                }
                texts.addView(TextView(this@SettingsActivity).apply {
                    this.text = title
                    textSize = 15f
                    setTextColor(Color.parseColor("#e8eaed"))
                })
                if (subtitle.isNotBlank()) {
                    texts.addView(TextView(this@SettingsActivity).apply {
                        this.text = subtitle
                        textSize = 12f
                        setTextColor(Color.parseColor("#9aa0a6"))
                    })
                }
                addView(texts)
                widget?.let { addView(it) }

                // Divider
                // Added outside
            }
        }

        fun divider() = View(this).apply {
            setBackgroundColor(Color.parseColor("#303134"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1
            )
        }

        val prefs = getSharedPreferences("nyro_settings", MODE_PRIVATE)

        // Privacy section
        content.addView(sectionLabel("PRIVACY"))

        val adblockSwitch = Switch(this).apply {
            isChecked = prefs.getBoolean("adblock_enabled", true)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean("adblock_enabled", checked).apply()
            }
        }
        content.addView(settingRow("Ad Blocker", "Block ads and trackers", adblockSwitch))
        content.addView(divider())

        val doNotTrackSwitch = Switch(this).apply {
            isChecked = prefs.getBoolean("do_not_track", true)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean("do_not_track", checked).apply()
            }
        }
        content.addView(settingRow("Do Not Track", "Send DNT header to websites", doNotTrackSwitch))
        content.addView(divider())

        val cookieSwitch = Switch(this).apply {
            isChecked = prefs.getBoolean("accept_cookies", true)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean("accept_cookies", checked).apply()
            }
        }
        content.addView(settingRow("Accept Cookies", "Allow websites to store cookies", cookieSwitch))
        content.addView(divider())

        // Appearance
        content.addView(sectionLabel("APPEARANCE"))

        val darkSwitch = Switch(this).apply {
            isChecked = prefs.getBoolean("force_dark", false)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean("force_dark", checked).apply()
            }
        }
        content.addView(settingRow("Force Dark Mode", "Force dark mode on all pages", darkSwitch))
        content.addView(divider())

        val desktopSwitch = Switch(this).apply {
            isChecked = prefs.getBoolean("desktop_mode", false)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean("desktop_mode", checked).apply()
            }
        }
        content.addView(settingRow("Desktop Mode", "Request desktop version of pages", desktopSwitch))
        content.addView(divider())

        // Search engine
        content.addView(sectionLabel("SEARCH"))
        content.addView(settingRow("Search Engine", "Google (default)", null))
        content.addView(divider())

        // About
        content.addView(sectionLabel("ABOUT"))
        content.addView(settingRow("Nyro Browser", "Version 1.0.0", null))
        content.addView(divider())
        content.addView(settingRow("Built on", "Chromium WebView (Blink engine)", null))

        scroll.addView(content)
        root.addView(scroll)
        setContentView(root)
    }
}
