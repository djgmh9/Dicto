package com.example.dicto.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import com.example.dicto.DictionaryViewModel
import com.example.dicto.DictionaryUiState
import com.example.dicto.ui.components.*

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

