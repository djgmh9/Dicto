package com.example.dicto.domain.model

/**
 * DictionaryUiState - Represents the different states of the dictionary screen
 *
 * Sealed interface for type-safe state management
 * Used to display loading, success, error, or idle states in the UI
 */
sealed interface DictionaryUiState {
    /**
     * Idle - No active translation
     */
    data object Idle : DictionaryUiState

    /**
     * Loading - Translation in progress
     */
    data object Loading : DictionaryUiState

    /**
     * Error - Translation failed
     * @property message Error message to display
     */
    data class Error(val message: String) : DictionaryUiState

    /**
     * Success - Translation completed successfully
     * @property fullTranslation Complete sentence translation
     * @property wordTranslations Individual word-by-word translations
     */
    data class Success(
        val fullTranslation: String,
        val wordTranslations: List<WordResult>
    ) : DictionaryUiState
}

