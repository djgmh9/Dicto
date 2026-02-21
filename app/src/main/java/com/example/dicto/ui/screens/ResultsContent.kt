package com.example.dicto.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dicto.presentation.screens.translator.TranslatorViewModel
import com.example.dicto.domain.model.DictionaryUiState
import com.example.dicto.ui.components.*

private const val TAG = "ResultsContent"

/**
 * ResultsContent - Displays translation results in a scrollable list
 *
 * Sub-components:
 * - Full translation header
 * - Phrase builder section
 * - Phrase result card
 * - Word by word list
 */
@Composable
fun ResultsContent(
    state: DictionaryUiState.Success,
    selectedPhrase: String,
    phraseTranslation: String?,
    viewModel: TranslatorViewModel
) {
    // Observe saved words to check if phrase is saved
    val savedWords by viewModel.savedWordsSet.collectAsState()

    Log.d(TAG, "[RENDER] Displaying results: fullTranslation='${state.fullTranslation}' wordCount=${state.wordTranslations.size}")

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Full Sentence Translation
        item {
            Log.d(TAG, "[ITEM] Rendering TranslationResultHeader with: '${state.fullTranslation}'")
            TranslationResultHeader(translation = state.fullTranslation)
        }

        // 2. Phrase Builder
        item {
            val originalWords = state.wordTranslations.map { it.original }
            Log.d(TAG, "[ITEM] Rendering PhraseBuilderSection with ${originalWords.size} words")
            PhraseBuilderSection(
                words = originalWords,
                onPhraseChanged = { viewModel.onPhraseSelectionChanged(it) }
            )
        }

        // 3. Phrase Result
        item {
            // Check if phrase is in saved words set
            val isPhraseSaved = savedWords.contains(selectedPhrase)
            PhraseResultCard(
                original = selectedPhrase,
                translation = phraseTranslation,
                isSaved = isPhraseSaved,
                onSave = { viewModel.toggleSave(selectedPhrase) },
                onPlayAudio = { text, _ ->
                    viewModel.pronounceOriginal(text)
                }
            )
        }

        // 4. Word by Word Header
        item {
            WordByWordHeader()
        }

        // 5. Individual Words
        items(state.wordTranslations) { wordItem ->
            WordRowItem(
                wordResult = wordItem,
                onToggleSave = { viewModel.toggleSave(it) },
                onPlayAudio = { text, _ ->
                    viewModel.pronounceOriginal(text)
                }
            )
        }
    }
}

