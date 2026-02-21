package com.example.dicto.fakes

import com.example.dicto.domain.manager.IClipboardManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Fake implementation of IClipboardManager for testing
 * This is a standalone fake that mimics the interface
 */
class FakeClipboardManager : IClipboardManager {
    private val _isMonitoringEnabled = MutableStateFlow(false) // Default to false like production
    override val isMonitoringEnabled: StateFlow<Boolean> = _isMonitoringEnabled

    private val _isFloatingWindowEnabled = MutableStateFlow(false)
    override val isFloatingWindowEnabled: StateFlow<Boolean> = _isFloatingWindowEnabled

    override suspend fun toggleMonitoring() {
        _isMonitoringEnabled.value = !_isMonitoringEnabled.value
    }

    override suspend fun toggleFloatingWindow() {
        _isFloatingWindowEnabled.value = !_isFloatingWindowEnabled.value
    }

    fun setMonitoringEnabled(enabled: Boolean) {
        _isMonitoringEnabled.value = enabled
    }

    fun setFloatingWindowEnabled(enabled: Boolean) {
        _isFloatingWindowEnabled.value = enabled
    }

    fun getMonitoringEnabled(): Boolean = _isMonitoringEnabled.value

    fun getFloatingWindowEnabled(): Boolean = _isFloatingWindowEnabled.value
}

