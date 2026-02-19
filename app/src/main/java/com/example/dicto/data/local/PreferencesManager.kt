package com.example.dicto.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * PreferencesManager - Handles app preferences with DataStore
 *
 * Data Layer Component
 * Single Responsibility: Persist and retrieve user preferences
 * Features:
 * - Type-safe preference access
 * - Reactive preference updates via Flow
 * - Easy to extend for new preferences
 *
 * Currently manages:
 * - clipboardMonitoringEnabled: Auto-translate from clipboard setting
 * - floatingWindowEnabled: Floating translator toggle
 * - floatingButtonX/Y: Button position persistence
 */

private val Context.preferencesDataStore by preferencesDataStore(name = "app_preferences")

class PreferencesManager(private val context: Context) {

    companion object {
        // Preference keys
        private val CLIPBOARD_MONITORING_KEY = booleanPreferencesKey("clipboard_monitoring_enabled")
        private val FLOATING_WINDOW_KEY = booleanPreferencesKey("floating_window_enabled")
        private val FLOATING_BUTTON_X_KEY = intPreferencesKey("floating_button_x")
        private val FLOATING_BUTTON_Y_KEY = intPreferencesKey("floating_button_y")

        // Default values
        private const val DEFAULT_CLIPBOARD_MONITORING = true
        private const val DEFAULT_FLOATING_WINDOW = false
        private const val DEFAULT_BUTTON_X = 0
        private const val DEFAULT_BUTTON_Y = 100
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
     * Observable floating button X position
     */
    val floatingButtonX: Flow<Int> = context.preferencesDataStore.data
        .map { preferences ->
            preferences[FLOATING_BUTTON_X_KEY] ?: DEFAULT_BUTTON_X
        }

    /**
     * Observable floating button Y position
     */
    val floatingButtonY: Flow<Int> = context.preferencesDataStore.data
        .map { preferences ->
            preferences[FLOATING_BUTTON_Y_KEY] ?: DEFAULT_BUTTON_Y
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

    /**
     * Save floating button position
     *
     * @param x X coordinate of button
     * @param y Y coordinate of button
     */
    suspend fun setFloatingButtonPosition(x: Int, y: Int) {
        context.preferencesDataStore.edit { preferences ->
            preferences[FLOATING_BUTTON_X_KEY] = x
            preferences[FLOATING_BUTTON_Y_KEY] = y
        }
    }
}

