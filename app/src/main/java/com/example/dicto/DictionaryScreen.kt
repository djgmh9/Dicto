package com.example.dicto

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.* // Make sure to import all '*' from runtime
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.TextStyle

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

/**
 * DictionaryScreen - Main container for navigation between tabs
 *
 * Follows separation of concerns:
 * - Handles tab navigation
 * - Delegates to specific content screens
 */
@Composable
fun DictionaryScreen(
    modifier: Modifier = Modifier,
    selectedTab: Int,
    viewModel: DictionaryViewModel = viewModel()
) {
    // Content is now switched based on the selectedTab passed from MainActivity
    Box(modifier = modifier.fillMaxSize()) {
        when (selectedTab) {
            0 -> TranslatorContent(viewModel)
            1 -> SavedWordsContent(viewModel)
        }
    }
}

@Composable
fun TranslatorContent(viewModel: DictionaryViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    // IMPORTANT: Binding the text field to the ViewModel
    val textInput by viewModel.searchQuery.collectAsState()

    // Phrase builder states
    val selectedPhrase by viewModel.selectedPhrase.collectAsState()
    val phraseTranslation by viewModel.phraseTranslation.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = textInput,
            onValueChange = {
                // This updates the ViewModel, which triggers the debounce flow
                viewModel.onQueryChanged(it)
            },
            label = { Text("أدخل جملة (Enter sentence)") },
            modifier = Modifier.fillMaxWidth(),
            // RTL support
            textStyle = TextStyle(
                textDirection = TextDirection.Rtl,
                fontSize = MaterialTheme.typography.bodyLarge.fontSize
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Clear button
        if (textInput.isNotEmpty()) {
            Button(
                onClick = { viewModel.onQueryChanged("") }, // Clear text
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Clear")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- RESULTS SECTION ---
        when (val state = uiState) {
            is DictionaryUiState.Idle -> {
                Text("Enter text to start", style = MaterialTheme.typography.bodyLarge)
            }
            is DictionaryUiState.Loading -> {
                CircularProgressIndicator()
            }
            is DictionaryUiState.Error -> {
                Text(text = state.message, color = MaterialTheme.colorScheme.error)
            }
            is DictionaryUiState.Success -> {
                // We use LazyColumn for the list so it scrolls efficiently
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. Full Sentence Result
                    item {
                        Text("Full Translation:", style = MaterialTheme.typography.labelLarge)
                        Text(state.fullTranslation, style = MaterialTheme.typography.headlineSmall)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    }

                    // 2. PHRASE BUILDER SECTION
                    item {
                        // Extract just the original words list from the result
                        val originalWords = state.wordTranslations.map { it.original }

                        PhraseBuilderSection(
                            words = originalWords,
                            onPhraseChanged = { viewModel.onPhraseSelectionChanged(it) }
                        )
                    }

                    // 3. PHRASE RESULT DISPLAY
                    item {
                        // Check if the phrase is saved by looking it up in saved words
                        val isPhraseSaved = state.wordTranslations.any { it.original == selectedPhrase && it.isSaved }

                        PhraseResultCard(
                            original = selectedPhrase,
                            translation = phraseTranslation,
                            isSaved = isPhraseSaved,
                            onSave = { viewModel.toggleSave(selectedPhrase) }
                        )
                    }

                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                        Text("Word by Word:", style = MaterialTheme.typography.labelLarge)
                    }

                    // 4. Individual Words
                    items(state.wordTranslations) { wordItem ->
                        WordRowItem(
                            wordResult = wordItem,
                            onToggleSave = { viewModel.toggleSave(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SavedWordsContent(viewModel: DictionaryViewModel) {
    val savedWords by viewModel.savedWordsList.collectAsState()

    if (savedWords.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No saved words yet", style = MaterialTheme.typography.bodyLarge)
        }
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
                // We reuse the exact same Row Item!
                // Because we pass 'onToggleSave', clicking the star here will UN-SAVE it
                // and it will instantly disappear from this list.
                WordRowItem(
                    wordResult = wordResult,
                    onToggleSave = { viewModel.toggleSave(it) }
                )
            }
        }
    }
}

// Helper composable for a single row
@Composable
fun WordRowItem(
    wordResult: WordResult,
    onToggleSave: (String) -> Unit // Callback function
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT: Star Icon + English
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onToggleSave(wordResult.original) }) {
                    Icon(
                        imageVector = if (wordResult.isSaved) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Save word",
                        tint = if (wordResult.isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = wordResult.translation,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.Ltr)
                )
            }

            // RIGHT: Arabic
            Text(
                text = wordResult.original,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    textDirection = TextDirection.Rtl
                )
            )
        }
    }
}
