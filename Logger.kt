package com.nyro.browser.utils

import android.util.Log

object Logger {
    private const val DEFAULT_TAG = "NYRO_Browser"
    
    fun d(message: String) {
        Log.d(DEFAULT_TAG, message)
    }
    
    fun d(tag: String, message: String) {
        Log.d(tag, message)
    }
    
    fun e(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(DEFAULT_TAG, message, throwable)
        } else {
            Log.e(DEFAULT_TAG, message)
        }
    }
    
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
    }
    
    fun i(message: String) {
        Log.i(DEFAULT_TAG, message)
    }
    
    fun i(tag: String, message: String) {
        Log.i(tag, message)
    }
    
    fun w(message: String) {
        Log.w(DEFAULT_TAG, message)
    }
    
    fun w(tag: String, message: String) {
        Log.w(tag, message)
    }
    
    fun v(message: String) {
        Log.v(DEFAULT_TAG, message)
    }
    
    fun v(tag: String, message: String) {
        Log.v(tag, message)
    }
}
