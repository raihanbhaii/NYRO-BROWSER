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
    private val secureLabel: TextView
    val progressBar: ProgressBar
    private var navigateListener: ((String) -> Unit)? = null

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(12, 10, 12, 10)
        setBackgroundColor(Color.parseColor("#202124"))

        val container = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#303134"))
                cornerRadius = 100f
            }
            layoutParams = LayoutParams(0, 120, 1f)
            setPadding(28, 0, 28, 0)
        }

        secureLabel = TextView(context).apply {
            text = "https"
            textSize = 11f
            setTextColor(Color.parseColor("#81c995"))
            typeface = Typeface.DEFAULT
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).also {
                it.marginEnd = 10
            }
        }

        urlEditText = EditText(context).apply {
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_URI or
                    android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
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
                        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                            .hideSoftInputFromWindow(windowToken, 0)
                    }
                    true
                } else false
            }

            setOnFocusChangeListener { _, hasFocus ->
                container.background = GradientDrawable().apply {
                    setColor(if (hasFocus) Color.parseColor("#404144") else Color.parseColor("#303134"))
                    cornerRadius = 100f
                }
                if (hasFocus) selectAll()
            }
        }

        container.addView(secureLabel)
        container.addView(urlEditText)
        addView(container)

        progressBar = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 4)
            max = 100
            progress = 0
            visibility = GONE
        }
    }

    fun getProgressBar(): ProgressBar = progressBar

    fun setUrl(url: String) {
        if (!urlEditText.isFocused) {
            urlEditText.setText(url)
            urlEditText.setSelection(0)
        }
        val secure = url.startsWith("https://")
        secureLabel.text = if (secure) "https" else if (url.startsWith("http://")) "http" else ""
        secureLabel.setTextColor(
            if (secure) Color.parseColor("#81c995") else Color.parseColor("#f28b82")
        )
    }

    fun setLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) VISIBLE else GONE
    }

    fun setOnNavigateListener(listener: (String) -> Unit) {
        navigateListener = listener
    }
}
