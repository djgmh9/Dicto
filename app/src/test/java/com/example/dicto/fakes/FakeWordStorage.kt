package com.example.dicto.fakes

import com.example.dicto.data.local.WordStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Fake implementation of WordStorage for testing
 * Stores words in memory without database
 */
class FakeWordStorage : WordStorage {
    private val _savedWordsFlow = MutableStateFlow<Set<String>>(emptySet())
    override val savedWordsFlow: StateFlow<Set<String>> = _savedWordsFlow

    // These methods are specific to the Fake for testing internal behavior
    // and are not part of the WordStorage interface.
    fun getSavedWords(): Set<String> = _savedWordsFlow.value

    // Simplified save/remove for the fake
    suspend fun save(word: String) {
        _savedWordsFlow.value = _savedWordsFlow.value + word
    }

    suspend fun remove(word: String) {
        _savedWordsFlow.value = _savedWordsFlow.value - word
    }

    override suspend fun toggleWord(word: String) {
        val current = _savedWordsFlow.value
        _savedWordsFlow.value = if (word in current) {
            current - word
        } else {
            current + word
        }
    }

    fun clear() {
        _savedWordsFlow.value = emptySet()
    }
}
