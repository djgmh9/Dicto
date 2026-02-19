package com.example.dicto.utils.logging

import android.util.Log
import com.example.dicto.BuildConfig

/**
 * LoggerImpl - Default logger implementation
 *
 * Automatically disables debug logs in release builds
 */
class LoggerImpl(
    private val isDebugMode: Boolean = BuildConfig.DEBUG
) : Logger {

    override fun debug(tag: String, message: String) {
        if (isDebugMode) {
            Log.d(tag, message)
        }
    }

    override fun info(tag: String, message: String) {
        Log.i(tag, message)
    }

    override fun warn(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) {
            Log.w(tag, message, throwable)
        } else {
            Log.w(tag, message)
        }
    }

    override fun error(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
    }
}

