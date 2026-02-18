package com.example.dicto.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * PreferencesManager - Handles app preferences with DataStore
 *
 * Single Responsibility: Persist and retrieve user preferences
 * Features:
 * - Type-safe preference access
 * - Reactive preference updates
 * - Easy to extend for new preferences
 *
 * Currently manages:
 * - clipboardMonitoringEnabled: Auto-translate from clipboard setting
 * - floatingWindowEnabled: Floating translator toggle
 */

private val Context.preferencesDataStore by preferencesDataStore(name = "app_preferences")

class PreferencesManager(private val context: Context) {

    companion object {
        // Preference keys
        private val CLIPBOARD_MONITORING_KEY = booleanPreferencesKey("clipboard_monitoring_enabled")
        private val FLOATING_WINDOW_KEY = booleanPreferencesKey("floating_window_enabled")

        // Default values
        private const val DEFAULT_CLIPBOARD_MONITORING = true
        private const val DEFAULT_FLOATING_WINDOW = false
    }

    /**
     * Observable clipboard monitoring preference
     * Emits saved value on subscribe, then any changes
     */
    val clipboardMonitoringEnabled: Flow<Boolean> = context.preferencesDataStore.data
        .map { preferences ->
            preferences[CLIPBOARD_MONITORING_KEY] ?: DEFAULT_CLIPBOARD_MONITORING
        }

    /**
     * Observable floating window preference
     * Emits saved value on subscribe, then any changes
     */
    val floatingWindowEnabled: Flow<Boolean> = context.preferencesDataStore.data
        .map { preferences ->
            preferences[FLOATING_WINDOW_KEY] ?: DEFAULT_FLOATING_WINDOW
        }

    /**
     * Save clipboard monitoring preference
     *
     * @param enabled Whether clipboard monitoring should be enabled
     */
    suspend fun setClipboardMonitoringEnabled(enabled: Boolean) {
        context.preferencesDataStore.edit { preferences ->
            preferences[CLIPBOARD_MONITORING_KEY] = enabled
        }
    }

    /**
     * Save floating window preference
     *
     * @param enabled Whether floating window should be enabled
     */
    suspend fun setFloatingWindowEnabled(enabled: Boolean) {
        context.preferencesDataStore.edit { preferences ->
            preferences[FLOATING_WINDOW_KEY] = enabled
        }
    }
}

