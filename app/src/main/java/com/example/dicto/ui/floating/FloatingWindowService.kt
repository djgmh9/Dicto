package com.example.dicto.ui.floating

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import com.example.dicto.utils.AppLogger

/**
 * FloatingWindowService - Displays a draggable floating translation button
 *
 * Single Responsibility: Manage floating window lifecycle and interactions
 * Features:
 * - Always-on-top floating button
 * - Draggable (user can move around screen)
 * - Foreground service (stays visible on Android 10+)
 * - Opens translator overlay on tap
 */
class FloatingWindowService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: ImageView? = null
    private var trashView: ImageView? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var trashParams: WindowManager.LayoutParams? = null
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false
    private var restoreReceiver: BroadcastReceiver? = null

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "floating_translator_channel"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        AppLogger.logServiceState("FloatingWindowService", "CREATED")
        createNotificationChannel()
        registerRestoreReceiver()
    }

    private fun registerRestoreReceiver() {
        restoreReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "com.example.dicto.RESTORE_FLOATING_BUTTON") {
                    AppLogger.logServiceState("FloatingWindowService", "Restoring floating button")
                    restoreFloatingButton()
                }
            }
        }
        val filter = IntentFilter("com.example.dicto.RESTORE_FLOATING_BUTTON")

        // Register with RECEIVER_EXPORTED flag for Android 12+ compatibility
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            registerReceiver(restoreReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(restoreReceiver, filter)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        AppLogger.logServiceState("FloatingWindowService", "STARTED", "Building notification")

        try {
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

            // Create notification FIRST before creating window
            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Dicto Translator")
                .setContentText("Floating translator is active")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build()

            // Start foreground BEFORE any other operations
            AppLogger.logServiceState("FloatingWindowService", "STARTING_FOREGROUND")
            startForeground(NOTIFICATION_ID, notification)
            AppLogger.logServiceState("FloatingWindowService", "FOREGROUND_ACTIVE", "Notification shown")

            // Now create the floating window
            AppLogger.logServiceState("FloatingWindowService", "CREATING_WINDOW")
            createFloatingWindow()
            AppLogger.logServiceState("FloatingWindowService", "WINDOW_CREATED", "Button visible")
        } catch (e: Exception) {
            AppLogger.logServiceState("FloatingWindowService", "ERROR", e.message ?: "Unknown error")
            AppLogger.error("FloatingWindowService", "Error in onStartCommand", e)
            stopSelf()
        }

        // Use START_STICKY to restart service if killed by system
        AppLogger.logServiceState("FloatingWindowService", "STICKY_MODE", "Will restart if killed")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        AppLogger.logServiceState("FloatingWindowService", "DESTROYING")
        try {
            // Unregister broadcast receiver
            if (restoreReceiver != null) {
                unregisterReceiver(restoreReceiver)
                AppLogger.debug("FloatingWindow", "Broadcast receiver unregistered")
            }

            // Remove trash bin first
            hideTrashBin()

            // Remove floating view
            if (floatingView != null && floatingView?.windowToken != null) {
                windowManager?.removeView(floatingView)
                AppLogger.logServiceState("FloatingWindowService", "VIEW_REMOVED")
            }
        } catch (e: Exception) {
            AppLogger.error("FloatingWindowService", "Error removing view", e)
        }
        stopForeground(true)
        AppLogger.logServiceState("FloatingWindowService", "DESTROYED", "Foreground stopped")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Floating Translator",
                NotificationManager.IMPORTANCE_LOW
            )
            channel.description = "Notification for floating translator service"
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun createFloatingWindow() {
        try {
            AppLogger.logServiceState("FloatingWindowService", "Creating floating window with drag support")

            // Create layout parameters FIRST (no gravity - allows free positioning)
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                // NO gravity - allows free positioning anywhere on screen
                x = 0
                y = 100
                width = 150
                height = 150
            }

            layoutParams = params

            // Create floating button
            floatingView = ImageView(this).apply {
                setBackgroundColor(Color.parseColor("#6200EE"))
                setImageResource(android.R.drawable.ic_menu_search)
                scaleType = ImageView.ScaleType.CENTER

                // Handle both drag and click
                setOnTouchListener { _, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            initialX = params.x
                            initialY = params.y
                            initialTouchX = event.rawX
                            initialTouchY = event.rawY
                            isDragging = false
                            showTrashBin()
                            AppLogger.debug("FloatingWindow", "Touch DOWN at (${event.rawX}, ${event.rawY})")
                            true
                        }

                        MotionEvent.ACTION_MOVE -> {
                            val deltaX = event.rawX - initialTouchX
                            val deltaY = event.rawY - initialTouchY

                            if (Math.abs(deltaX) > 10 || Math.abs(deltaY) > 10) {
                                isDragging = true
                                params.x = (initialX + deltaX).toInt()
                                params.y = (initialY + deltaY).toInt()
                                windowManager?.updateViewLayout(floatingView, params)
                                updateTrashBinState(event.rawX, event.rawY)
                                AppLogger.debug("FloatingWindow", "Dragging to (${params.x}, ${params.y})")
                            }
                            true
                        }

                        MotionEvent.ACTION_UP -> {
                            if (!isDragging) {
                                AppLogger.logUserAction("Floating Button Tapped", "Opening translator overlay - hiding button")
                                try {
                                    // Hide floating button before opening translator
                                    windowManager?.removeView(floatingView)
                                    hideTrashBin()

                                    val intent = Intent(this@FloatingWindowService, FloatingTranslatorActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                    startActivity(intent)
                                } catch (e: Exception) {
                                    AppLogger.error("FloatingWindow", "Error opening app", e)
                                    // Restore button if error
                                    windowManager?.addView(floatingView, params)
                                }
                            } else {
                                AppLogger.debug("FloatingWindow", "Checking if near trash at (${event.rawX}, ${event.rawY})")
                                if (isNearTrash(event.rawX, event.rawY)) {
                                    AppLogger.logUserAction("Floating Button", "Dropped on trash - closing")
                                    closeFloatingWindow()
                                } else {
                                    AppLogger.debug("FloatingWindow", "Drag completed at (${params.x}, ${params.y})")
                                }
                            }
                            hideTrashBin()
                            isDragging = false
                            true
                        }

                        else -> false
                    }
                }
            }

            windowManager?.addView(floatingView, params)
            AppLogger.logServiceState("FloatingWindowService", "WINDOW_ADDED", "Button visible - full drag support enabled")
        } catch (e: Exception) {
            AppLogger.error("FloatingWindowService", "Error creating floating window", e)
            stopSelf()
        }
    }

    private fun showTrashBin() {
        try {
            if (trashView == null) {
                trashView = ImageView(this).apply {
                    setBackgroundColor(Color.parseColor("#FF0000"))
                    setImageResource(android.R.drawable.ic_menu_delete)
                    scaleType = ImageView.ScaleType.CENTER
                    alpha = 0.7f
                }

                val screenWidth = windowManager?.defaultDisplay?.width ?: 1080
                val trashSize = 200
                val trashCenterX = (screenWidth - trashSize) / 2  // Properly centered

                trashParams = WindowManager.LayoutParams(
                    trashSize,
                    trashSize,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    else
                        WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    x = trashCenterX  // Centered horizontally
                    y = 1500          // Bottom of screen
                    width = trashSize
                    height = trashSize
                }

                windowManager?.addView(trashView, trashParams)
                AppLogger.debug("FloatingWindow", "Trash bin shown at x=$trashCenterX")
            }
        } catch (e: Exception) {
            AppLogger.error("FloatingWindow", "Error showing trash bin", e)
        }
    }

    private fun hideTrashBin() {
        try {
            if (trashView != null && trashView?.windowToken != null) {
                windowManager?.removeView(trashView)
                trashView = null
                AppLogger.debug("FloatingWindow", "Trash bin hidden")
            }
        } catch (e: Exception) {
            AppLogger.error("FloatingWindow", "Error hiding trash bin", e)
        }
    }

    private fun updateTrashBinState(x: Float, y: Float) {
        if (isNearTrash(x, y)) {
            trashView?.alpha = 1.0f
            trashView?.setBackgroundColor(Color.parseColor("#FF3333"))
        } else {
            trashView?.alpha = 0.7f
            trashView?.setBackgroundColor(Color.parseColor("#FF0000"))
        }
    }

    private fun isNearTrash(x: Float, y: Float): Boolean {
        val screenWidth = windowManager?.defaultDisplay?.width ?: 1080
        val trashCenterX = (screenWidth / 2).toFloat()  // Properly centered
        val trashCenterY = 1600f

        val distance = Math.sqrt(
            Math.pow((x - trashCenterX).toDouble(), 2.0) +
            Math.pow((y - trashCenterY).toDouble(), 2.0)
        )

        val isNear = distance < 200  // Increased threshold from 150 to 200
        AppLogger.debug("FloatingWindow", "Distance to trash: ${distance.toInt()}, isNear: $isNear")
        return isNear
    }

    private fun closeFloatingWindow() {
        try {
            AppLogger.logServiceState("FloatingWindowService", "CLOSING", "User closed via trash")
            stopSelf()
        } catch (e: Exception) {
            AppLogger.error("FloatingWindow", "Error closing floating window", e)
        }
    }

    private fun restoreFloatingButton() {
        try {
            if (floatingView != null && layoutParams != null) {
                // Check if button is still in window
                if (floatingView?.windowToken == null) {
                    // Button was removed, add it back
                    windowManager?.addView(floatingView, layoutParams)
                    AppLogger.logServiceState("FloatingWindowService", "RESTORED", "Button restored to screen")
                } else {
                    AppLogger.debug("FloatingWindow", "Button already visible, no restore needed")
                }
            }
        } catch (e: Exception) {
            AppLogger.error("FloatingWindow", "Error restoring button", e)
        }
    }
}


