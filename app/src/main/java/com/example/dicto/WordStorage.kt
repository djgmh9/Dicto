package com.example.dicto

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 1. Create the extension property for DataStore
private val Context.dataStore by preferencesDataStore(name = "saved_words_prefs")

class WordStorage(private val context: Context) {

    // Define the key (like a filename inside the database)
    private val SAVED_WORDS_KEY = stringSetPreferencesKey("saved_words")

    // 2. Expose a "Flow" (a stream of data) that updates whenever the list changes
    val savedWordsFlow: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[SAVED_WORDS_KEY] ?: emptySet()
        }

    // 3. Function to toggle (Save/Unsave) a word
    suspend fun toggleWord(word: String) {
        context.dataStore.edit { preferences ->
            val currentWords = preferences[SAVED_WORDS_KEY] ?: emptySet()
            if (currentWords.contains(word)) {
                preferences[SAVED_WORDS_KEY] = currentWords - word // Remove
            } else {
                preferences[SAVED_WORDS_KEY] = currentWords + word // Add
            }
        }
    }
}