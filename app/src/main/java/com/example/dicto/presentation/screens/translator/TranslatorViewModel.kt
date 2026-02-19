package com.example.dicto.presentation.screens.translator

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dicto.data.local.WordStorage
import com.example.dicto.data.local.PreferencesManager
import com.example.dicto.data.repository.TranslationRepository
import com.example.dicto.domain.PronunciationManager
import com.example.dicto.domain.TranslationManager
import com.example.dicto.domain.model.DictionaryUiState
import com.example.dicto.domain.model.WordResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

/**
 * TranslatorViewModel - Handles translation-specific UI state
 *
 * Single Responsibility: Translation and phrase building logic
 * Features:
 * - Text translation with debouncing
 * - Phrase builder state management
 * - Word-by-word translation
 * - Save/unsave words
 * - Pronunciation for original and translation
 *
 * Delegates to:
 * - TranslationManager: Translation business logic
 * - PronunciationManager: TTS
 * - WordStorage: Word persistence
 */
class TranslatorViewModel(application: Application) : AndroidViewModel(application) {

    private val translationManager = TranslationManager(TranslationRepository())
    private val pronunciationManager = PronunciationManager(application, viewModelScope)
    private val storage = WordStorage(application)

    // ==================== SEARCH INPUT ====================
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // ==================== TRANSLATION STATE ====================
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DictionaryUiState> = _searchQuery
        .debounce(600L)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf<DictionaryUiState>(DictionaryUiState.Idle)
            } else {
                flow<DictionaryUiState> {
                    emit(DictionaryUiState.Loading)
                    try {
                        val fullTranslation = translationManager.translateSentence(query).getOrDefault("")
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
        .combine(storage.savedWordsFlow) { state, savedSet ->
            if (state is DictionaryUiState.Success) {
                val updatedWords = state.wordTranslations.map { word ->
                    word.copy(isSaved = savedSet.contains(word.original))
                }
                state.copy(wordTranslations = updatedWords)
            } else {
                state
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DictionaryUiState.Idle)

    // ==================== PHRASE BUILDER ====================
    private val _selectedPhrase = MutableStateFlow("")
    val selectedPhrase: StateFlow<String> = _selectedPhrase.asStateFlow()

    private val _phraseTranslation = MutableStateFlow<String?>(null)
    val phraseTranslation: StateFlow<String?> = _phraseTranslation.asStateFlow()

    // ==================== USER INTERACTIONS ====================

    fun onQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun toggleSave(word: String) {
        viewModelScope.launch {
            storage.toggleWord(word)
        }
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

    // ==================== PRONUNCIATION ====================

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

    // ==================== CLEANUP ====================

    override fun onCleared() {
        super.onCleared()
        translationManager.close()
        pronunciationManager.shutdown()
    }
}

