package com.example.dicto.ui.floating

import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * FloatingButtonRestoreHelper - Handles restoring floating button when closing activities
 *
 * Single Responsibility: Provide consistent restore mechanism for floating button
 * Used by:
 * - FloatingTranslatorActivity (overlay)
 * - MainActivity (main app)
 *
 * Ensures floating button is restored correctly in all scenarios
 */
object FloatingButtonRestoreHelper {
    private const val TAG = "FloatingButtonRestore"
    private const val RESTORE_ACTION = "com.example.dicto.RESTORE_FLOATING_BUTTON"

    /**
     * Restore floating button by sending broadcast to FloatingWindowService
     * Call this from any Activity's onDestroy() to ensure button is restored
     *
     * @param context Context to send broadcast from
     * @param reason Debug reason for restoration (for logging)
     */
    fun restoreFloatingButton(context: Context, reason: String = "Activity destroyed") {
        Log.d(TAG, "Restoring floating button - Reason: $reason")
        try {
            val intent = Intent(RESTORE_ACTION)
            context.sendBroadcast(intent)
            Log.d(TAG, "Broadcast sent successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring floating button: ${e.message}", e)
        }
    }
}

