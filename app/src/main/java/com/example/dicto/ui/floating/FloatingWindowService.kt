package com.example.dicto.ui.floating

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import com.example.dicto.R

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
    private var layoutParams: WindowManager.LayoutParams? = null
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "floating_translator_channel"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("FloatingWindowService", "onCreate called")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("FloatingWindowService", "Service started")

        // Create and show notification for foreground service
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Dicto Translator")
            .setContentText("Floating translator is active")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        try {
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            createFloatingWindow()
        } catch (e: Exception) {
            Log.e("FloatingWindowService", "Error starting service: ${e.message}", e)
            stopSelf()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (floatingView != null && floatingView?.windowToken != null) {
                windowManager?.removeView(floatingView)
                Log.d("FloatingWindowService", "Floating view removed")
            }
        } catch (e: Exception) {
            Log.e("FloatingWindowService", "Error removing view: ${e.message}", e)
        }
        stopForeground(true)
        Log.d("FloatingWindowService", "Service destroyed")
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
            Log.d("FloatingWindowService", "Creating floating window...")

            // Create layout parameters first
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
                gravity = Gravity.TOP or Gravity.END
                x = 0
                y = 100
                width = 150
                height = 150
            }

            layoutParams = params

            // Create a simple ImageView
            floatingView = ImageView(this).apply {
                setBackgroundColor(Color.parseColor("#6200EE"))
                setImageResource(android.R.drawable.ic_menu_search)
                scaleType = ImageView.ScaleType.CENTER

                // Handle both drag and click
                setOnTouchListener { _, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            // Store current position from layoutParams
                            initialX = params.x
                            initialY = params.y
                            initialTouchX = event.rawX
                            initialTouchY = event.rawY
                            isDragging = false
                            true
                        }

                        MotionEvent.ACTION_MOVE -> {
                            val deltaX = event.rawX - initialTouchX
                            val deltaY = event.rawY - initialTouchY

                            // If movement is significant, start dragging
                            if (Math.abs(deltaX) > 10 || Math.abs(deltaY) > 10) {
                                isDragging = true
                                params.x = (initialX + deltaX).toInt()
                                params.y = (initialY + deltaY).toInt()
                                windowManager?.updateViewLayout(floatingView, params)
                            }
                            true
                        }

                        MotionEvent.ACTION_UP -> {
                            // If not dragging, treat as click
                            if (!isDragging) {
                                Log.d("FloatingWindowService", "Floating button clicked")
                                try {
                                    val intent = Intent(this@FloatingWindowService, FloatingTranslatorActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                    startActivity(intent)
                                } catch (e: Exception) {
                                    Log.e("FloatingWindowService", "Error opening app: ${e.message}", e)
                                }
                            }
                            isDragging = false
                            true
                        }

                        else -> false
                    }
                }
            }

            windowManager?.addView(floatingView, params)
            Log.d("FloatingWindowService", "Floating window created successfully")
        } catch (e: Exception) {
            Log.e("FloatingWindowService", "Error creating floating window: ${e.message}", e)
            stopSelf()
        }
    }
}


