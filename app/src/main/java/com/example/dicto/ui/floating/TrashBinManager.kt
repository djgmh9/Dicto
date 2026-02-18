package com.example.dicto.ui.floating

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import android.widget.ImageView
import com.example.dicto.utils.AppLogger

/**
 * TrashBinManager - Manages the trash bin for deleting floating button
 *
 * Single Responsibility: Show/hide trash bin and detect proximity
 * Separation of Concerns: Trash bin logic separated from button and service
 */
class TrashBinManager(
    private val context: Context,
    private val windowManager: WindowManager
) {
    private var trashView: ImageView? = null
    private var trashParams: WindowManager.LayoutParams? = null

    companion object {
        private const val TRASH_SIZE = 200
        private const val TRASH_BOTTOM_PADDING = 100
        private const val PROXIMITY_THRESHOLD = 200
    }

    /**
     * Show the trash bin at the bottom center of screen
     */
    fun show() {
        if (trashView != null) {
            AppLogger.debug("TrashBinManager", "Trash bin already visible")
            return
        }

        try {
            val screenWidth = windowManager.defaultDisplay?.width ?: 1080
            val screenHeight = windowManager.defaultDisplay?.height ?: 1920

            trashView = ImageView(context).apply {
                setBackgroundColor(Color.parseColor("#FF0000"))
                setImageResource(android.R.drawable.ic_menu_delete)
                scaleType = ImageView.ScaleType.CENTER
                alpha = 0.7f
            }

            trashParams = WindowManager.LayoutParams(
                TRASH_SIZE,
                TRASH_SIZE,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                y = TRASH_BOTTOM_PADDING  // Offset from bottom
                width = TRASH_SIZE
                height = TRASH_SIZE
            }

            windowManager.addView(trashView, trashParams)
            AppLogger.debug("TrashBinManager", "Trash bin shown at bottom center (screen: ${screenWidth}x${screenHeight})")
        } catch (e: Exception) {
            AppLogger.error("TrashBinManager", "Error showing trash bin", e)
        }
    }

    /**
     * Hide the trash bin
     */
    fun hide() {
        try {
            if (trashView != null && trashView?.windowToken != null) {
                windowManager.removeView(trashView)
                trashView = null
                trashParams = null
                AppLogger.debug("TrashBinManager", "Trash bin hidden")
            }
        } catch (e: Exception) {
            AppLogger.error("TrashBinManager", "Error hiding trash bin", e)
        }
    }

    /**
     * Update trash bin visual state based on proximity
     */
    fun updateState(x: Float, y: Float) {
        if (isNear(x, y)) {
            trashView?.alpha = 1.0f
            trashView?.setBackgroundColor(Color.parseColor("#FF3333"))
        } else {
            trashView?.alpha = 0.7f
            trashView?.setBackgroundColor(Color.parseColor("#FF0000"))
        }
    }

    /**
     * Check if coordinates are near the trash bin
     */
    fun isNear(x: Float, y: Float): Boolean {
        val screenWidth = windowManager.defaultDisplay?.width ?: 1080
        val screenHeight = windowManager.defaultDisplay?.height ?: 1920

        // Calculate center of trash bin - matches gravity positioning
        // Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL means:
        // - X: screen center
        // - Y: screen height - TRASH_BOTTOM_PADDING - (TRASH_SIZE / 2)
        val trashCenterX = (screenWidth / 2).toFloat()
        val trashCenterY = (screenHeight - TRASH_BOTTOM_PADDING - (TRASH_SIZE / 2)).toFloat()

        val distance = Math.sqrt(
            Math.pow((x - trashCenterX).toDouble(), 2.0) +
            Math.pow((y - trashCenterY).toDouble(), 2.0)
        )

        val isNear = distance < PROXIMITY_THRESHOLD
        AppLogger.debug("TrashBinManager", "Distance to trash: ${distance.toInt()}, isNear: $isNear (trash center: $trashCenterX, $trashCenterY)")
        return isNear
    }

    /**
     * Destroy the trash bin completely
     */
    fun destroy() {
        hide()
    }
}

