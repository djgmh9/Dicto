package com.example.dicto.domain

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.dicto.ui.floating.FloatingWindowService

/**
 * FloatingWindowManager - Manages floating window lifecycle
 *
 * Single Responsibility: Control when floating window is shown/hidden
 * Features:
 * - Start/stop floating window service
 * - Check if service is running
 * - Lifecycle management
 *
 * Usage:
 * val manager = FloatingWindowManager(context)
 * manager.startFloatingWindow()  // Show floating button
 * manager.stopFloatingWindow()   // Hide floating button
 */
class FloatingWindowManager(private val context: Context) {

    /**
     * Start the floating window service
     */
    fun startFloatingWindow() {
        try {
            val intent = Intent(context, FloatingWindowService::class.java)
            context.startService(intent)
            Log.d("FloatingWindowManager", "Floating window service started")
        } catch (e: Exception) {
            Log.e("FloatingWindowManager", "Error starting floating window: ${e.message}")
        }
    }

    /**
     * Stop the floating window service
     */
    fun stopFloatingWindow() {
        try {
            val intent = Intent(context, FloatingWindowService::class.java)
            context.stopService(intent)
            Log.d("FloatingWindowManager", "Floating window service stopped")
        } catch (e: Exception) {
            Log.e("FloatingWindowManager", "Error stopping floating window: ${e.message}")
        }
    }

    /**
     * Toggle floating window on/off
     */
    fun toggleFloatingWindow(shouldShow: Boolean) {
        if (shouldShow) {
            startFloatingWindow()
        } else {
            stopFloatingWindow()
        }
    }
}

