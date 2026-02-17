package com.example.dicto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

// 1. Add a data class to hold individual word results
data class WordResult(
    val original: String,
    val translation: String
)

// Represents the different states of our screen
sealed interface DictionaryUiState {
    data object Idle : DictionaryUiState
    data object Loading : DictionaryUiState
    data class Error(val message: String) : DictionaryUiState

    // UPDATED: Now holds full sentence + list of words
    data class Success(
        val fullTranslation: String,
        val wordTranslations: List<WordResult>
    ) : DictionaryUiState
}

class DictionaryViewModel : ViewModel() {

    private val repository = TranslationRepository()

    // Backing property to avoid external modification
    private val _uiState = MutableStateFlow<DictionaryUiState>(DictionaryUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private var currentQuery = ""

    fun onQueryChanged(newQuery: String) {
        currentQuery = newQuery
        if (newQuery.isBlank()) {
            _uiState.value = DictionaryUiState.Idle
            return
        }

        // Optimisation: You might want to "Debounce" here (wait for user to stop typing)
        // For simplicity, we translate on button press in the UI
    }

    fun translate() {
        if (currentQuery.isBlank()) return

        _uiState.value = DictionaryUiState.Loading

        viewModelScope.launch {
            // 1. Translate the full sentence
            val fullResult = repository.translateText(currentQuery)

            // 2. Split sentence into words (removing punctuation like . , ! ?)
            // Regex "\\W+" splits by anything that isn't a word character.
            // Remove duplicates
            val words = currentQuery.trim()
                .split(Regex("\\W+"))             // Split by non-word characters
                .filter { it.isNotEmpty() }       // Remove empty strings
                .distinctBy { it.lowercase() }    // Removes duplicates (case-insensitive)

            // 3. Translate each word in PARALLEL using async/awaitAll
            // This is much faster than doing them one by one!
            val wordTasks = words.map { word ->
                async {
                    val translation = repository.translateText(word).getOrDefault("")
                    WordResult(original = word, translation = translation)
                }
            }
            val wordResults = wordTasks.awaitAll()

            // 4. Handle the result
            fullResult.onSuccess { translatedSentence ->
                _uiState.value = DictionaryUiState.Success(
                    fullTranslation = translatedSentence,
                    wordTranslations = wordResults
                )
            }.onFailure { error ->
                _uiState.value = DictionaryUiState.Error(error.localizedMessage ?: "Unknown error")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.close() // Cleanup memory
    }
}
