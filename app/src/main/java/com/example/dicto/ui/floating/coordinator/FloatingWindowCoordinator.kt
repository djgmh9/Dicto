package com.example.dicto.ui.floating.coordinator

import android.app.Service
import android.content.Context
import android.content.Intent
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
 * Button visibility is controlled via Intent actions sent to the service:
 * - ACTION_SHOW: Show the floating button (sent when leaving any screen)
 * - ACTION_HIDE: Hide the floating button (sent when entering overlay or main app)
 *
 * This avoids broadcast race conditions by routing all commands through
 * onStartCommand, which is sequential and safe.
 */
class FloatingWindowCoordinator(private val service: Service) {

    companion object {
        const val ACTION_SHOW = "com.example.dicto.SHOW_FLOATING_BUTTON"
        const val ACTION_HIDE = "com.example.dicto.HIDE_FLOATING_BUTTON"
    }

    private var windowManager: WindowManager? = null
    private var buttonManager: FloatingButtonManager? = null
    private var trashBinManager: TrashBinManager? = null
    private var notificationHelper: NotificationHelper? = null
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
    }

    fun start(intent: Intent?): Int {
        FloatingWindowLogger.onStartCommand()
        AppLogger.logServiceState("FloatingWindowCoordinator", "STARTING", "Building notification")

        return try {
            val action = intent?.action
            android.util.Log.d("DICTO_FLOATING", ">>> [START_COMMAND] action=$action")

            // Start foreground if not already running
            if (action != ACTION_SHOW && action != ACTION_HIDE) {
                // First start - set up foreground and load button
                val notification = notificationHelper?.createNotification()
                if (notification != null) {
                    FloatingWindowLogger.startingForeground()
                    AppLogger.logServiceState("FloatingWindowCoordinator", "STARTING_FOREGROUND")
                    service.startForeground(1, notification)
                    FloatingWindowLogger.foregroundStarted()
                    AppLogger.logServiceState("FloatingWindowCoordinator", "FOREGROUND_ACTIVE", "Notification shown")
                }
                loadAndShowButton()
            } else if (action == ACTION_SHOW) {
                handleShowAction()
            } else if (action == ACTION_HIDE) {
                handleHideAction()
            }

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

    private fun handleShowAction() {
        android.util.Log.d("DICTO_FLOATING", ">>> [ACTION_SHOW] Showing floating button")
        if (buttonManager == null) {
            android.util.Log.d("DICTO_FLOATING", ">>> [ACTION_SHOW] Managers not ready, loading position first")
            loadAndShowButton()
            return
        }
        if (buttonManager?.isVisible() == true) {
            android.util.Log.d("DICTO_FLOATING", ">>> [ACTION_SHOW] Button already visible, skipping")
            return
        }
        android.util.Log.d("DICTO_FLOATING", ">>> [ACTION_SHOW] Calling buttonManager.show()")
        buttonManager?.show()
    }

    private fun handleHideAction() {
        android.util.Log.d("DICTO_FLOATING", ">>> [ACTION_HIDE] Hiding floating button")
        buttonManager?.hide()
    }

    fun cleanup() {
        AppLogger.logServiceState("FloatingWindowCoordinator", "CLEANING_UP")
        android.util.Log.d("DICTO_FLOATING", ">>> [CLEANUP_START] Cleaning up floating window service")

        try {
            // Save current button position synchronously before destroying
            val position = buttonManager?.getCurrentPosition()
            if (position != null) {
                val (x, y) = position
                android.util.Log.d("DICTO_FLOATING", ">>> [CLEANUP] Saving position: x=$x, y=$y")
                try {
                    kotlinx.coroutines.runBlocking {
                        positionPersistence?.savePositionSync(x, y)
                    }
                    android.util.Log.d("DICTO_FLOATING", ">>> [CLEANUP] Position saved: x=$x, y=$y")
                    FloatingWindowLogger.positionSaved(x, y)
                } catch (e: Exception) {
                    android.util.Log.e("DICTO_FLOATING", ">>> [CLEANUP] Error saving position: ${e.message}", e)
                }
            }

            trashBinManager?.destroy()
            buttonManager?.destroy()
            buttonManager = null
            trashBinManager = null
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

    private fun loadAndShowButton() {
        if (windowManager == null) return

        android.util.Log.d("DICTO_FLOATING", ">>> [LOAD_POSITION_START] loadAndShowButton() called")
        var positionLoaded = false

        serviceScope.launch {
            val savedX = preferencesManager?.floatingButtonX
            val savedY = preferencesManager?.floatingButtonY

            if (savedX != null && savedY != null) {
                combine(savedX, savedY) { x, y -> Pair(x, y) }
                    .take(1)
                    .collect { (x, y) ->
                        positionLoaded = true
                        android.util.Log.d("DICTO_FLOATING", ">>> [LOAD_POSITION_SUCCESS] x=$x, y=$y")
                        FloatingWindowLogger.loadedPosition(x, y)
                        initializeManagers(x, y)
                        buttonManager?.show()
                        AppLogger.logServiceState("FloatingWindowCoordinator", "WINDOW_CREATED", "Button visible")
                    }
            } else {
                positionLoaded = true
            }
        }

        // Fallback after 1000ms
        serviceScope.launch {
            delay(1000)
            if (!positionLoaded && buttonManager == null) {
                android.util.Log.d("DICTO_FLOATING", ">>> [LOAD_POSITION_FALLBACK] using defaults")
                initializeManagers(0, 100)
                buttonManager?.show()
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
            buttonManager?.show()
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
            service.stopSelf()
        } else if (wasDragging) {
            positionPersistence?.savePosition(finalX, finalY)
            FloatingWindowLogger.positionSaved(finalX, finalY)
        }
        trashBinManager?.hide()
    }
}
