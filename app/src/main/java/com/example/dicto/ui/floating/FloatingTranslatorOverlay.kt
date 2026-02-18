package com.example.dicto.ui.floating

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.dicto.DictionaryViewModel
import com.example.dicto.DictionaryUiState
import com.example.dicto.ui.components.*

/**
 * FloatingTranslatorOverlay - Full translator interface as overlay
 *
 * Complete translator with all features:
 * - Input text field with pronunciation
 * - Full translation
 * - Phrase builder
 * - Phrase result with save/pronounce
 * - Word by word translation with save/pronounce
 */
@Composable
fun FloatingTranslatorOverlay(
    viewModel: DictionaryViewModel,
    onDismiss: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val selectedPhrase by viewModel.selectedPhrase.collectAsState()
    val phraseTranslation by viewModel.phraseTranslation.collectAsState()
    val savedWords by viewModel.savedWordsList.collectAsState()

    // Transparent background for the entire dialog area
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.9f),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header with close button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Floating Translator",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Filled.Close, contentDescription = "Close")
                        }
                    }

                    HorizontalDivider()

                    Spacer(modifier = Modifier.height(8.dp))

                    // Input TextField with Pronunciation Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.onQueryChanged(it) },
                            label = { Text("أدخل جملة (Enter sentence)") },
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(
                                textDirection = TextDirection.Rtl,
                                fontSize = MaterialTheme.typography.bodyLarge.fontSize
                            )
                        )

                        // Pronunciation button for input sentence
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.pronounceInputSentence() },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.VolumeUp,
                                    contentDescription = "Pronounce input sentence",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Clear button
                    if (searchQuery.isNotEmpty()) {
                        Button(
                            onClick = { viewModel.onQueryChanged("") },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Clear")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Results Section
                    when (val state = uiState) {
                        is DictionaryUiState.Idle -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                EmptyStateDisplay(message = "Enter text to start")
                            }
                        }

                        is DictionaryUiState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                LoadingStateIndicator()
                            }
                        }

                        is DictionaryUiState.Error -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                ErrorStateDisplay(message = state.message)
                            }
                        }

                        is DictionaryUiState.Success -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 1. Full Sentence Translation
                                item {
                                    TranslationResultHeader(translation = state.fullTranslation)
                                }

                                // 2. Phrase Builder
                                item {
                                    val originalWords = state.wordTranslations.map { it.original }
                                    PhraseBuilderSection(
                                        words = originalWords,
                                        onPhraseChanged = { viewModel.onPhraseSelectionChanged(it) }
                                    )
                                }

                                // 3. Phrase Result
                                item {
                                    val isPhraseSaved = savedWords.any { it.original == selectedPhrase }
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
                    }
                }
            }
        }
    }
}




