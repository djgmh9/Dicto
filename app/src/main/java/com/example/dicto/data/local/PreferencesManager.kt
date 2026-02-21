package com.example.dicto.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * PreferencesManager - Interface for app preferences with DataStore
 *
 * This interface defines the contract for preference management, allowing for
 * different implementations (e.g., in-memory for testing, DataStore for production).
 */
interface PreferencesManager {
    val clipboardMonitoringEnabled: Flow<Boolean>
    val floatingWindowEnabled: Flow<Boolean>
    val floatingButtonX: Flow<Int>
    val floatingButtonY: Flow<Int>

    suspend fun setClipboardMonitoringEnabled(enabled: Boolean)
    suspend fun setFloatingWindowEnabled(enabled: Boolean)
    suspend fun setFloatingButtonPosition(x: Int, y: Int)
}

/**
 * DefaultPreferencesManager - Concrete implementation of PreferencesManager using DataStore
 */
private val Context.preferencesDataStore by preferencesDataStore(name = "app_preferences")

class DefaultPreferencesManager(
    private val context: Context,
    private val dataStore: DataStore<Preferences>? = null
) : PreferencesManager {
    // Use provided dataStore (for tests) or the singleton from context
    private val internalDataStore: DataStore<Preferences>
        get() = dataStore ?: context.preferencesDataStore

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
    override val clipboardMonitoringEnabled: Flow<Boolean> = internalDataStore.data
        .map { preferences ->
            preferences[CLIPBOARD_MONITORING_KEY] ?: DEFAULT_CLIPBOARD_MONITORING
        }

    /**
     * Observable floating window preference
     * Emits saved value on subscribe, then any changes
     */
    override val floatingWindowEnabled: Flow<Boolean> = internalDataStore.data
        .map { preferences ->
            preferences[FLOATING_WINDOW_KEY] ?: DEFAULT_FLOATING_WINDOW
        }

    /**
     * Observable floating button X position
     */
    override val floatingButtonX: Flow<Int> = internalDataStore.data
        .map { preferences ->
            preferences[FLOATING_BUTTON_X_KEY] ?: DEFAULT_BUTTON_X
        }

    /**
     * Observable floating button Y position
     */
    override val floatingButtonY: Flow<Int> = internalDataStore.data
        .map { preferences ->
            preferences[FLOATING_BUTTON_Y_KEY] ?: DEFAULT_BUTTON_Y
        }

    /**
     * Save clipboard monitoring preference
     *
     * @param enabled Whether clipboard monitoring should be enabled
     */
    override suspend fun setClipboardMonitoringEnabled(enabled: Boolean) {
        internalDataStore.edit { preferences ->
            preferences[CLIPBOARD_MONITORING_KEY] = enabled
        }
    }

    /**
     * Save floating window preference
     *
     * @param enabled Whether floating window should be enabled
     */
    override suspend fun setFloatingWindowEnabled(enabled: Boolean) {
        internalDataStore.edit { preferences ->
            preferences[FLOATING_WINDOW_KEY] = enabled
        }
    }

    /**
     * Save floating button position
     *
     * @param x X coordinate of button
     * @param y Y coordinate of button
     */
    override suspend fun setFloatingButtonPosition(x: Int, y: Int) {
        internalDataStore.edit { preferences ->
            preferences[FLOATING_BUTTON_X_KEY] = x
            preferences[FLOATING_BUTTON_Y_KEY] = y
        }
    }
}
