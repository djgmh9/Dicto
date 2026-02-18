package com.example.dicto.ui.floating.manager

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.ImageView
import com.example.dicto.utils.AppLogger

/**
 * FloatingButtonView - Creates and manages floating button UI
 *
 * Single Responsibility: UI creation, visibility, and window management
 * Separation of Concerns: UI rendering separated from touch logic and service logic
 *
 * Responsibilities:
 * - Create ImageView for button
 * - Manage window parameters
 * - Handle show/hide/restore
 * - Provide access to button reference for touch events
 */
class FloatingButtonView(
    private val context: Context,
    private val windowManager: WindowManager,
    private val initialX: Int = 0,
    private val initialY: Int = 100
) {
    companion object {
        private const val BUTTON_SIZE = 150
    }

    private var floatingView: ImageView? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    /**
     * Create and add button to window
     * @param onTouchListener Callback for touch events
     */
    fun show(onTouchListener: (MotionEvent) -> Boolean) {
        android.util.Log.d("DICTO_FLOATING", ">>> FloatingButtonView.show() called, floatingView=$floatingView")

        if (floatingView != null) {
            android.util.Log.d("DICTO_FLOATING", ">>> FloatingButtonView.show() - Button already exists, checking if visible")
            if (floatingView?.windowToken != null) {
                android.util.Log.d("DICTO_FLOATING", ">>> FloatingButtonView.show() - Button already visible, skipping")
                AppLogger.debug("FloatingButtonView", "Button already exists, skipping creation")
                return
            } else {
                android.util.Log.d("DICTO_FLOATING", ">>> FloatingButtonView.show() - Button exists but not visible, adding to window")
            }
        }

        val params = createLayoutParams()
        layoutParams = params

        floatingView = createImageView(params, onTouchListener)

        android.util.Log.d("DICTO_FLOATING", ">>> FloatingButtonView.show() - About to addView")
        windowManager.addView(floatingView, params)
        android.util.Log.d("DICTO_FLOATING", ">>> FloatingButtonView.show() - Button added to window")
        AppLogger.debug("FloatingButtonView", "Floating button created and shown")
    }

    /**
     * Hide button temporarily (keeps reference for restoration)
     */
    fun hide() {
        android.util.Log.d("DICTO_FLOATING", ">>> FloatingButtonView.hide() called")
        try {
            if (floatingView != null && floatingView?.windowToken != null) {
                android.util.Log.d("DICTO_FLOATING", ">>> FloatingButtonView.hide() - Removing button from window")
                windowManager.removeView(floatingView)
                android.util.Log.d("DICTO_FLOATING", ">>> FloatingButtonView.hide() - Button removed")
                AppLogger.debug("FloatingButtonView", "Floating button hidden")
            }
        } catch (e: Exception) {
            android.util.Log.e("DICTO_FLOATING", ">>> FloatingButtonView.hide() ERROR: ${e.message}", e)
            AppLogger.error("FloatingButtonView", "Error hiding button", e)
        }
    }

    /**
     * Restore button if it was hidden
     */
    fun restore() {
        android.util.Log.d("DICTO_FLOATING", ">>> FloatingButtonView.restore() called")
        try {
            if (floatingView != null && layoutParams != null && floatingView?.windowToken == null) {
                android.util.Log.d("DICTO_FLOATING", ">>> FloatingButtonView.restore() - Re-adding button to window")
                windowManager.addView(floatingView, layoutParams)
                android.util.Log.d("DICTO_FLOATING", ">>> FloatingButtonView.restore() - Button restored")
                AppLogger.debug("FloatingButtonView", "Floating button restored")
            }
        } catch (e: Exception) {
            android.util.Log.e("DICTO_FLOATING", ">>> FloatingButtonView.restore() ERROR: ${e.message}", e)
            AppLogger.error("FloatingButtonView", "Error restoring button", e)
        }
    }

    /**
     * Completely destroy button (can't restore after)
     */
    fun destroy() {
        android.util.Log.d("DICTO_FLOATING", ">>> FloatingButtonView.destroy() called")
        try {
            if (floatingView != null && floatingView?.windowToken != null) {
                windowManager.removeView(floatingView)
            }
            floatingView = null
            layoutParams = null
            android.util.Log.d("DICTO_FLOATING", ">>> FloatingButtonView destroyed")
            AppLogger.debug("FloatingButtonView", "Floating button destroyed")
        } catch (e: Exception) {
            android.util.Log.e("DICTO_FLOATING", ">>> FloatingButtonView.destroy() ERROR: ${e.message}", e)
            AppLogger.error("FloatingButtonView", "Error destroying button", e)
        }
    }

    /**
     * Check if button is currently visible
     */
    fun isVisible(): Boolean = floatingView?.windowToken != null

    /**
     * Get current layout params for touch handling
     */
    fun getLayoutParams(): WindowManager.LayoutParams? = layoutParams

    /**
     * Get the floating button view reference
     */
    fun getFloatingView(): android.widget.ImageView? = floatingView

    /**
     * Update window layout (used during dragging)
     */
    fun updateLayout() {
        if (floatingView != null && layoutParams != null) {
            windowManager.updateViewLayout(floatingView, layoutParams)
        }
    }

    private fun createLayoutParams(): WindowManager.LayoutParams {
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            x = initialX
            y = initialY
            width = BUTTON_SIZE
            height = BUTTON_SIZE
        }
    }

    private fun createImageView(
        params: WindowManager.LayoutParams,
        onTouchListener: (MotionEvent) -> Boolean
    ): ImageView {
        return ImageView(context).apply {
            setBackgroundColor(Color.parseColor("#6200EE"))
            setImageResource(android.R.drawable.ic_menu_search)
            scaleType = ImageView.ScaleType.CENTER
            setOnTouchListener { _, event ->
                onTouchListener(event)
            }
        }
    }
}


