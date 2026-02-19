package com.example.dicto.ui.floating

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.dicto.ui.floating.coordinator.FloatingWindowCoordinator
import com.example.dicto.utils.AppLogger
import com.example.dicto.utils.logging.FloatingWindowLogger

/**
 * FloatingWindowService - Manages the floating translator button
 *
 * Single Responsibility: Service lifecycle management
 * Delegates all logic to FloatingWindowCoordinator
 *
 * This service:
 * - Runs as a foreground service (visible to user)
 * - Shows a floating button for quick translation access
 * - Allows dragging and position persistence
 * - Respects trash bin for deletion
 *
 * All component management delegated to FloatingWindowCoordinator
 */
class FloatingWindowService : Service() {

    private var coordinator: FloatingWindowCoordinator? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        FloatingWindowLogger.serviceCreated()
        AppLogger.logServiceState("FloatingWindowService", "CREATED")

        // Create and initialize coordinator
        coordinator = FloatingWindowCoordinator(this)
        coordinator?.initialize()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        FloatingWindowLogger.onStartCommand()
        return coordinator?.start() ?: START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        FloatingWindowLogger.positionSaved(0, 0) // Log cleanup
        AppLogger.logServiceState("FloatingWindowService", "DESTROYING")

        coordinator?.cleanup()
        coordinator = null

        AppLogger.logServiceState("FloatingWindowService", "DESTROYED")
    }
}
