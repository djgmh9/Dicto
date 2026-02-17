package com.example.dicto

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
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

class DictionaryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TranslationRepository()
    private val storage = WordStorage(application)

    // 1. INPUT: The text the user types
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // 2. INTERNAL: Phrase building states
    private val _selectedPhrase = MutableStateFlow("")
    val selectedPhrase = _selectedPhrase.asStateFlow()
    private val _phraseTranslation = MutableStateFlow<String?>(null)
    val phraseTranslation = _phraseTranslation.asStateFlow()

    // 2.5. CLIPBOARD MONITORING: Control clipboard auto-translate
    private val _clipboardMonitoringEnabled = MutableStateFlow(true)
    val clipboardMonitoringEnabled = _clipboardMonitoringEnabled.asStateFlow()

    // 3. OUTPUT: The Reactive UI State Pipeline
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DictionaryUiState> = _searchQuery
        // A. DEBOUNCE: Wait 600ms after user stops typing to avoid spamming translation
        .debounce(600L)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf<DictionaryUiState>(DictionaryUiState.Idle)
            } else {
                // Start loading immediately
                flow<DictionaryUiState> {
                    emit(DictionaryUiState.Loading)

                    // B. PERFORM TRANSLATION
                    val result = performTranslation(query)
                    emit(result)
                }
            }
        }
        // C. MERGE WITH SAVED WORDS (So the stars update automatically)
        .combine(storage.savedWordsFlow) { state, savedSet ->
            if (state is DictionaryUiState.Success) {
                // Check which words are saved
                val updatedWords = state.wordTranslations.map { word ->
                    word.copy(isSaved = savedSet.contains(word.original))
                }
                state.copy(wordTranslations = updatedWords)
            } else {
                state
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DictionaryUiState.Idle)

    // 4. SAVED WORDS LIST: Expose saved words with their translations
    @OptIn(ExperimentalCoroutinesApi::class)
    val savedWordsList: StateFlow<List<WordResult>> = storage.savedWordsFlow
        .flatMapLatest { savedSet ->
            flow {
                if (savedSet.isEmpty()) {
                    emit(emptyList())
                } else {
                    // Translate all saved words
                    val wordResults = savedSet.map { word ->
                        viewModelScope.async {
                            val translation = repository.translateText(word).getOrDefault("")
                            WordResult(word, translation, isSaved = true)
                        }
                    }.awaitAll()
                    emit(wordResults)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- HELPER FUNCTIONS ---

    private suspend fun performTranslation(query: String): DictionaryUiState {
        return try {
            // 1. Translate Full Sentence
            val fullResult = repository.translateText(query).getOrDefault("")

            // 2. Split into words (Unicode aware)
            val uniqueWords = query.trim()
                .split(Regex("[^\\p{L}]+"))
                .filter { it.isNotEmpty() }
                .distinctBy { it.lowercase() }

            // 3. Translate words in parallel
            val wordResults = uniqueWords.map { word ->
                viewModelScope.async {
                    val translation = repository.translateText(word).getOrDefault("")
                    WordResult(word, translation, isSaved = false) // isSaved checked later
                }
            }.awaitAll()

            DictionaryUiState.Success(fullResult, wordResults)
        } catch (e: Exception) {
            DictionaryUiState.Error(e.localizedMessage ?: "Unknown error")
        }
    }

    // Called by UI when user types
    fun onQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    // Called by Clipboard Logic
    fun onClipboardTextFound(text: String) {
        Log.d("DictionaryViewModel", "onClipboardTextFound called with: $text")
        Log.d("DictionaryViewModel", "Monitoring enabled: ${_clipboardMonitoringEnabled.value}")
        Log.d("DictionaryViewModel", "Current search query: ${_searchQuery.value}")

        // Only process if monitoring is enabled
        if (_clipboardMonitoringEnabled.value && text.isNotBlank() && text != _searchQuery.value) {
            Log.d("DictionaryViewModel", "Setting search query to clipboard text: $text")
            _searchQuery.value = text
            // No need to call translate() manually, the flow handles it!
        } else {
            Log.d("DictionaryViewModel", "Skipped clipboard text - conditions not met")
        }
    }

    // Toggle clipboard monitoring on/off
    fun toggleClipboardMonitoring() {
        _clipboardMonitoringEnabled.value = !_clipboardMonitoringEnabled.value
    }

    // Called by Star Icon
    fun toggleSave(word: String) {
        viewModelScope.launch { storage.toggleWord(word) }
    }

    // Called by Phrase Builder
    fun onPhraseSelectionChanged(selectedWords: List<String>) {
        if (selectedWords.isEmpty()) {
            _selectedPhrase.value = ""
            _phraseTranslation.value = null
            return
        }
        val combinedPhrase = selectedWords.joinToString(" ")
        _selectedPhrase.value = combinedPhrase

        viewModelScope.launch {
            val result = repository.translateText(combinedPhrase)
            _phraseTranslation.value = result.getOrDefault(null)
        }
    }

    // Cleanup
    override fun onCleared() {
        super.onCleared()
        repository.close()
    }
}