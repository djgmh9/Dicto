package com.example.dicto.domain.manager

import kotlinx.coroutines.flow.StateFlow

/**
 * IClipboardManager - Interface for clipboard monitoring preferences
 *
 * This interface defines the contract for clipboard management,
 * allowing for different implementations (real, fake for testing, etc.)
 */
interface IClipboardManager {
    /**
     * StateFlow of clipboard monitoring enabled state
     */
    val isMonitoringEnabled: StateFlow<Boolean>

    /**
     * StateFlow of floating window enabled state
     */
    val isFloatingWindowEnabled: StateFlow<Boolean>

    /**
     * Toggle clipboard monitoring on/off and persist preference
     */
    suspend fun toggleMonitoring()

    /**
     * Toggle floating window on/off and persist preference
     */
    suspend fun toggleFloatingWindow()
}

