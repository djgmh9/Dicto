package com.example.dicto.presentation.screens.translator

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dicto.data.local.WordStorage
import com.example.dicto.domain.manager.IPronunciationManager
import com.example.dicto.domain.manager.TranslationManager
import com.example.dicto.domain.model.DictionaryUiState
import com.example.dicto.domain.model.WordResult
import dagger.hilt.android.lifecycle.HiltViewModel
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
import javax.inject.Inject

private const val TAG = "TranslatorVM"

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
@HiltViewModel
class TranslatorViewModel @Inject constructor(
    private val translationManager: TranslationManager,
    private val pronunciationManager: IPronunciationManager,
    private val storage: WordStorage
) : ViewModel() {

    // ==================== SEARCH INPUT ====================
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // ==================== TRANSLATION STATE ====================
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DictionaryUiState> = _searchQuery
        .debounce(600L)
        .flatMapLatest { query ->
            Log.d(TAG, "[TRANSLATION] flatMapLatest triggered with query: '$query'")
            if (query.isBlank()) {
                Log.d(TAG, "[TRANSLATION] Query is blank, emitting Idle")
                flowOf<DictionaryUiState>(DictionaryUiState.Idle)
            } else {
                flow<DictionaryUiState> {
                    Log.d(TAG, "[TRANSLATION] Starting translation for: '$query'")
                    emit(DictionaryUiState.Loading)
                    try {
                        Log.d(TAG, "[TRANSLATION] Calling translateSentence for: '$query'")
                        val fullTranslation = translationManager.translateSentence(query).getOrDefault("")
                        Log.d(TAG, "[TRANSLATION] Got fullTranslation: '$fullTranslation'")

                        Log.d(TAG, "[TRANSLATION] Calling translateWords for: '$query'")
                        val wordResults = translationManager.translateWords(query)
                            .getOrDefault(emptyList())
                            .map { WordResult(it.original, it.translation, isSaved = false) }
                        Log.d(TAG, "[TRANSLATION] Got ${wordResults.size} word results")

                        emit(DictionaryUiState.Success(fullTranslation, wordResults))
                        Log.d(TAG, "[TRANSLATION] Emitted Success state")
                    } catch (e: Exception) {
                        Log.e(TAG, "[TRANSLATION] Error during translation: ${e.message}", e)
                        emit(DictionaryUiState.Error(e.localizedMessage ?: "Unknown error"))
                    }
                }
            }
        }
        .combine(storage.savedWordsFlow) { state, savedSet ->
            Log.d(TAG, "[TRANSLATION_COMBINE] Combining state=$state with savedSet size=${savedSet.size}")
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

    // ==================== SAVED WORDS SET ====================
    val savedWordsSet: StateFlow<Set<String>> = storage.savedWordsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // ==================== PHRASE BUILDER ====================
    private val _selectedPhrase = MutableStateFlow("")
    val selectedPhrase: StateFlow<String> = _selectedPhrase.asStateFlow()

    private val _phraseTranslation = MutableStateFlow<String?>(null)
    val phraseTranslation: StateFlow<String?> = _phraseTranslation.asStateFlow()

    // ==================== USER INTERACTIONS ====================

    fun onQueryChanged(newQuery: String) {
        Log.d(TAG, "[USER_INPUT] onQueryChanged called: oldQuery='${_searchQuery.value}' newQuery='$newQuery'")
        _searchQuery.value = newQuery
    }

    fun onClipboardTextFound(text: String) {
        Log.d(TAG, "[CLIPBOARD] onClipboardTextFound: text='$text' currentQuery='${_searchQuery.value}'")
        if (text.isNotBlank() && text != _searchQuery.value) {
            Log.d(TAG, "[CLIPBOARD] Setting query to clipboard text")
            _searchQuery.value = text
        } else {
            Log.d(TAG, "[CLIPBOARD] Skipped clipboard text - blank or duplicate")
        }
    }

    fun toggleSave(word: String) {
        Log.d(TAG, "[SAVE_WORD] toggleSave called for: '$word'")
        viewModelScope.launch {
            storage.toggleWord(word)
        }
    }

    fun onPhraseSelectionChanged(selectedWords: List<String>) {
        Log.d(TAG, "[PHRASE_BUILDER] onPhraseSelectionChanged with ${selectedWords.size} words: $selectedWords")
        if (selectedWords.isEmpty()) {
            _selectedPhrase.value = ""
            _phraseTranslation.value = null
            return
        }
        val combinedPhrase = selectedWords.joinToString(" ")
        _selectedPhrase.value = combinedPhrase
        Log.d(TAG, "[PHRASE_BUILDER] Phrase combined: '$combinedPhrase'")

        viewModelScope.launch {
            Log.d(TAG, "[PHRASE_BUILDER] Calling translatePhrase for: '$combinedPhrase'")
            val result = translationManager.translatePhrase(selectedWords)
            _phraseTranslation.value = result.getOrDefault(null)
            Log.d(TAG, "[PHRASE_BUILDER] Got phrase translation: '${_phraseTranslation.value}'")
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
        // NOTE: Do NOT close translationManager or repository here!
        // The TranslationRepository is a singleton that persists across ViewModels.
        // Closing the ML Kit translator here causes it to be unusable on next ViewModel creation.
        // This was causing "Translator has been closed" errors on app reopen.
        pronunciationManager.shutdown()
    }
}

