package com.example.dicto

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.TextStyle

import com.example.dicto.ui.components.*

/**
 * DictionaryScreen - Main container for navigation between tabs
 *
 * Follows separation of concerns:
 * - Handles tab navigation logic
 * - Delegates to specific content screens
 * - Does not handle business logic
 */
@Composable
fun DictionaryScreen(
    modifier: Modifier = Modifier,
    selectedTab: Int,
    viewModel: DictionaryViewModel = viewModel()
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (selectedTab) {
            0 -> TranslatorContent(viewModel)
            1 -> SavedWordsContent(viewModel)
            2 -> SettingsContent(viewModel)
        }
    }
}

/**
 * TranslatorContent - Main translator interface
 *
 * Responsibilities:
 * - Input text field
 * - Translation state display (Loading, Error, Success)
 * - Phrase builder
 * - Word by word results
 *
 * Uses extracted components for each section
 */
@Composable
fun TranslatorContent(viewModel: DictionaryViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val textInput by viewModel.searchQuery.collectAsState()
    val selectedPhrase by viewModel.selectedPhrase.collectAsState()
    val phraseTranslation by viewModel.phraseTranslation.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Input TextField with Pronunciation Button
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { viewModel.onQueryChanged(it) },
                label = { Text("أدخل جملة (Enter sentence)") },
                modifier = Modifier
                    .weight(1f),
                textStyle = TextStyle(
                    textDirection = TextDirection.Rtl,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize
                )
            )

            // Pronunciation button for input sentence
            if (textInput.isNotEmpty()) {
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
        if (textInput.isNotEmpty()) {
            Button(
                onClick = { viewModel.onQueryChanged("") },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Clear")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Results Section - Changes based on UI state
        when (val state = uiState) {
            is DictionaryUiState.Idle -> {
                EmptyStateDisplay(message = "Enter text to start")
            }

            is DictionaryUiState.Loading -> {
                LoadingStateIndicator()
            }

            is DictionaryUiState.Error -> {
                ErrorStateDisplay(message = state.message)
            }

            is DictionaryUiState.Success -> {
                ResultsContent(
                    state = state,
                    selectedPhrase = selectedPhrase,
                    phraseTranslation = phraseTranslation,
                    viewModel = viewModel
                )
            }
        }
    }
}

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
private fun ResultsContent(
    state: DictionaryUiState.Success,
    selectedPhrase: String,
    phraseTranslation: String?,
    viewModel: DictionaryViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
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
            val isPhraseSaved = state.wordTranslations.any {
                it.original == selectedPhrase && it.isSaved
            }
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

/**
 * SavedWordsContent - Displays user's saved words library
 *
 * Responsibilities:
 * - List all saved words
 * - Allow unsaving words
 * - Show empty state when no words saved
 */
@Composable
fun SavedWordsContent(viewModel: DictionaryViewModel) {
    val savedWords by viewModel.savedWordsList.collectAsState()

    if (savedWords.isEmpty()) {
        EmptyStateDisplay(message = "No saved words yet")
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    "My Vocabulary",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            items(savedWords) { wordResult ->
                WordRowItem(
                    wordResult = wordResult,
                    onToggleSave = { viewModel.toggleSave(it) },
                    onPlayAudio = { text, _ ->
                        viewModel.pronounceOriginal(text)
                    }
                )
            }
        }
    }
}

/**
 * SettingsContent - Displays application settings as a tab
 *
 * Responsibilities:
 * - Clipboard monitoring toggle
 * - About information
 * - All settings in compact tab format
 */
@Composable
fun SettingsContent(viewModel: DictionaryViewModel) {
    val clipboardMonitoringEnabled by viewModel.clipboardMonitoringEnabled.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Settings",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Clipboard Monitoring Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Auto-Translate Clipboard",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    "Auto-translate copied text",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = clipboardMonitoringEnabled,
                                onCheckedChange = { viewModel.toggleClipboardMonitoring() }
                            )
                        }
                    }
                }
            }

            // About Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Dicto",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            "Version 1.0",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Arabic to English Dictionary with Auto-Translate",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
