package com.example.dicto.ui.floating.manager

import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.example.dicto.utils.AppLogger
import kotlin.math.abs

/**
 * FloatingButtonTouchHandler - Handles touch events for floating button
 *
 * Single Responsibility: Process touch events and calculate drag operations
 * Separation of Concerns: Touch logic separated from UI and position management
 *
 * Responsibilities:
 * - Track touch start position
 * - Calculate drag deltas
 * - Detect drag vs tap
 * - Invoke appropriate callbacks
 * - Update window parameters
 */
class FloatingButtonTouchHandler(
    private val windowManager: WindowManager,
    private val floatingView: View,
    private val onButtonTapped: () -> Unit,
    private val onDragStart: () -> Unit,
    private val onDragMove: (Float, Float) -> Unit,
    private val onDragEnd: (Float, Float, Int, Int, Boolean) -> Unit
) {
    companion object {
        private const val DRAG_THRESHOLD = 10
    }

    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var initialButtonX = 0  // Store initial button position
    private var initialButtonY = 0  // Store initial button position
    private var isDragging = false

    /**
     * Handle touch events from the floating button
     * @param event MotionEvent from the button
     * @param layoutParams Current WindowManager.LayoutParams
     * @return true if event was handled
     */
    fun handleTouch(event: MotionEvent, layoutParams: WindowManager.LayoutParams): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> handleActionDown(event, layoutParams)
            MotionEvent.ACTION_MOVE -> handleActionMove(event, layoutParams)
            MotionEvent.ACTION_UP -> handleActionUp(event, layoutParams)
            else -> false
        }
    }

    private fun handleActionDown(event: MotionEvent, layoutParams: WindowManager.LayoutParams): Boolean {
        android.util.Log.d("DICTO_FLOATING", ">>> FloatingButtonTouchHandler ACTION_DOWN at (${event.rawX}, ${event.rawY})")

        // Store initial touch position
        initialTouchX = event.rawX
        initialTouchY = event.rawY

        // Store initial button position as the starting point for delta calculations
        initialButtonX = layoutParams.x
        initialButtonY = layoutParams.y
        android.util.Log.d("DICTO_FLOATING", ">>> FloatingButtonTouchHandler stored initial button position: x=$initialButtonX, y=$initialButtonY")

        isDragging = false

        return true
    }

    private fun handleActionMove(event: MotionEvent, layoutParams: WindowManager.LayoutParams): Boolean {
        val deltaX = (event.rawX - initialTouchX).toInt()
        val deltaY = (event.rawY - initialTouchY).toInt()

        if (abs(deltaX) > DRAG_THRESHOLD || abs(deltaY) > DRAG_THRESHOLD) {
            if (!isDragging) {
                android.util.Log.d("DICTO_FLOATING", ">>> FloatingButtonTouchHandler drag started")
                isDragging = true
                onDragStart()
            }

            // Calculate new position based on INITIAL button position + delta (not current position)
            val newX = initialButtonX + deltaX
            val newY = initialButtonY + deltaY

            // Update layout params
            layoutParams.x = newX
            layoutParams.y = newY

            // Update window with correct view reference
            windowManager.updateViewLayout(floatingView, layoutParams)

            android.util.Log.d("DICTO_FLOATING", ">>> FloatingButtonTouchHandler dragging: delta=($deltaX, $deltaY), new position=(${layoutParams.x}, ${layoutParams.y})")

            onDragMove(event.rawX, event.rawY)
            AppLogger.debug("FloatingButtonTouchHandler", "Dragging to (${layoutParams.x}, ${layoutParams.y})")
        }
        return true
    }

    private fun handleActionUp(event: MotionEvent, layoutParams: WindowManager.LayoutParams): Boolean {
        if (!isDragging) {
            android.util.Log.d("DICTO_FLOATING", ">>> FloatingButtonTouchHandler tapped")
            AppLogger.logUserAction("Floating Button", "Tapped")
            onButtonTapped()
        } else {
            android.util.Log.d("DICTO_FLOATING", ">>> FloatingButtonTouchHandler ACTION_UP: final position x=${layoutParams.x}, y=${layoutParams.y}")
            AppLogger.debug("FloatingButtonTouchHandler", "Drag ended at (${event.rawX}, ${event.rawY})")
            // Pass position to onDragEnd for service to decide whether to save
            onDragEnd(event.rawX, event.rawY, layoutParams.x, layoutParams.y, isDragging)
        }
        isDragging = false
        return true
    }

    /**
     * Reset touch state (used when button is hidden/destroyed)
     */
    fun resetTouchState() {
        isDragging = false
        initialTouchX = 0f
        initialTouchY = 0f
        initialButtonX = 0
        initialButtonY = 0
    }

    /**
     * Check if currently dragging
     */
    fun isDraggingNow(): Boolean = isDragging
}


