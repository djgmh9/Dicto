package com.example.dicto.presentation.screens.saved

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dicto.data.local.WordStorage
import com.example.dicto.domain.manager.PronunciationManager
import com.example.dicto.domain.model.WordResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import com.example.dicto.data.repository.TranslationRepository
import com.example.dicto.domain.manager.TranslationManager

/**
 * SavedWordsViewModel - Handles saved words list and search
 *
 * Single Responsibility: Saved words management and filtering
 * Features:
 * - Display saved words
 * - Search/filter saved words
 * - Delete words
 * - Pronunciation
 *
 * Delegates to:
 * - WordStorage: Persistence
 * - TranslationManager: Translation of words
 * - PronunciationManager: TTS
 */
class SavedWordsViewModel(application: Application) : AndroidViewModel(application) {

    private val storage = WordStorage(application)
    private val translationManager = TranslationManager(TranslationRepository())
    private val pronunciationManager = PronunciationManager(application, viewModelScope)

    // ==================== SEARCH ====================
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // ==================== SAVED WORDS LIST ====================
    @OptIn(ExperimentalCoroutinesApi::class)
    val savedWordsList: StateFlow<List<WordResult>> = storage.savedWordsFlow
        .flatMapLatest { savedSet ->
            flow {
                if (savedSet.isEmpty()) {
                    emit(emptyList())
                } else {
                    val wordResults = savedSet.map { word ->
                        val translation = translationManager.translateSentence(word)
                            .getOrDefault("")
                        WordResult(word, translation, isSaved = true)
                    }
                    emit(wordResults.reversed())
                }
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

