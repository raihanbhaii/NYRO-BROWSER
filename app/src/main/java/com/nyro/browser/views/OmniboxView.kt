package com.nyro.browser.views

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*

class OmniboxView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val urlEditText: EditText
    private val secureIcon: TextView
    private val loadingBar: ProgressBar
    private var navigateListener: ((String) -> Unit)? = null

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(12, 8, 12, 8)
        setBackgroundColor(Color.parseColor("#202124"))

        val container = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#303134"))
                cornerRadius = 100f
            }
            layoutParams = LayoutParams(0, 112, 1f)
            setPadding(24, 0, 24, 0)
        }

        secureIcon = TextView(context).apply {
            text = "https"
            textSize = 11f
            setTextColor(Color.parseColor("#9aa0a6"))
            typeface = Typeface.DEFAULT
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            ).also { it.marginEnd = 8 }
        }

        urlEditText = EditText(context).apply {
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_URI
            imeOptions = EditorInfo.IME_ACTION_GO
            setSingleLine(true)
            background = null
            setTextColor(Color.parseColor("#e8eaed"))
            setHintTextColor(Color.parseColor("#9aa0a6"))
            hint = "Search or type URL"
            textSize = 14f
            typeface = Typeface.DEFAULT

            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    val input = text.toString().trim()
                    if (input.isNotBlank()) {
                        navigateListener?.invoke(input)
                        clearFocus()
                        hideKeyboard()
                    }
                    true
                } else false
            }

            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    selectAll()
                    container.background = GradientDrawable().apply {
                        setColor(Color.parseColor("#404144"))
                        cornerRadius = 100f
                    }
                } else {
                    container.background = GradientDrawable().apply {
                        setColor(Color.parseColor("#303134"))
                        cornerRadius = 100f
                    }
                }
            }
        }

        container.addView(secureIcon)
        container.addView(urlEditText)
        addView(container)

        loadingBar = ProgressBar(
            context, null,
            android.R.attr.progressBarStyleHorizontal
        ).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 6)
            max = 100
            progress = 0
            visibility = GONE
        }
    }

    fun getProgressBar(): ProgressBar = loadingBar

    fun setUrl(url: String) {
        if (!urlEditText.isFocused) {
            urlEditText.setText(url)
            urlEditText.setSelection(0)
        }
        val isSecure = url.startsWith("https://")
        secureIcon.text = if (isSecure) "https" else "http"
        secureIcon.setTextColor(
            if (isSecure) Color.parseColor("#81c995")
            else Color.parseColor("#f28b82")
        )
    }

    fun setLoading(isLoading: Boolean) {
        loadingBar.visibility = if (isLoading) VISIBLE else GONE
    }

    fun setOnNavigateListener(listener: (String) -> Unit) {
        navigateListener = listener
    }

    private fun hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}
