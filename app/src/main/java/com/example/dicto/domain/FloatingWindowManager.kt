package com.example.dicto.domain

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.dicto.ui.floating.FloatingWindowService
import com.example.dicto.utils.PermissionHelper

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
            android.util.Log.d("DICTO_FLOATING", ">>> FloatingWindowManager.startFloatingWindow() called")
            val intent = Intent(context, FloatingWindowService::class.java)
            context.startService(intent)
            android.util.Log.d("DICTO_FLOATING", ">>> FloatingWindowManager.startFloatingWindow() - Intent sent to start service")
            Log.d("FloatingWindowManager", "Floating window service started")
        } catch (e: Exception) {
            android.util.Log.e("DICTO_FLOATING", ">>> FloatingWindowManager.startFloatingWindow() ERROR: ${e.message}")
            Log.e("FloatingWindowManager", "Error starting floating window: ${e.message}")
        }
    }

    /**
     * Stop the floating window service
     */
    fun stopFloatingWindow() {
        try {
            android.util.Log.d("DICTO_FLOATING", ">>> FloatingWindowManager.stopFloatingWindow() called")
            val intent = Intent(context, FloatingWindowService::class.java)
            context.stopService(intent)
            android.util.Log.d("DICTO_FLOATING", ">>> FloatingWindowManager.stopFloatingWindow() - Service stopped")
            Log.d("FloatingWindowManager", "Floating window service stopped")
        } catch (e: Exception) {
            android.util.Log.e("DICTO_FLOATING", ">>> FloatingWindowManager.stopFloatingWindow() ERROR: ${e.message}")
            Log.e("FloatingWindowManager", "Error stopping floating window: ${e.message}")
        }
    }

    /**
     * Toggle floating window on/off
     */
    fun toggleFloatingWindow(shouldShow: Boolean) {
        android.util.Log.d("DICTO_FLOATING", ">>> FloatingWindowManager.toggleFloatingWindow(shouldShow=$shouldShow)")
        if (shouldShow) {
            startFloatingWindow()
        } else {
            stopFloatingWindow()
        }
    }

    /**
     * Check if overlay permission is granted
     */
    fun isPermissionGranted(): Boolean {
        return PermissionHelper.canDrawOverlays(context)
    }
}

