package com.example.dicto.presentation.screens

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dicto.data.local.WordStorage
import com.example.dicto.data.local.PreferencesManager
import com.example.dicto.data.repository.TranslationRepository
import com.example.dicto.domain.ClipboardManager
import com.example.dicto.domain.PronunciationManager
import com.example.dicto.domain.TranslationManager
import com.example.dicto.domain.model.DictionaryUiState
import com.example.dicto.domain.model.WordResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn


/**
 * DictionaryViewModel - Main view model for the dictionary app
 *
 * Responsibilities:
 * - Manage UI state
 * - Coordinate managers (Translation, Pronunciation, Clipboard)
 * - Expose state flows to UI
 * - Handle user interactions
 *
 * Separated concerns via managers:
 * - TranslationManager: All translation logic
 * - PronunciationManager: All TTS/pronunciation
 * - ClipboardManager: All clipboard preferences
 */
class DictionaryViewModel(application: Application) : AndroidViewModel(application) {

    // MANAGERS - Each handles one specific concern
    private val translationManager = TranslationManager(TranslationRepository())
    private val pronunciationManager = PronunciationManager(application, viewModelScope)
    private val clipboardManager = ClipboardManager(PreferencesManager(application), viewModelScope)
    private val storage = WordStorage(application)

    // 1. INPUT: The text the user types
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // 2. INTERNAL: Phrase building states
    private val _selectedPhrase = MutableStateFlow("")
    val selectedPhrase = _selectedPhrase.asStateFlow()
    private val _phraseTranslation = MutableStateFlow<String?>(null)
    val phraseTranslation = _phraseTranslation.asStateFlow()

    // 2.5. CLIPBOARD MONITORING: Expose clipboard manager's states
    val clipboardMonitoringEnabled: StateFlow<Boolean> = clipboardManager.isMonitoringEnabled
    val floatingWindowEnabled: StateFlow<Boolean> = clipboardManager.isFloatingWindowEnabled

    // 3. OUTPUT: The Reactive UI State Pipeline
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DictionaryUiState> = _searchQuery
        // A. DEBOUNCE: Wait 600ms after user stops typing
        .debounce(600L)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf<DictionaryUiState>(DictionaryUiState.Idle)
            } else {
                // Start loading immediately
                flow<DictionaryUiState> {
                    emit(DictionaryUiState.Loading)

                    // B. PERFORM TRANSLATION via TranslationManager
                    try {
                        val fullTranslation = translationManager.translateSentence(query)
                            .getOrDefault("")
                        val wordResults = translationManager.translateWords(query)
                            .getOrDefault(emptyList())
                            .map { WordResult(it.original, it.translation, isSaved = false) }

                        emit(DictionaryUiState.Success(fullTranslation, wordResults))
                    } catch (e: Exception) {
                        emit(DictionaryUiState.Error(e.localizedMessage ?: "Unknown error"))
                    }
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
                    // Translate all saved words via TranslationManager
                    val wordResults = savedSet.map { word ->
                        val translation = translationManager.translateSentence(word)
                            .getOrDefault("")
                        WordResult(word, translation, isSaved = true)
                    }
                    // Reverse to show last-added-first
                    emit(wordResults.reversed())
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- USER INTERACTION HANDLERS ---

    fun onQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onClipboardTextFound(text: String) {
        Log.d("DictionaryViewModel", "onClipboardTextFound called with: $text")
        Log.d("DictionaryViewModel", "Monitoring enabled: ${clipboardMonitoringEnabled.value}")

        if (clipboardMonitoringEnabled.value && text.isNotBlank() && text != _searchQuery.value) {
            Log.d("DictionaryViewModel", "Setting search query to clipboard text: $text")
            _searchQuery.value = text
        } else {
            Log.d("DictionaryViewModel", "Skipped clipboard text - conditions not met")
        }
    }

    fun toggleClipboardMonitoring() {
        viewModelScope.launch {
            clipboardManager.toggleMonitoring()
        }
    }

    fun toggleFloatingWindow() {
        viewModelScope.launch {
            clipboardManager.toggleFloatingWindow()
        }
    }

    fun toggleSave(word: String) {
        viewModelScope.launch { storage.toggleWord(word) }
    }

    fun onPhraseSelectionChanged(selectedWords: List<String>) {
        if (selectedWords.isEmpty()) {
            _selectedPhrase.value = ""
            _phraseTranslation.value = null
            return
        }
        val combinedPhrase = selectedWords.joinToString(" ")
        _selectedPhrase.value = combinedPhrase

        viewModelScope.launch {
            val result = translationManager.translatePhrase(selectedWords)
            _phraseTranslation.value = result.getOrDefault(null)
        }
    }

    // --- PRONUNCIATION METHODS (Delegated to PronunciationManager) ---

    fun pronounceOriginal(word: String) {
        pronunciationManager.speakArabic(word)
    }

    fun pronounceTranslation(translation: String) {
        pronunciationManager.speakEnglish(translation)
    }

    fun pronounceInputSentence() {
        val inputText = _searchQuery.value
        pronunciationManager.speakArabic(inputText)
    }

    fun stopPronunciation() {
        pronunciationManager.stop()
    }

    // --- CLEANUP ---

    override fun onCleared() {
        super.onCleared()
        translationManager.close()
        pronunciationManager.shutdown()
        Log.d("DictionaryViewModel", "ViewModel cleared and resources cleaned up")
    }
}

