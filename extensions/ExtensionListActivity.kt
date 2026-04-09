package com.nyro.browser.extensions

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ExtensionListActivity : AppCompatActivity() {

    @Inject lateinit var extensionManager: ExtensionManager

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ExtensionAdapter
    private val PICK_JS_FILE = 1001

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
        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(Color.parseColor("#202124"))
            setPadding(16, 48, 16, 16)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            addView(TextView(this@ExtensionListActivity).apply {
                text = "<"
                textSize = 18f
                setTextColor(Color.parseColor("#e8eaed"))
                setPadding(8, 8, 24, 8)
                setOnClickListener { finish() }
            })

            addView(TextView(this@ExtensionListActivity).apply {
                text = "Extensions"
                textSize = 18f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#e8eaed"))
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            })

            addView(TextView(this@ExtensionListActivity).apply {
                text = "+ Add"
                textSize = 14f
                setTextColor(Color.parseColor("#4f8ef7"))
                setPadding(16, 8, 8, 8)
                setOnClickListener { showAddOptions() }
            })
        }

        // Subtitle
        val subtitle = TextView(this).apply {
            text = "Manage your browser extensions and userscripts"
            textSize = 12f
            setTextColor(Color.parseColor("#9aa0a6"))
            setPadding(24, 0, 24, 16)
        }

        recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@ExtensionListActivity)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
            )
        }

        adapter = ExtensionAdapter(
            extensions = extensionManager.getAllExtensions().toMutableList(),
            onToggle = { ext ->
                extensionManager.toggleExtension(ext.id)
            },
            onDelete = { ext ->
                AlertDialog.Builder(this)
                    .setTitle("Remove Extension")
                    .setMessage("Remove ${ext.name}?")
                    .setPositiveButton("Remove") { _, _ ->
                        extensionManager.removeExtension(ext.id)
                        refreshList()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        recyclerView.adapter = adapter

        root.addView(toolbar)
        root.addView(subtitle)
        root.addView(recyclerView)
        setContentView(root)
    }

    private fun showAddOptions() {
        val options = arrayOf("Import .js file", "Paste script manually")
        AlertDialog.Builder(this)
            .setTitle("Add Extension")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openFilePicker()
                    1 -> showPasteDialog()
                }
            }
            .show()
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, PICK_JS_FILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_JS_FILE && resultCode == Activity.RESULT_OK) {
            val uri = data?.data ?: return
            try {
                val script = contentResolver.openInputStream(uri)?.bufferedReader()?.readText() ?: return
                val name = uri.lastPathSegment?.removeSuffix(".js") ?: "Custom Script"
                extensionManager.installFromScript(name, script)
                refreshList()
                Toast.makeText(this, "Extension installed: $name", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to read file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showPasteDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 24)
        }

        val nameInput = EditText(this).apply {
            hint = "Extension name"
            setTextColor(Color.parseColor("#e8eaed"))
            setHintTextColor(Color.parseColor("#9aa0a6"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = 16 }
        }

        val scriptInput = EditText(this).apply {
            hint = "Paste JavaScript code here..."
            setTextColor(Color.parseColor("#e8eaed"))
            setHintTextColor(Color.parseColor("#9aa0a6"))
            minLines = 6
            gravity = Gravity.TOP
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        layout.addView(nameInput)
        layout.addView(scriptInput)

        AlertDialog.Builder(this)
            .setTitle("Paste Script")
            .setView(layout)
            .setPositiveButton("Install") { _, _ ->
                val name = nameInput.text.toString().ifBlank { "Custom Script" }
                val script = scriptInput.text.toString()
                if (script.isNotBlank()) {
                    extensionManager.installFromScript(name, script)
                    refreshList()
                    Toast.makeText(this, "Extension installed", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun refreshList() {
        adapter.updateList(extensionManager.getAllExtensions())
    }
}

class ExtensionAdapter(
    private var extensions: MutableList<ExtensionManager.Extension>,
    private val onToggle: (ExtensionManager.Extension) -> Unit,
    private val onDelete: (ExtensionManager.Extension) -> Unit
) : RecyclerView.Adapter<ExtensionAdapter.ViewHolder>() {

    inner class ViewHolder(val root: LinearLayout) : RecyclerView.ViewHolder(root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val row = LinearLayout(parent.context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(24, 20, 24, 20)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        return ViewHolder(row)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ext = extensions[position]
        holder.root.removeAllViews()

        // Info column
        val info = LinearLayout(holder.root.context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        info.addView(TextView(holder.root.context).apply {
            text = ext.name
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#e8eaed"))
        })

        info.addView(TextView(holder.root.context).apply {
            text = ext.description.ifBlank { "v${ext.version}" }
            textSize = 12f
            setTextColor(Color.parseColor("#9aa0a6"))
        })

        // Toggle switch
        val toggle = Switch(holder.root.context).apply {
            isChecked = ext.enabled
            setOnCheckedChangeListener(null)
            setOnCheckedChangeListener { _, _ ->
                onToggle(ext)
            }
        }

        // Delete button
        val deleteBtn = TextView(holder.root.context).apply {
            text = "X"
            textSize = 13f
            setTextColor(Color.parseColor("#f28b82"))
            setPadding(24, 8, 8, 8)
            setOnClickListener { onDelete(ext) }
        }

        // Divider
        val divider = View(holder.root.context).apply {
            setBackgroundColor(Color.parseColor("#303134"))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 1
            )
        }

        holder.root.addView(info)
        holder.root.addView(toggle)
        holder.root.addView(deleteBtn)
    }

    override fun getItemCount() = extensions.size

    fun updateList(newList: List<ExtensionManager.Extension>) {
        extensions = newList.toMutableList()
        notifyDataSetChanged()
    }
}
