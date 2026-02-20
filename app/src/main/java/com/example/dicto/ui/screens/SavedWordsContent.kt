package com.example.dicto.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dicto.domain.model.DictionaryUiState
import com.example.dicto.presentation.screens.saved.SavedWordsViewModel
import com.example.dicto.ui.components.*

/**
 * SavedWordsContent - Displays user's saved words library
 *
 * Responsibilities:
 * - List all saved words with error/loading states
 * - Allow unsaving words
 * - Show empty state when no words saved
 * - Show loading indicator while fetching
 * - Show error message if translation fails
 */
@Composable
fun SavedWordsContent(
    viewModel: SavedWordsViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (uiState) {
        is DictionaryUiState.Loading -> {
            // Show loading indicator
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is DictionaryUiState.Success -> {
            val savedWords = (uiState as DictionaryUiState.Success).wordTranslations
            if (savedWords.isEmpty()) {
                EmptyStateDisplay(message = "No saved words yet")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp),
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
                            onToggleSave = { viewModel.onDeleteWord(it) },
                            onPlayAudio = { text, _ ->
                                viewModel.onPlayPronunciation(text)
                            }
                        )
                    }
                }
            }
        }

        is DictionaryUiState.Error -> {
            // Show error message
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Error Loading Saved Words",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        (uiState as DictionaryUiState.Error).message,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        else -> {} // Handle Idle state if needed
    }
}

