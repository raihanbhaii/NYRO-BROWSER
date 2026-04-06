package com.nyro.browser.utils

import android.util.Log

object Logger {
    private const val TAG = "NYRO_Browser"
    
    fun d(message: String) {
        Log.d(TAG, message)
    }
    
    fun e(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(TAG, message, throwable)
        } else {
            Log.e(TAG, message)
        }
    }
    
    fun i(message: String) {
        Log.i(TAG, message)
    }
    
    fun w(message: String) {
        Log.w(TAG, message)
    }
    
    fun v(message: String) {
        Log.v(TAG, message)
    }
}
