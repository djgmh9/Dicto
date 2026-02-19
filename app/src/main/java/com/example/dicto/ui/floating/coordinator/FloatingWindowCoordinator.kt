package com.example.dicto.ui.floating.coordinator

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.view.WindowManager
import com.example.dicto.ui.floating.FloatingButtonManager
import com.example.dicto.ui.floating.FloatingTranslatorActivity
import com.example.dicto.ui.floating.TrashBinManager
import com.example.dicto.utils.notification.NotificationHelper
import com.example.dicto.data.local.PositionPersistence
import com.example.dicto.data.local.PreferencesManager
import com.example.dicto.utils.AppLogger
import com.example.dicto.utils.logging.FloatingWindowLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

/**
 * FloatingWindowCoordinator - Manages the floating window service lifecycle
 *
 * Single Responsibility: Coordinate all floating window components
 * Separates concerns:
 * - Service lifecycle (onCreate, onStartCommand, onDestroy)
 * - Component initialization
 * - User interactions (button tap, drag)
 * - Broadcasting and notifications
 *
 * Delegates to:
 * - FloatingButtonManager: Button UI and touch handling
 * - TrashBinManager: Trash bin display and proximity detection
 * - NotificationHelper: Foreground notification management
 * - PositionPersistence: Position loading/saving
 */
class FloatingWindowCoordinator(private val service: Service) {

    private var windowManager: WindowManager? = null
    private var buttonManager: FloatingButtonManager? = null
    private var trashBinManager: TrashBinManager? = null
    private var notificationHelper: NotificationHelper? = null
    private var restoreReceiver: BroadcastReceiver? = null
    private var preferencesManager: PreferencesManager? = null
    private var positionPersistence: PositionPersistence? = null

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    fun initialize() {
        FloatingWindowLogger.serviceCreated()
        AppLogger.logServiceState("FloatingWindowCoordinator", "INITIALIZING")

        windowManager = service.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        preferencesManager = PreferencesManager(service)
        positionPersistence = PositionPersistence(service, preferencesManager!!, serviceScope)
        notificationHelper = NotificationHelper(service)
        notificationHelper?.createNotificationChannel()

        registerRestoreReceiver()
    }

    fun start(): Int {
        FloatingWindowLogger.onStartCommand()
        AppLogger.logServiceState("FloatingWindowCoordinator", "STARTING", "Building notification")

        return try {
            // Start foreground service
            val notification = notificationHelper?.createNotification()
            if (notification != null) {
                FloatingWindowLogger.startingForeground()
                AppLogger.logServiceState("FloatingWindowCoordinator", "STARTING_FOREGROUND")
                service.startForeground(1, notification)
                FloatingWindowLogger.foregroundStarted()
                AppLogger.logServiceState("FloatingWindowCoordinator", "FOREGROUND_ACTIVE", "Notification shown")
            }

            // Initialize managers and show button with saved position
            loadAndShowButton()

            AppLogger.logServiceState("FloatingWindowCoordinator", "STICKY_MODE", "Will restart if killed")
            Service.START_STICKY
        } catch (e: Exception) {
            FloatingWindowLogger.error("Error in start()", e)
            AppLogger.logServiceState("FloatingWindowCoordinator", "ERROR", e.message ?: "Unknown error")
            AppLogger.error("FloatingWindowCoordinator", "Error in start", e)
            service.stopSelf()
            Service.START_STICKY
        }
    }

