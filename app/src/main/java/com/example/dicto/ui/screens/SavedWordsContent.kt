package com.example.dicto.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dicto.presentation.screens.DictionaryViewModel
import com.example.dicto.ui.components.*

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
                    onToggleSave = { viewModel.toggleSave(it) },
                    onPlayAudio = { text, _ ->
                        viewModel.pronounceOriginal(text)
                    }
                )
            }
        }
    }
}

