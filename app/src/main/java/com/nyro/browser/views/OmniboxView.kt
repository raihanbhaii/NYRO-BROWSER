package com.nyro.browser.views

import android.content.Context
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar

class OmniboxView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    
    private val urlEditText: EditText
    private val progressBar: ProgressBar
    private var navigateListener: ((String) -> Unit)? = null
    
    init {
        orientation = HORIZONTAL
        
        urlEditText = EditText(context).apply {
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
            inputType = EditorInfo.TYPE_TEXT_VARIATION_URI
            imeOptions = EditorInfo.IME_ACTION_GO
            setSingleLine(true)
            
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    val url = text.toString()
                    if (url.isNotBlank()) {
                        navigateListener?.invoke(url)
                    }
                    true
                } else false
            }
        }
        
        progressBar = ProgressBar(context).apply {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            isIndeterminate = true
            visibility = GONE
        }
        
        addView(urlEditText)
        addView(progressBar)
    }
    
    fun setUrl(url: String) {
        urlEditText.setText(url)
    }
    
    fun setLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) VISIBLE else GONE
    }
    
    fun setOnNavigateListener(listener: (String) -> Unit) {
        navigateListener = listener
    }
}
