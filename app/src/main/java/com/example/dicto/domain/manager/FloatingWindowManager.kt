package com.example.dicto.domain.manager

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.dicto.ui.floating.FloatingWindowService
import com.example.dicto.ui.floating.coordinator.FloatingWindowCoordinator
import com.example.dicto.utils.PermissionHelper

/**
 * FloatingWindowManager - Manages floating window lifecycle
 *
 * Single Responsibility: Control when floating window is shown/hidden
 *
 * Strategy:
 * - startFloatingWindow(): Starts the service the first time. If already running,
 *   sends ACTION_SHOW to make the button visible instantly (no restart).
 * - stopFloatingWindow(): Stops the service entirely (called when entering main app).
 * - showFloatingButton(): Sends ACTION_SHOW to the running service (used when
 *   leaving overlay - service stays alive, button is just made visible).
 * - hideFloatingButton(): Sends ACTION_HIDE to the running service.
 */
class FloatingWindowManager(private val context: Context) : IFloatingWindowManager {

    /** Start service (first launch from background) */
    override fun startFloatingWindow() {
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

    /** Stop service entirely (called when entering main app) */
    override fun stopFloatingWindow() {
        try {
            android.util.Log.d("DICTO_FLOATING", ">>> [BEFORE_STOP] FloatingWindowManager.stopFloatingWindow() - STOPPING SERVICE")
            val intent = Intent(context, FloatingWindowService::class.java)
            context.stopService(intent)
            android.util.Log.d("DICTO_FLOATING", ">>> [AFTER_STOP] FloatingWindowManager.stopFloatingWindow() - Service stopped")
            Log.d("FloatingWindowManager", "Floating window service stopped")
        } catch (e: Exception) {
            android.util.Log.e("DICTO_FLOATING", ">>> FloatingWindowManager.stopFloatingWindow() ERROR: ${e.message}")
            Log.e("FloatingWindowManager", "Error stopping floating window: ${e.message}")
        }
    }

    /**
     * Show floating button instantly without restarting the service.
     * Use this when returning from the overlay - service is already running.
     */
    override fun showFloatingButton() {
        try {
            android.util.Log.d("DICTO_FLOATING", ">>> FloatingWindowManager.showFloatingButton() - sending ACTION_SHOW")
            val intent = Intent(context, FloatingWindowService::class.java).apply {
                action = FloatingWindowCoordinator.ACTION_SHOW
            }
            context.startService(intent)
        } catch (e: Exception) {
            android.util.Log.e("DICTO_FLOATING", ">>> FloatingWindowManager.showFloatingButton() ERROR: ${e.message}")
        }
    }

    /**
     * Hide floating button without stopping the service.
     * Use this when entering the overlay.
     */
    fun hideFloatingButton() {
        try {
            android.util.Log.d("DICTO_FLOATING", ">>> FloatingWindowManager.hideFloatingButton() - sending ACTION_HIDE")
            val intent = Intent(context, FloatingWindowService::class.java).apply {
                action = FloatingWindowCoordinator.ACTION_HIDE
            }
            context.startService(intent)
        } catch (e: Exception) {
            android.util.Log.e("DICTO_FLOATING", ">>> FloatingWindowManager.hideFloatingButton() ERROR: ${e.message}")
        }
    }

    /**
     * Check if overlay permission is granted
     */
    override fun isPermissionGranted(): Boolean {
        return PermissionHelper.canDrawOverlays(context)
    }
}
