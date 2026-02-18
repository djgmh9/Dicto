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
 */

private val Context.preferencesDataStore by preferencesDataStore(name = "app_preferences")

class PreferencesManager(private val context: Context) {

    companion object {
        // Preference keys
        private val CLIPBOARD_MONITORING_KEY = booleanPreferencesKey("clipboard_monitoring_enabled")

        // Default values
        private const val DEFAULT_CLIPBOARD_MONITORING = true
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
     * Save clipboard monitoring preference
     *
     * @param enabled Whether clipboard monitoring should be enabled
     */
    suspend fun setClipboardMonitoringEnabled(enabled: Boolean) {
        context.preferencesDataStore.edit { preferences ->
            preferences[CLIPBOARD_MONITORING_KEY] = enabled
        }
    }
}

