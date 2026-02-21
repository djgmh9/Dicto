package com.example.dicto.fakes

import com.example.dicto.data.local.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Fake implementation of PreferencesManager for testing
 */
class FakePreferencesManager : PreferencesManager {
    private val _clipboardMonitoringEnabled = MutableStateFlow(false)
    override val clipboardMonitoringEnabled: StateFlow<Boolean> = _clipboardMonitoringEnabled

    private val _floatingWindowEnabled = MutableStateFlow(false)
    override val floatingWindowEnabled: StateFlow<Boolean> = _floatingWindowEnabled

    private val _floatingButtonX = MutableStateFlow(0)
    override val floatingButtonX: StateFlow<Int> = _floatingButtonX

    private val _floatingButtonY = MutableStateFlow(0)
    override val floatingButtonY: StateFlow<Int> = _floatingButtonY

    override suspend fun setClipboardMonitoringEnabled(enabled: Boolean) {
        _clipboardMonitoringEnabled.value = enabled
    }

    override suspend fun setFloatingWindowEnabled(enabled: Boolean) {
        _floatingWindowEnabled.value = enabled
    }

    override suspend fun setFloatingButtonPosition(x: Int, y: Int) {
        _floatingButtonX.value = x
        _floatingButtonY.value = y
    }

    // Helper functions for testing specific values
    fun getClipboardMonitoringEnabledValue(): Boolean = _clipboardMonitoringEnabled.value
    fun getFloatingWindowEnabledValue(): Boolean = _floatingWindowEnabled.value
    fun getFloatingButtonXValue(): Int = _floatingButtonX.value
    fun getFloatingButtonYValue(): Int = _floatingButtonY.value
}
