package com.example.dicto.presentation.screens.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dicto.data.local.WordStorage
import com.example.dicto.domain.manager.PronunciationManager
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * SavedWordsViewModel - Handles saved words list and search
 *
 * Single Responsibility: Saved words management and filtering
 * Features:
 * - Display saved words with error/loading states
 * - Search/filter saved words
 * - Delete words
 * - Pronunciation
 *
 * Delegates to:
 * - WordStorage: Persistence
 * - TranslationManager: Translation of words
 * - PronunciationManager: TTS
 */
@HiltViewModel
class SavedWordsViewModel @Inject constructor(
    private val storage: WordStorage,
    private val translationManager: TranslationManager,
    private val pronunciationManager: PronunciationManager
) : ViewModel() {

    // ==================== SEARCH ====================
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // ==================== SAVED WORDS LIST WITH ERROR/LOADING STATES ====================
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DictionaryUiState> = storage.savedWordsFlow
        .flatMapLatest { savedSet ->
            flow {
                emit(DictionaryUiState.Loading)
                try {
                    if (savedSet.isEmpty()) {
                        emit(DictionaryUiState.Success("", emptyList()))
                    } else {
                        val wordResults = savedSet.map { word ->
                            val translation = translationManager.translateSentence(word)
                                .getOrDefault("")
                            WordResult(word, translation, isSaved = true)
                        }
                        emit(DictionaryUiState.Success("", wordResults.reversed()))
                    }
                } catch (e: Exception) {
                    emit(DictionaryUiState.Error(e.message ?: "Unknown error loading saved words"))
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DictionaryUiState.Loading)

    // ==================== BACKWARD COMPATIBILITY ====================
    // Keep the old savedWordsList property for backward compatibility
    val savedWordsList: StateFlow<List<WordResult>> = uiState
        .map { state ->
            when (state) {
                is DictionaryUiState.Success -> state.wordTranslations
                else -> emptyList()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ==================== FILTERED WORDS ====================
    @OptIn(FlowPreview::class)
    val filteredWords: StateFlow<List<WordResult>> = combine(
        savedWordsList,
        _searchQuery
    ) { words, query ->
        if (query.isEmpty()) {
            words
        } else {
            words.filter { word ->
                word.original.contains(query, ignoreCase = true) ||
                word.translation.contains(query, ignoreCase = true)
            }
        }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ==================== USER INTERACTIONS ====================

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onDeleteWord(original: String) {
        viewModelScope.launch {
            storage.toggleWord(original)
        }
    }

    // ==================== PRONUNCIATION ====================

    fun onPlayPronunciation(text: String) {
        pronunciationManager.speakArabic(text)
    }

    fun onStopPronunciation() {
        pronunciationManager.stop()
    }

    // ==================== CLEANUP ====================

    override fun onCleared() {
        super.onCleared()
        translationManager.close()
        pronunciationManager.shutdown()
    }
}

