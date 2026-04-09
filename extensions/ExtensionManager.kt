package com.nyro.browser.extensions

import android.content.Context
import com.nyro.browser.extensions.ExtensionManager.Extension
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExtensionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    data class Extension(
        val id: String,
        val name: String,
        val description: String,
        val version: String,
        val script: String,
        var enabled: Boolean = true
    )

    private val extensionsDir: File
        get() = File(context.filesDir, "extensions").also { it.mkdirs() }

    private val extensions = mutableListOf<Extension>()

    init {
        loadExtensions()
        installBuiltinExtensions()
    }

    private fun installBuiltinExtensions() {
        // Dark mode extension
        if (extensions.none { it.id == "builtin_darkmode" }) {
            installExtension(
                id = "builtin_darkmode",
                name = "Dark Mode",
                description = "Forces dark mode on all websites",
                version = "1.0",
                script = """
                    (function() {
                        var style = document.createElement('style');
                        style.innerHTML = 'html { filter: invert(1) hue-rotate(180deg) !important; } img, video { filter: invert(1) hue-rotate(180deg) !important; }';
                        document.head.appendChild(style);
                    })();
                """.trimIndent(),
                enabled = false
            )
        }

        // Ad hide extension (CSS-based)
        if (extensions.none { it.id == "builtin_adblock_css" }) {
            installExtension(
                id = "builtin_adblock_css",
                name = "Ad Hider",
                description = "Hides common ad elements via CSS",
                version = "1.0",
                script = """
                    (function() {
                        var style = document.createElement('style');
                        style.innerHTML = '[class*="ad-"],[class*="-ad"],[id*="ad-"],[id*="-ad"],' +
                        '[class*="banner"],[class*="popup"],[class*="overlay"],[class*="sponsored"],' +
                        'iframe[src*="ad"],iframe[src*="doubleclick"] { display: none !important; }';
                        document.head.appendChild(style);
                    })();
                """.trimIndent(),
                enabled = true
            )
        }

        // Reader mode extension
        if (extensions.none { it.id == "builtin_reader" }) {
            installExtension(
                id = "builtin_reader",
                name = "Reader Mode",
                description = "Cleans up articles for easy reading",
                version = "1.0",
                script = """
                    (function() {
                        document.querySelectorAll('nav,header,footer,aside,[class*="sidebar"],[class*="menu"],[class*="cookie"],[class*="popup"],[class*="ad"]').forEach(function(el){ el.style.display='none'; });
                        var main = document.querySelector('article') || document.querySelector('main') || document.body;
                        document.body.style.cssText = 'background:#1a1a1a;color:#e8eaed;font-family:Georgia,serif;max-width:700px;margin:40px auto;padding:20px;font-size:18px;line-height:1.8;';
                        if(main !== document.body){ document.body.innerHTML = main.innerHTML; }
                    })();
                """.trimIndent(),
                enabled = false
            )
        }

        // No tracking extension
        if (extensions.none { it.id == "builtin_notrack" }) {
            installExtension(
                id = "builtin_notrack",
                name = "Anti Tracking",
                description = "Blocks common tracking scripts",
                version = "1.0",
                script = """
                    (function() {
                        var trackers = ['google-analytics.com','googletagmanager.com','facebook.net','connect.facebook.net','hotjar.com','mixpanel.com','segment.com','amplitude.com'];
                        var origCreate = document.createElement.bind(document);
                        document.createElement = function(tag) {
                            var el = origCreate(tag);
                            if(tag.toLowerCase() === 'script') {
                                var origSetAttr = el.setAttribute.bind(el);
                                el.setAttribute = function(name, value) {
                                    if(name === 'src' && trackers.some(function(t){ return value.includes(t); })) return;
                                    origSetAttr(name, value);
                                };
                            }
                            return el;
                        };
                    })();
                """.trimIndent(),
                enabled = true
            )
        }
    }

    fun installExtension(
        id: String,
        name: String,
        description: String,
        version: String,
        script: String,
        enabled: Boolean = true
    ) {
        val ext = Extension(id, name, description, version, script, enabled)
        val existing = extensions.indexOfFirst { it.id == id }
        if (existing >= 0) extensions[existing] = ext else extensions.add(ext)
        saveExtension(ext)
    }

    fun installFromScript(name: String, script: String) {
        val id = "user_${System.currentTimeMillis()}"
        val description = extractMetaField(script, "description") ?: "User script"
        val version = extractMetaField(script, "version") ?: "1.0"
        installExtension(id, name, description, version, script)
    }

    private fun extractMetaField(script: String, field: String): String? {
        val regex = Regex("@$field\\s+(.+)")
        return regex.find(script)?.groupValues?.get(1)?.trim()
    }

    fun toggleExtension(id: String) {
        extensions.find { it.id == id }?.let {
            it.enabled = !it.enabled
            saveExtension(it)
        }
    }

    fun removeExtension(id: String) {
        extensions.removeAll { it.id == id }
        File(extensionsDir, "$id.json").delete()
    }

    fun getEnabledExtensions(): List<Extension> = extensions.filter { it.enabled }

    fun getAllExtensions(): List<Extension> = extensions.toList()

    private fun saveExtension(ext: Extension) {
        val json = JSONObject().apply {
            put("id", ext.id)
            put("name", ext.name)
            put("description", ext.description)
            put("version", ext.version)
            put("script", ext.script)
            put("enabled", ext.enabled)
        }
        File(extensionsDir, "${ext.id}.json").writeText(json.toString())
    }

    private fun loadExtensions() {
        extensionsDir.listFiles()?.filter { it.extension == "json" }?.forEach { file ->
            try {
                val json = JSONObject(file.readText())
                extensions.add(
                    Extension(
                        id = json.getString("id"),
                        name = json.getString("name"),
                        description = json.optString("description", ""),
                        version = json.optString("version", "1.0"),
                        script = json.getString("script"),
                        enabled = json.optBoolean("enabled", true)
                    )
                )
            } catch (e: Exception) {
                file.delete()
            }
        }
    }
}
