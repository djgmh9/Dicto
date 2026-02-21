package com.example.dicto.data.local

import android.content.Context
import com.example.dicto.data.local.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * PositionPersistence - Centralized position management
 *
 * Single Responsibility: Load, save, validate, and constrain button position
 * Eliminates duplication between FloatingButtonManager and FloatingWindowService
 *
 * Provides:
 * - Load position from storage
 * - Save position to storage (with validation)
 * - Constrain positions to valid screen bounds
 */
class PositionPersistence(
    private val context: Context,
    private val preferencesManager: PreferencesManager,
    private val coroutineScope: CoroutineScope
) {
    companion object {
        private const val BUTTON_SIZE = 150
    }

    /**
     * Load position from persistent storage
     * @return Flow of position pairs (x, y)
     */
    fun loadPosition(): Pair<Flow<Int>, Flow<Int>>? {
        val savedX = preferencesManager.floatingButtonX
        val savedY = preferencesManager.floatingButtonY

        return if (savedY != null) {
            Pair(savedX, savedY)
        } else {
            null
        }
    }

    /**
     * Save position to persistent storage
     * IMPORTANT: Saves EXACT position, NO constraining!
     * WindowManager already handles bounds, so we save what user dragged to.
     * WARNING: This launches a coroutine but does NOT wait for completion!
     * For immediate saves during cleanup, use savePositionSync() instead.
     *
     * @param x X coordinate (saved as-is)
     * @param y Y coordinate (saved as-is)
     */
    fun savePosition(x: Int, y: Int) {
        android.util.Log.d("DICTO_FLOATING", ">>> PositionPersistence.savePosition() async: x=$x, y=$y (NO constraint)")
        coroutineScope.launch {
            preferencesManager.setFloatingButtonPosition(x, y)
            android.util.Log.d("DICTO_FLOATING", ">>> PositionPersistence async save completed: x=$x, y=$y")
        }
    }

    /**
     * Save position synchronously (blocks until saved to disk)
     * IMPORTANT: Saves EXACT position, NO constraining!
     * Use this during cleanup or when service is being destroyed.
     * WindowManager already handles bounds, so we save what user dragged to.
     *
     * @param x X coordinate (saved as-is)
     * @param y Y coordinate (saved as-is)
     */
    suspend fun savePositionSync(x: Int, y: Int) {
        android.util.Log.d("DICTO_FLOATING", ">>> PositionPersistence.savePositionSync() sync START: x=$x, y=$y (NO constraint)")
        preferencesManager.setFloatingButtonPosition(x, y)
        android.util.Log.d("DICTO_FLOATING", ">>> PositionPersistence.savePositionSync() sync COMPLETE: x=$x, y=$y")
    }

    /**
     * Constrain button position to remain within visible screen bounds
     * Prevents button from being positioned off-screen
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @return Pair of (constrainedX, constrainedY)
     */
    fun constrainPositionToBounds(x: Int, y: Int): Pair<Int, Int> {
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        // Ensure button stays within screen bounds
        val minX = -BUTTON_SIZE / 2  // Allow 50% off-screen on left
        val maxX = screenWidth - BUTTON_SIZE / 2  // Allow 50% off-screen on right
        val minY = 0  // Keep at or below top
        val maxY = screenHeight - BUTTON_SIZE  // Keep within bottom

        val constrainedX = x.coerceIn(minX, maxX)
        val constrainedY = y.coerceIn(minY, maxY)

        return Pair(constrainedX, constrainedY)
    }

    /**
     * Check if position was constrained (i.e., was invalid)
     * @param originalX Original X coordinate
     * @param constrainedX Constrained X coordinate
     * @param originalY Original Y coordinate
     * @param constrainedY Constrained Y coordinate
     * @return true if position was out of bounds and had to be constrained
     */
    fun wasPositionConstrained(
        originalX: Int,
        constrainedX: Int,
        originalY: Int,
        constrainedY: Int
    ): Boolean {
        return originalX != constrainedX || originalY != constrainedY
    }
}


