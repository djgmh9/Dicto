package com.example.dicto.presentation.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dicto.data.local.PreferencesManager
import com.example.dicto.domain.manager.ClipboardManager
import com.example.dicto.domain.manager.FloatingWindowManager
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesManager = PreferencesManager(application)
    private val clipboardManager = ClipboardManager(preferencesManager, viewModelScope)
    private val floatingWindowManager = FloatingWindowManager(application)

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