    fun cleanup() {
        FloatingWindowLogger.positionSaved(0, 0) // Log cleanup
        AppLogger.logServiceState("FloatingWindowCoordinator", "CLEANING_UP")
        android.util.Log.d("DICTO_FLOATING", ">>> [CLEANUP_START] Cleaning up floating window service")

        try {
            // Save current button position before destroying it (SYNCHRONOUSLY!)
            android.util.Log.d("DICTO_FLOATING", ">>> [CLEANUP] Getting current button position...")
            val position = buttonManager?.getCurrentPosition()
            if (position != null) {
                val (x, y) = position
                android.util.Log.d("DICTO_FLOATING", ">>> [CLEANUP] Current button position BEFORE cleanup: x=$x, y=$y")
                // Use runBlocking to ensure position is saved BEFORE service is destroyed
                try {
                    kotlinx.coroutines.runBlocking {
                        positionPersistence?.savePositionSync(x, y)
                    }
                    android.util.Log.d("DICTO_FLOATING", ">>> [CLEANUP] Position saved and committed: x=$x, y=$y")
                    FloatingWindowLogger.positionSaved(x, y)
                } catch (e: Exception) {
                    android.util.Log.e("DICTO_FLOATING", ">>> [CLEANUP] Error saving position: ${e.message}", e)
                }
            } else {
                android.util.Log.d("DICTO_FLOATING", ">>> [CLEANUP] No position to save (buttonManager is null or no position)")
            }

            // Unregister broadcast receiver
            if (restoreReceiver != null) {
                unregisterRestoreReceiver()
                AppLogger.debug("FloatingWindow", "Broadcast receiver unregistered")
            }

            // Destroy managers
            trashBinManager?.destroy()
            buttonManager?.destroy()
            android.util.Log.d("DICTO_FLOATING", ">>> [CLEANUP_END] Service cleanup completed")
            AppLogger.logServiceState("FloatingWindowCoordinator", "COMPONENTS_DESTROYED")
        } catch (e: Exception) {
            FloatingWindowLogger.error("Error in cleanup()", e)
            AppLogger.error("FloatingWindowCoordinator", "Error during cleanup", e)
        }

        // Stop foreground
        if (Build.VERSION.SDK_INT >= 33) {
            @Suppress("NewApi")
            service.stopForeground(Service.STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            service.stopForeground(true)
        }

        AppLogger.logServiceState("FloatingWindowCoordinator", "DESTROYED", "Foreground stopped")
    }

    /**
     * Load saved position and display button
     * Uses timeout mechanism to prevent indefinite waiting
     */
    private fun loadAndShowButton() {
        if (windowManager == null) return

        android.util.Log.d("DICTO_FLOATING", ">>> [LOAD_POSITION_START] loadAndShowButton() called")
        var positionLoaded = false
        var loadedX = 0
        var loadedY = 100

        // Launch coroutine to load position
        serviceScope.launch {
            android.util.Log.d("DICTO_FLOATING", ">>> [LOAD_POSITION_COROUTINE_START] Getting preferences flows")
            val savedX = preferencesManager?.floatingButtonX
            val savedY = preferencesManager?.floatingButtonY

            android.util.Log.d("DICTO_FLOATING", ">>> [LOAD_POSITION_CHECK] savedX=$savedX, savedY=$savedY")

            if (savedX != null && savedY != null) {
                android.util.Log.d("DICTO_FLOATING", ">>> [LOAD_POSITION_COMBINING] Combining X and Y flows")
                combine(savedX, savedY) { x, y -> Pair(x, y) }
                    .take(1)  // Only first emission to prevent spam
                    .collect { (x, y) ->
                        loadedX = x
                        loadedY = y
                        positionLoaded = true

                        android.util.Log.d("DICTO_FLOATING", ">>> [LOAD_POSITION_SUCCESS] Position loaded from Flow: x=$x, y=$y")
                        FloatingWindowLogger.loadedPosition(x, y)
                        AppLogger.debug("FloatingWindowCoordinator", "Loaded saved position: x=$x, y=$y")

                        android.util.Log.d("DICTO_FLOATING", ">>> [LOAD_POSITION_INIT_MANAGERS] Initializing managers with x=$x, y=$y")
                        initializeManagers(loadedX, loadedY)
                        showButton()
                        AppLogger.logServiceState("FloatingWindowCoordinator", "WINDOW_CREATED", "Button visible")
                    }
            } else {
                android.util.Log.d("DICTO_FLOATING", ">>> [LOAD_POSITION_NO_SAVED] No saved position found in preferences")
                positionLoaded = true  // Mark as loaded so fallback won't run
            }
        }

        // Fallback: if position doesn't load in 1000ms, show with defaults
        serviceScope.launch {
            delay(1000)
            if (!positionLoaded && buttonManager == null) {
                android.util.Log.d("DICTO_FLOATING", ">>> [LOAD_POSITION_FALLBACK] Timeout reached, using defaults (0, 100)")
                FloatingWindowLogger.loadedPosition(0, 100)
                initializeManagers(0, 100)
                showButton()
                AppLogger.logServiceState("FloatingWindowCoordinator", "WINDOW_CREATED", "Button visible (fallback)")
            } else if (positionLoaded) {
                android.util.Log.d("DICTO_FLOATING", ">>> [LOAD_POSITION_FALLBACK_SKIP] Position already loaded, skipping fallback")
            } else if (buttonManager != null) {
                android.util.Log.d("DICTO_FLOATING", ">>> [LOAD_POSITION_FALLBACK_SKIP] ButtonManager already initialized, skipping fallback")
            }
        }
    }

    private fun initializeManagers(savedX: Int = 0, savedY: Int = 100) {
        if (buttonManager == null && windowManager != null) {
            trashBinManager = TrashBinManager(service, windowManager!!)

            buttonManager = FloatingButtonManager(
                context = service,
                windowManager = windowManager!!,
                onButtonTapped = ::onButtonTapped,
                onDragStart = ::onDragStart,
                onDragMove = ::onDragMove,
                onDragEnd = ::onDragEnd,
                initialX = savedX,
                initialY = savedY
            )
        }
    }

    private fun showButton() {
        buttonManager?.show()
    }

    private fun onButtonTapped() {
        AppLogger.logUserAction("Floating Button Tapped", "Opening translator overlay - hiding button")
        try {
            buttonManager?.hide()
            trashBinManager?.hide()

            val intent = Intent(service, FloatingTranslatorActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            service.startActivity(intent)
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

    private fun onDragEnd(x: Float, y: Float, finalX: Int, finalY: Int, wasDragging: Boolean) {
        if (wasDragging && trashBinManager?.isNear(x, y) == true) {
            AppLogger.logUserAction("Floating Button", "Dropped on trash - closing")
            // Don't save position when dropped on trash
            service.stopSelf()
        } else if (wasDragging) {
            // Save position via PositionPersistence (handles constraining)
            positionPersistence?.savePosition(finalX, finalY)
            FloatingWindowLogger.positionSaved(finalX, finalY)
        }
        trashBinManager?.hide()
    }

    @Suppress("UnspecifiedRegisterReceiverFlag")
    private fun registerRestoreReceiver() {
        restoreReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "com.example.dicto.RESTORE_FLOATING_BUTTON") {
                    AppLogger.logServiceState("FloatingWindowCoordinator", "Restoring floating button")
                    buttonManager?.restore()
                }
            }
        }
        val filter = IntentFilter("com.example.dicto.RESTORE_FLOATING_BUTTON")

        if (Build.VERSION.SDK_INT >= 33) {
            service.registerReceiver(restoreReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            service.registerReceiver(restoreReceiver, filter)
        }
    }

    private fun unregisterRestoreReceiver() {
        if (restoreReceiver != null) {
            try {
                service.unregisterReceiver(restoreReceiver)
            } catch (e: Exception) {
                AppLogger.warn("FloatingWindowCoordinator", "Error unregistering receiver: ${e.message}")
            }
        }
    }
}



