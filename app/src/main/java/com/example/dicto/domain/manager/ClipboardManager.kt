package com.example.dicto.domain.manager

import android.util.Log
import com.example.dicto.data.local.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

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
) : IClipboardManager {
    /**
     * StateFlow of clipboard monitoring enabled state
     * Automatically updates when preference changes
     *
     * Uses Eagerly to ensure the flow stays in sync with preferences
     * so that toggle() reads the correct current value
     */
    override val isMonitoringEnabled: StateFlow<Boolean> = preferencesManager.clipboardMonitoringEnabled
        .stateIn(scope, SharingStarted.Eagerly, false)  // ✅ Eagerly keeps it in sync

    /**
     * StateFlow of floating window enabled state
     * Automatically updates when preference changes
     *
     * Uses Eagerly to ensure the flow stays in sync with preferences
     * so that toggle() reads the correct current value
     */
    override val isFloatingWindowEnabled: StateFlow<Boolean> = preferencesManager.floatingWindowEnabled
        .stateIn(scope, SharingStarted.Eagerly, false)

    /**
     * Toggle clipboard monitoring on/off and persist preference
     */
    override suspend fun toggleMonitoring() {
        val currentState = isMonitoringEnabled.value
        val newState = !currentState
        Log.d("ClipboardManager", "Toggling clipboard monitoring to: $newState")
        preferencesManager.setClipboardMonitoringEnabled(newState)
    }

    /**
     * Toggle floating window on/off and persist preference
     */
    override suspend fun toggleFloatingWindow() {
        val currentState = isFloatingWindowEnabled.value
        val newState = !currentState
        Log.d("ClipboardManager", "Toggling floating window to: $newState")
        preferencesManager.setFloatingWindowEnabled(newState)
    }

    /**
     * Set monitoring state explicitly
     */
    suspend fun setMonitoringEnabled(enabled: Boolean) {
        Log.d("ClipboardManager", "Setting clipboard monitoring to: $enabled")
        preferencesManager.setClipboardMonitoringEnabled(enabled)
    }

    /**
     * Set floating window state explicitly
     */
    suspend fun setFloatingWindowEnabled(enabled: Boolean) {
        Log.d("ClipboardManager", "Setting floating window to: $enabled")
        preferencesManager.setFloatingWindowEnabled(enabled)
    }

    /**
     * Check if monitoring is currently enabled
     */
    fun isMonitoringActive(): Boolean = isMonitoringEnabled.value

    /**
     * Check if floating window is currently enabled
     */
    fun isFloatingWindowActive(): Boolean = isFloatingWindowEnabled.value
}