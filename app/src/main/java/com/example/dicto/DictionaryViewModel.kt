package com.example.dicto

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

// 1. Add a data class to hold individual word results
data class WordResult(
    val original: String,
    val translation: String,
    val isSaved: Boolean = false
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

// CHANGE: Inherit from AndroidViewModel to get 'application' context
class DictionaryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TranslationRepository()
    private val storage = WordStorage(application) // Initialize storage

    // 1. A state to hold the list of saved words + their translations
    private val _savedWordsList = MutableStateFlow<List<WordResult>>(emptyList())
    val savedWordsList = _savedWordsList.asStateFlow()

    init {
        // 2. Watch the DataStore for changes automatically
        viewModelScope.launch {
            storage.savedWordsFlow.collect { savedSet ->
                // When the set changes, translate all of them
                // (In a real pro app, you might cache these translations in a Room database,
                // but for ML Kit on-device, this is acceptable and simple)
                val formattedList = savedSet.map { arabicWord ->
                    async {
                        val translation = repository.translateText(arabicWord).getOrDefault("...")
                        WordResult(arabicWord, translation, isSaved = true)
                    }
                }.awaitAll() // Wait for all to finish

                _savedWordsList.value = formattedList.sortedBy { it.original } // Sort A-Z
            }
        }
    }

    // We keep the raw translation separate from the UI state now
    private val _rawTranslation = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    private val _fullSentence = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    // MERGE LOGIC: Combine Raw Translation + Saved Words -> Final UI State
    val uiState: StateFlow<DictionaryUiState> = combine(
        _isLoading,
        _error,
        _fullSentence,
        _rawTranslation,
        storage.savedWordsFlow
    ) { isLoading, error, fullSentence, rawWords, savedSet ->

        if (isLoading) {
            DictionaryUiState.Loading
        } else if (error != null) {
            DictionaryUiState.Error(error)
        } else if (rawWords.isEmpty()) {
            DictionaryUiState.Idle
        } else {
            // Check which words are in the savedSet
            val finalWords = rawWords.map { (original, translation) ->
                WordResult(
                    original = original,
                    translation = translation,
                    isSaved = savedSet.contains(original) // Check if saved
                )
            }
            DictionaryUiState.Success(fullSentence, finalWords)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DictionaryUiState.Idle)

    private var currentQuery = ""

    fun onQueryChanged(newQuery: String) {
        currentQuery = newQuery
    }

    fun toggleSave(word: String) {
        viewModelScope.launch {
            storage.toggleWord(word)
        }
    }

    fun translate() {
        if (currentQuery.isBlank()) return
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            val fullResult = repository.translateText(currentQuery)

            // Regex for Arabic/Unicode letters
            val uniqueWords = currentQuery.trim()
                .split(Regex("[^\\p{L}]+"))
                .filter { it.isNotEmpty() }
                .distinctBy { it.lowercase() }

            val wordTasks = uniqueWords.map { word ->
                async {
                    val translation = repository.translateText(word).getOrDefault("")
                    word to translation // Return a simple Pair
                }
            }

            val wordResults = wordTasks.awaitAll()

            fullResult.onSuccess { translatedSentence ->
                _fullSentence.value = translatedSentence
                _rawTranslation.value = wordResults
                _isLoading.value = false
            }.onFailure { error ->
                _error.value = error.localizedMessage
                _isLoading.value = false
            }
        }
    }

    // 1. State for the Phrase Builder
    private val _selectedPhrase = MutableStateFlow("")
    val selectedPhrase = _selectedPhrase.asStateFlow()

    private val _phraseTranslation = MutableStateFlow<String?>(null)
    val phraseTranslation = _phraseTranslation.asStateFlow()

    // 2. Function to update selection and translate immediately
    fun onPhraseSelectionChanged(selectedWords: List<String>) {
        if (selectedWords.isEmpty()) {
            _selectedPhrase.value = ""
            _phraseTranslation.value = null
            return
        }

        // Join the words with a space (e.g., "Hot" + "Dog" -> "Hot Dog")
        val combinedPhrase = selectedWords.joinToString(" ")
        _selectedPhrase.value = combinedPhrase

        // Translate this specific chunk
        viewModelScope.launch {
            val result = repository.translateText(combinedPhrase)
            result.onSuccess {
                _phraseTranslation.value = it
            }.onFailure {
                _phraseTranslation.value = "Error"
            }
        }
    }

    // Cleanup
    override fun onCleared() {
        super.onCleared()
        repository.close()
    }
}
