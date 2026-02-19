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
import com.example.dicto.utils.PreferencesManager
import com.example.dicto.ui.floating.util.PositionPersistence
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

/**
 * FloatingWindowService - Coordinates floating window components
 *
 * Single Responsibility: Service lifecycle and component coordination
 * Delegates to:
 * - FloatingButtonManager: Button creation and touch handling
 * - TrashBinManager: Trash bin display and proximity detection
 * - NotificationHelper: Foreground notification management
 * - PositionPersistence: Position loading/saving/constraining (shared)
 */
class FloatingWindowService : Service() {

    private var windowManager: WindowManager? = null
    private var buttonManager: FloatingButtonManager? = null
    private var trashBinManager: TrashBinManager? = null
    private var notificationHelper: NotificationHelper? = null
    private var restoreReceiver: BroadcastReceiver? = null
    private var preferencesManager: PreferencesManager? = null
    private var positionPersistence: PositionPersistence? = null

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())


    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        android.util.Log.d("DICTO_FLOATING", ">>> FloatingWindowService.onCreate() called")
        AppLogger.logServiceState("FloatingWindowService", "CREATED")

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        preferencesManager = PreferencesManager(this)
        positionPersistence = PositionPersistence(this, preferencesManager!!, serviceScope)
        notificationHelper = NotificationHelper(this)
        notificationHelper?.createNotificationChannel()

        registerRestoreReceiver()
        android.util.Log.d("DICTO_FLOATING", ">>> FloatingWindowService.onCreate() completed")
    }

    @Suppress("UnspecifiedRegisterReceiverFlag")
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

        if (Build.VERSION.SDK_INT >= 33) {  // Android 13+ (TIRAMISU)
            registerReceiver(restoreReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(restoreReceiver, filter)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        android.util.Log.d("DICTO_FLOATING", ">>> FloatingWindowService.onStartCommand() called")
        AppLogger.logServiceState("FloatingWindowService", "STARTED", "Building notification")

        try {
            // Start foreground service
            val notification = notificationHelper?.createNotification()
            if (notification != null) {
                android.util.Log.d("DICTO_FLOATING", ">>> FloatingWindowService starting foreground")
                AppLogger.logServiceState("FloatingWindowService", "STARTING_FOREGROUND")
                startForeground(NotificationHelper.NOTIFICATION_ID, notification)
                android.util.Log.d("DICTO_FLOATING", ">>> FloatingWindowService foreground started")
                AppLogger.logServiceState("FloatingWindowService", "FOREGROUND_ACTIVE", "Notification shown")
            }

            // Initialize managers
            if (windowManager != null) {
                android.util.Log.d("DICTO_FLOATING", ">>> FloatingWindowService loading saved position and initializing")

                // Load saved position from preferences SYNCHRONOUSLY
                var loadedX = 0
                var loadedY = 100
                var positionLoaded = false

                // Launch a coroutine to load position ONCE
                serviceScope.launch {
                    val savedX = preferencesManager?.floatingButtonX
                    val savedY = preferencesManager?.floatingButtonY

                    // Combine both Flows and take only the FIRST emission
                    if (savedX != null && savedY != null) {
                        combine(savedX, savedY) { x, y -> Pair(x, y) }
                            .take(1)  // Only take the first emission to prevent spam
                            .collect { (x, y) ->
                                loadedX = x
                                loadedY = y
                                positionLoaded = true

                                android.util.Log.d("DICTO_FLOATING", ">>> FloatingWindowService loaded position from Flow: x=$x, y=$y")
                                AppLogger.debug("FloatingWindowService", "Loaded saved position: x=$x, y=$y")

                                // Initialize managers with loaded position
                                android.util.Log.d("DICTO_FLOATING", ">>> FloatingWindowService about to initializeManagers with x=$loadedX, y=$loadedY")
                                initializeManagers(loadedX, loadedY)

                                // Show the button with correct position
                                android.util.Log.d("DICTO_FLOATING", ">>> FloatingWindowService calling buttonManager.show() with correct position")
                                buttonManager?.show()
                                android.util.Log.d("DICTO_FLOATING", ">>> FloatingWindowService button shown with position x=$loadedX, y=$loadedY")
                                AppLogger.logServiceState("FloatingWindowService", "WINDOW_CREATED", "Button visible")
                            }
                    }
                }

                // Fallback: if position doesn't load in 500ms, show with defaults
                android.util.Log.d("DICTO_FLOATING", ">>> FloatingWindowService scheduled fallback task")
                serviceScope.launch {
                    kotlinx.coroutines.delay(500)
                    if (!positionLoaded) {
                        android.util.Log.d("DICTO_FLOATING", ">>> FloatingWindowService FALLBACK: position load timeout, showing with defaults x=0, y=100")
                        initializeManagers(0, 100)
                        buttonManager?.show()
                        android.util.Log.d("DICTO_FLOATING", ">>> FloatingWindowService button shown with default position (fallback)")
                        AppLogger.logServiceState("FloatingWindowService", "WINDOW_CREATED", "Button visible (fallback)")
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("DICTO_FLOATING", ">>> FloatingWindowService.onStartCommand() EXCEPTION: ${e.message}", e)
            AppLogger.logServiceState("FloatingWindowService", "ERROR", e.message ?: "Unknown error")
            AppLogger.error("FloatingWindowService", "Error in onStartCommand", e)
            stopSelf()
        }

        android.util.Log.d("DICTO_FLOATING", ">>> FloatingWindowService.onStartCommand() returning START_STICKY")
        AppLogger.logServiceState("FloatingWindowService", "STICKY_MODE", "Will restart if killed")
        return START_STICKY
    }

    private fun initializeManagers(savedX: Int = 0, savedY: Int = 100) {
        if (buttonManager == null && windowManager != null) {
            trashBinManager = TrashBinManager(this, windowManager!!)

            buttonManager = FloatingButtonManager(
                context = this,
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

    private fun onPositionChanged(x: Int, y: Int) {
        // Deprecated - position saving now handled in onDragEnd to avoid saving trash bin positions
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

    private fun onDragEnd(x: Float, y: Float, finalX: Int, finalY: Int, wasDragging: Boolean) {
        android.util.Log.d("DICTO_FLOATING", ">>> onDragEnd called: touchX=$x, touchY=$y, finalX=$finalX, finalY=$finalY, wasDragging=$wasDragging")

        if (wasDragging && trashBinManager?.isNear(x, y) == true) {
            android.util.Log.d("DICTO_FLOATING", ">>> Dropped on trash - NOT saving position, keeping previous location")
            AppLogger.logUserAction("Floating Button", "Dropped on trash - closing")
            // Don't save position when dropped on trash - button will restore to previous location
            stopSelf()
        } else if (wasDragging) {
            android.util.Log.d("DICTO_FLOATING", ">>> Normal drag end - saving position via PositionPersistence")
            // Use PositionPersistence to save (it handles constraining)
            positionPersistence?.savePosition(finalX, finalY)
            android.util.Log.d("DICTO_FLOATING", ">>> Position saved via PositionPersistence")
        }
        trashBinManager?.hide()
    }

    override fun onDestroy() {
        super.onDestroy()
        android.util.Log.d("DICTO_FLOATING", ">>> FloatingWindowService.onDestroy() called")
        AppLogger.logServiceState("FloatingWindowService", "DESTROYING")

        try {
            // Unregister broadcast receiver
            if (restoreReceiver != null) {
                android.util.Log.d("DICTO_FLOATING", ">>> FloatingWindowService unregistering receiver")
                unregisterReceiver(restoreReceiver)
                AppLogger.debug("FloatingWindow", "Broadcast receiver unregistered")
            }

            // Destroy managers
            android.util.Log.d("DICTO_FLOATING", ">>> FloatingWindowService destroying managers")
            trashBinManager?.destroy()
            buttonManager?.destroy()
            android.util.Log.d("DICTO_FLOATING", ">>> FloatingWindowService managers destroyed")
        } catch (e: Exception) {
            android.util.Log.e("DICTO_FLOATING", ">>> FloatingWindowService.onDestroy() ERROR: ${e.message}", e)
            AppLogger.error("FloatingWindowService", "Error removing views", e)
        }

        if (Build.VERSION.SDK_INT >= 33) {
            @Suppress("NewApi")
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        android.util.Log.d("DICTO_FLOATING", ">>> FloatingWindowService.onDestroy() completed")
        AppLogger.logServiceState("FloatingWindowService", "DESTROYED", "Foreground stopped")
    }
}
