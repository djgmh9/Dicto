package com.example.dicto.domain

import android.util.Log
import com.example.dicto.utils.PreferencesManager
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

/**
 * ClipboardManager - Handles clipboard monitoring preferences
 *
 * Single Responsibility: Manage clipboard monitoring state and preferences
 * Features:
 * - Get clipboard monitoring state
 * - Toggle clipboard monitoring on/off
 * - Persist preference to storage
 *
 * Separated from ViewModel for:
 * - Easier testing
 * - Reusability
 * - Clean preference management
 */
class ClipboardManager(
    private val preferencesManager: PreferencesManager,
    private val scope: CoroutineScope
) {
    /**
     * StateFlow of clipboard monitoring enabled state
     * Automatically updates when preference changes
     */
    val isMonitoringEnabled: StateFlow<Boolean> = preferencesManager.clipboardMonitoringEnabled
        .stateIn(scope, kotlinx.coroutines.flow.SharingStarted.Lazily, true)

    /**
     * Toggle clipboard monitoring on/off and persist preference
     */
    suspend fun toggleMonitoring() {
        val currentState = isMonitoringEnabled.value
        val newState = !currentState
        Log.d("ClipboardManager", "Toggling clipboard monitoring to: $newState")
        preferencesManager.setClipboardMonitoringEnabled(newState)
    }

    /**
     * Set monitoring state explicitly
     */
    suspend fun setMonitoringEnabled(enabled: Boolean) {
        Log.d("ClipboardManager", "Setting clipboard monitoring to: $enabled")
        preferencesManager.setClipboardMonitoringEnabled(enabled)
    }

    /**
     * Check if monitoring is currently enabled
     */
    fun isEnabled(): Boolean = isMonitoringEnabled.value
}



