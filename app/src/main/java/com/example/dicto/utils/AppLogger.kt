package com.example.dicto.utils

import android.content.Context
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

/**
 * AppLogger - Centralized logging system for debugging
 *
 * Single Responsibility: Handle all app logging with timestamps
 * Features:
 * - Timestamped logs
 * - Different log levels (DEBUG, INFO, WARN, ERROR)
 * - Easy filtering in logcat
 * - Persistent log file (optional)
 */
object AppLogger {
    private const val TAG = "DictoApp"
    private const val DEBUG = true // Set to false to disable debug logs in production

    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    /**
     * Log debug message with timestamp
     */
    fun debug(tag: String, message: String) {
        if (DEBUG) {
            val timestamp = dateFormat.format(Date())
            val fullMessage = "[$timestamp] $message"
            Log.d("$TAG:$tag", fullMessage)
        }
    }

    /**
     * Log info message with timestamp
     */
    fun info(tag: String, message: String) {
        val timestamp = dateFormat.format(Date())
        val fullMessage = "[$timestamp] $message"
        Log.i("$TAG:$tag", fullMessage)
    }

    /**
     * Log warning message with timestamp
     */
    fun warn(tag: String, message: String) {
        val timestamp = dateFormat.format(Date())
        val fullMessage = "[$timestamp] $message"
        Log.w("$TAG:$tag", fullMessage)
    }

    /**
     * Log error message with timestamp and exception
     */
    fun error(tag: String, message: String, exception: Exception? = null) {
        val timestamp = dateFormat.format(Date())
        val fullMessage = "[$timestamp] $message"
        if (exception != null) {
            Log.e("$TAG:$tag", fullMessage, exception)
        } else {
            Log.e("$TAG:$tag", fullMessage)
        }
    }

    /**
     * Log app lifecycle events
     */
    fun logAppEvent(event: String, details: String = "") {
        val timestamp = dateFormat.format(Date())
        val message = if (details.isNotEmpty()) {
            "[$timestamp] APP_EVENT: $event - $details"
        } else {
            "[$timestamp] APP_EVENT: $event"
        }
        Log.i("$TAG:AppLifecycle", message)
    }

    /**
     * Log service state
     */
    fun logServiceState(serviceName: String, state: String, details: String = "") {
        val timestamp = dateFormat.format(Date())
        val message = if (details.isNotEmpty()) {
            "[$timestamp] SERVICE_STATE: $serviceName = $state ($details)"
        } else {
            "[$timestamp] SERVICE_STATE: $serviceName = $state"
        }
        Log.i("$TAG:ServiceState", message)
    }

    /**
     * Log user action
     */
    fun logUserAction(action: String, details: String = "") {
        val timestamp = dateFormat.format(Date())
        val message = if (details.isNotEmpty()) {
            "[$timestamp] USER_ACTION: $action - $details"
        } else {
            "[$timestamp] USER_ACTION: $action"
        }
        Log.i("$TAG:UserAction", message)
    }
}

