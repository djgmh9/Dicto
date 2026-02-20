package com.example.dicto.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * WordStorage - Local storage for saved vocabulary words
 *
 * Data Layer Component
 * Responsibilities:
 * - Persist saved words using DataStore
 * - Provide reactive stream of saved words
 * - Toggle word save/unsave state
 *
 * Uses DataStore for:
 * - Type-safe persistent storage
 * - Coroutine-based async operations
 * - Flow-based reactive updates
 */

// Create the extension property for DataStore
private val Context.dataStore by preferencesDataStore(name = "saved_words_prefs")

class WordStorage(
    private val context: Context,
    private val dataStore: DataStore<Preferences>? = null
) {
    private val internalDataStore: DataStore<Preferences>
        get() = dataStore ?: context.dataStore

    // Define the key for saved words - using stringSet for compatibility
    private val SAVED_WORDS_KEY = stringSetPreferencesKey("saved_words")

    // Expose a "Flow" (a stream of data) that updates whenever the list changes
    // Returns words in last-added-first order
    val savedWordsFlow: Flow<Set<String>> = internalDataStore.data
        .map { preferences ->
            preferences[SAVED_WORDS_KEY] ?: emptySet()
        }

    // Function to toggle (Save/Unsave) a word
    suspend fun toggleWord(word: String) {
        internalDataStore.edit { preferences ->
            val currentWords = preferences[SAVED_WORDS_KEY] ?: emptySet()
            if (currentWords.contains(word)) {
                // Remove word if it exists
                preferences[SAVED_WORDS_KEY] = currentWords - word
            } else {
                // Add word if it doesn't exist
                preferences[SAVED_WORDS_KEY] = currentWords + word
            }
        }
    }
}
