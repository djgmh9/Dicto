package com.example.dicto.ui.floating

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.view.WindowManager
import com.example.dicto.utils.AppLogger

/**
 * FloatingWindowService - Coordinates floating window components
 *
 * Single Responsibility: Service lifecycle and component coordination
 * Delegates to:
 * - FloatingButtonManager: Button creation and touch handling
 * - TrashBinManager: Trash bin display and proximity detection
 * - NotificationHelper: Foreground notification management
 */
class FloatingWindowService : Service() {

    private var windowManager: WindowManager? = null
    private var buttonManager: FloatingButtonManager? = null
    private var trashBinManager: TrashBinManager? = null
    private var notificationHelper: NotificationHelper? = null
    private var restoreReceiver: BroadcastReceiver? = null


    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        AppLogger.logServiceState("FloatingWindowService", "CREATED")

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        notificationHelper = NotificationHelper(this)
        notificationHelper?.createNotificationChannel()

        registerRestoreReceiver()
    }

    private fun registerRestoreReceiver() {
        restoreReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "com.example.dicto.RESTORE_FLOATING_BUTTON") {
                    AppLogger.logServiceState("FloatingWindowService", "Restoring floating button")
                    buttonManager?.restore()
                }
            }
        }
        val filter = IntentFilter("com.example.dicto.RESTORE_FLOATING_BUTTON")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            registerReceiver(restoreReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(restoreReceiver, filter)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        AppLogger.logServiceState("FloatingWindowService", "STARTED", "Building notification")

        try {
            // Start foreground service
            val notification = notificationHelper?.createNotification()
            if (notification != null) {
                AppLogger.logServiceState("FloatingWindowService", "STARTING_FOREGROUND")
                startForeground(NotificationHelper.NOTIFICATION_ID, notification)
                AppLogger.logServiceState("FloatingWindowService", "FOREGROUND_ACTIVE", "Notification shown")
            }

            // Initialize managers
            if (windowManager != null) {
                initializeManagers()

                // Show floating button
                buttonManager?.show()
                AppLogger.logServiceState("FloatingWindowService", "WINDOW_CREATED", "Button visible")
            }
        } catch (e: Exception) {
            AppLogger.logServiceState("FloatingWindowService", "ERROR", e.message ?: "Unknown error")
            AppLogger.error("FloatingWindowService", "Error in onStartCommand", e)
            stopSelf()
        }

        AppLogger.logServiceState("FloatingWindowService", "STICKY_MODE", "Will restart if killed")
        return START_STICKY
    }

    private fun initializeManagers() {
        if (buttonManager == null && windowManager != null) {
            trashBinManager = TrashBinManager(this, windowManager!!)

            buttonManager = FloatingButtonManager(
                context = this,
                windowManager = windowManager!!,
                onButtonTapped = ::onButtonTapped,
                onDragStart = ::onDragStart,
                onDragMove = ::onDragMove,
                onDragEnd = ::onDragEnd
            )
        }
    }

    private fun onButtonTapped() {
        AppLogger.logUserAction("Floating Button Tapped", "Opening translator overlay - hiding button")
        try {
            buttonManager?.hide()
            trashBinManager?.hide()

            val intent = Intent(this, FloatingTranslatorActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        } catch (e: Exception) {
            AppLogger.error("FloatingWindow", "Error opening app", e)
            buttonManager?.restore()
        }
    }

    private fun onDragStart() {
        trashBinManager?.show()
    }

    private fun onDragMove(x: Float, y: Float) {
        trashBinManager?.updateState(x, y)
    }

    private fun onDragEnd(x: Float, y: Float, wasDragging: Boolean) {
        if (wasDragging && trashBinManager?.isNear(x, y) == true) {
            AppLogger.logUserAction("Floating Button", "Dropped on trash - closing")
            stopSelf()
        }
        trashBinManager?.hide()
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

            // Destroy managers
            trashBinManager?.destroy()
            buttonManager?.destroy()
        } catch (e: Exception) {
            AppLogger.error("FloatingWindowService", "Error removing views", e)
        }

        stopForeground(true)
        AppLogger.logServiceState("FloatingWindowService", "DESTROYED", "Foreground stopped")
    }
}
