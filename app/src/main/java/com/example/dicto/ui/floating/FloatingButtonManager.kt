package com.example.dicto.ui.floating

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.ImageView
import com.example.dicto.utils.AppLogger

/**
 * FloatingButtonManager - Manages the draggable floating button
 *
 * Single Responsibility: Create, position, and handle touch events for floating button
 * Separation of Concerns: UI creation and touch handling separated from service logic
 */
class FloatingButtonManager(
    private val context: Context,
    private val windowManager: WindowManager,
    private val onButtonTapped: () -> Unit,
    private val onDragStart: () -> Unit,
    private val onDragMove: (Float, Float) -> Unit,
    private val onDragEnd: (Float, Float, Boolean) -> Unit
) {
    private var floatingView: ImageView? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false

    companion object {
        private const val BUTTON_SIZE = 150
        private const val DRAG_THRESHOLD = 10
    }

    /**
     * Create and show the floating button
     */
    fun show() {
        if (floatingView != null) {
            AppLogger.debug("FloatingButtonManager", "Button already exists, skipping creation")
            return
        }

        val params = createLayoutParams()
        layoutParams = params

        floatingView = createFloatingView(params)
        windowManager.addView(floatingView, params)
        AppLogger.debug("FloatingButtonManager", "Floating button created and shown")
    }

    /**
     * Hide the floating button (temporarily, keeps reference for restoration)
     */
    fun hide() {
        try {
            if (floatingView != null && floatingView?.windowToken != null) {
                windowManager.removeView(floatingView)
                AppLogger.debug("FloatingButtonManager", "Floating button hidden")
            }
        } catch (e: Exception) {
            AppLogger.error("FloatingButtonManager", "Error hiding button", e)
        }
    }

    /**
     * Restore the floating button if it was hidden
     */
    fun restore() {
        try {
            if (floatingView != null && layoutParams != null && floatingView?.windowToken == null) {
                windowManager.addView(floatingView, layoutParams)
                AppLogger.debug("FloatingButtonManager", "Floating button restored")
            }
        } catch (e: Exception) {
            AppLogger.error("FloatingButtonManager", "Error restoring button", e)
        }
    }

    /**
     * Destroy the floating button completely
     */
    fun destroy() {
        try {
            if (floatingView != null && floatingView?.windowToken != null) {
                windowManager.removeView(floatingView)
            }
            floatingView = null
            layoutParams = null
            AppLogger.debug("FloatingButtonManager", "Floating button destroyed")
        } catch (e: Exception) {
            AppLogger.error("FloatingButtonManager", "Error destroying button", e)
        }
    }

    /**
     * Check if button is currently visible
     */
    fun isVisible(): Boolean {
        return floatingView?.windowToken != null
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
            x = 0
            y = 100
            width = BUTTON_SIZE
            height = BUTTON_SIZE
        }
    }

    private fun createFloatingView(params: WindowManager.LayoutParams): ImageView {
        return ImageView(context).apply {
            setBackgroundColor(Color.parseColor("#6200EE"))
            setImageResource(android.R.drawable.ic_menu_search)
            scaleType = ImageView.ScaleType.CENTER

            setOnTouchListener { _, event ->
                handleTouch(event, params)
            }
        }
    }

    private fun handleTouch(event: MotionEvent, params: WindowManager.LayoutParams): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = params.x
                initialY = params.y
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                isDragging = false
                onDragStart()
                AppLogger.debug("FloatingButtonManager", "Touch DOWN at (${event.rawX}, ${event.rawY})")
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.rawX - initialTouchX
                val deltaY = event.rawY - initialTouchY

                if (Math.abs(deltaX) > DRAG_THRESHOLD || Math.abs(deltaY) > DRAG_THRESHOLD) {
                    isDragging = true
                    params.x = (initialX + deltaX).toInt()
                    params.y = (initialY + deltaY).toInt()
                    windowManager.updateViewLayout(floatingView, params)
                    onDragMove(event.rawX, event.rawY)
                    AppLogger.debug("FloatingButtonManager", "Dragging to (${params.x}, ${params.y})")
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                if (!isDragging) {
                    AppLogger.logUserAction("Floating Button", "Tapped")
                    onButtonTapped()
                } else {
                    AppLogger.debug("FloatingButtonManager", "Drag ended at (${event.rawX}, ${event.rawY})")
                    onDragEnd(event.rawX, event.rawY, isDragging)
                }
                isDragging = false
                return true
            }

            else -> return false
        }
    }
}

