package com.example.dicto.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dicto.data.local.PreferencesManager
import com.example.dicto.domain.manager.ClipboardManager
import com.example.dicto.domain.manager.FloatingWindowManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * SettingsViewModel - Handles app settings and preferences
 *
 * Single Responsibility: Settings management and toggling
 * Features:
 * - Clipboard monitoring toggle
 * - Floating window toggle
 * - Language preferences
 * - Permission checking
 *
 * Delegates to:
 * - ClipboardManager: Clipboard preferences
 * - FloatingWindowManager: Floating window coordination
 * - PreferencesManager: Preferences persistence
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val clipboardManager: ClipboardManager,
    private val floatingWindowManager: FloatingWindowManager
) : ViewModel() {

    // ==================== SETTINGS STATE ====================

    val clipboardMonitoringEnabled: StateFlow<Boolean> = clipboardManager.isMonitoringEnabled
    val floatingWindowEnabled: StateFlow<Boolean> = clipboardManager.isFloatingWindowEnabled

    // ==================== USER INTERACTIONS ====================

    fun toggleClipboardMonitoring() {
        viewModelScope.launch {
            clipboardManager.toggleMonitoring()
        }
    }

    fun toggleFloatingWindow() {
        viewModelScope.launch {
            clipboardManager.toggleFloatingWindow()
        }
    }

    fun isFloatingWindowPermissionGranted(): Boolean {
        return floatingWindowManager.isPermissionGranted()
    }

    fun startFloatingWindow() {
        floatingWindowManager.startFloatingWindow()
    }

    fun stopFloatingWindow() {
        floatingWindowManager.stopFloatingWindow()
    }
}

