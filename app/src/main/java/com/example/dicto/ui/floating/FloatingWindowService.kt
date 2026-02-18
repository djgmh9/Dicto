package com.example.dicto.ui.floating

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.example.dicto.MainActivity
import com.example.dicto.R

/**
 * FloatingWindowService - Displays a floating translation button
 *
 * Single Responsibility: Manage floating window lifecycle and interactions
 * Features:
 * - Always-on-top floating button
 * - Simple ImageView implementation (no Compose to avoid crashes)
 * - Opens app on tap
 */
class FloatingWindowService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("FloatingWindowService", "onCreate called")
        try {
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            createFloatingWindow()
        } catch (e: Exception) {
            Log.e("FloatingWindowService", "Error in onCreate: ${e.message}", e)
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("FloatingWindowService", "Service started")
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
        Log.d("FloatingWindowService", "Service destroyed")
    }

    private fun createFloatingWindow() {
        try {
            Log.d("FloatingWindowService", "Creating floating window...")

            // Create a simple ImageView instead of ComposeView
            floatingView = ImageView(this).apply {
                // Use a simple drawable or create a colored circle
                setBackgroundColor(Color.parseColor("#6200EE"))
                setImageResource(android.R.drawable.ic_menu_search)
                scaleType = ImageView.ScaleType.CENTER

                setOnClickListener {
                    Log.d("FloatingWindowService", "Floating button clicked")
                    try {
                        val intent = Intent(this@FloatingWindowService, FloatingTranslatorActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)
                    } catch (e: Exception) {
                        Log.e("FloatingWindowService", "Error opening app: ${e.message}", e)
                    }
                }
            }

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

            windowManager?.addView(floatingView, params)
            Log.d("FloatingWindowService", "Floating window created successfully")
        } catch (e: Exception) {
            Log.e("FloatingWindowService", "Error creating floating window: ${e.message}", e)
            stopSelf()
        }
    }
}

