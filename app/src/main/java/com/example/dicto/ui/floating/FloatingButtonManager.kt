package com.example.dicto.ui.floating

import android.content.Context
import android.view.WindowManager
import com.example.dicto.ui.floating.manager.FloatingButtonView
import com.example.dicto.ui.floating.manager.FloatingButtonTouchHandler

/**
 * FloatingButtonManager - Orchestrates floating button components
 *
 * Single Responsibility: Coordinate UI and touch handling
 * Separation of Concerns: Delegates to FloatingButtonView (UI) and FloatingButtonTouchHandler (Touch)
 *
 * This class acts as a facade to simplify the interface and coordinate between
 * the view and touch handler components.
 */
class FloatingButtonManager(
    private val context: Context,
    private val windowManager: WindowManager,
    private val onButtonTapped: () -> Unit,
    private val onDragStart: () -> Unit,
    private val onDragMove: (Float, Float) -> Unit,
    private val onDragEnd: (Float, Float, Int, Int, Boolean) -> Unit,
    initialX: Int = 0,
    initialY: Int = 100
) {
    // ...existing code...
    private val buttonView = FloatingButtonView(context, windowManager, initialX, initialY)

    private var touchHandler: FloatingButtonTouchHandler? = null

    /**
     * Create and show the floating button
     */
    fun show() {
        android.util.Log.d("DICTO_FLOATING", ">>> FloatingButtonManager.show() delegating to buttonView")

        // Show button first
        buttonView.show { event ->
            // Get the floatingView and create touch handler on first touch
            val floatingView = buttonView.getFloatingView()
            if (floatingView != null && touchHandler == null) {
                touchHandler = FloatingButtonTouchHandler(
                    windowManager = windowManager,
                    floatingView = floatingView,
                    onButtonTapped = onButtonTapped,
                    onDragStart = onDragStart,
                    onDragMove = onDragMove,
                    onDragEnd = onDragEnd
                )
            }

            touchHandler?.handleTouch(event, buttonView.getLayoutParams() ?: return@show false) ?: false
        }
    }

    /**
     * Hide the floating button (temporarily, keeps reference for restoration)
     */
    fun hide() {
        android.util.Log.d("DICTO_FLOATING", ">>> FloatingButtonManager.hide() delegating to buttonView")
        buttonView.hide()
        touchHandler?.resetTouchState()
    }

    /**
     * Restore the floating button if it was hidden
     */
    fun restore() {
        android.util.Log.d("DICTO_FLOATING", ">>> FloatingButtonManager.restore() delegating to buttonView")
        buttonView.restore()
    }

    /**
     * Destroy the floating button completely
     */
    fun destroy() {
        android.util.Log.d("DICTO_FLOATING", ">>> FloatingButtonManager.destroy() delegating to buttonView")
        buttonView.destroy()
        touchHandler = null
    }

    /**
     * Check if button is currently visible
     */
    fun isVisible(): Boolean = buttonView.isVisible()

    /**
     * Get current button position
     * @return Pair of (x, y) coordinates or null if not initialized
     */
    fun getCurrentPosition(): Pair<Int, Int>? = buttonView.getCurrentPosition()
}

