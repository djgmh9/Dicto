package com.example.dicto.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import com.example.dicto.presentation.screens.translator.TranslatorViewModel
import com.example.dicto.domain.model.DictionaryUiState
import com.example.dicto.ui.screens.ResultsContent

/**
 * TranslatorUI - Reusable translator interface component
 *
 * Single Responsibility: Display complete translator interface
 * DRY Principle: Used by both main app and floating translator
 *
 * Features:
 * - Input text field with pronunciation
 * - Clear button
 * - State-based results display (Idle, Loading, Error, Success)
 * - Full translation, phrase builder, and word-by-word results
 *
 * This component is UI-only and doesn't know about its container
 * (whether it's in a tab, dialog, or floating window)
 */
@Composable
fun TranslatorUI(
    viewModel: TranslatorViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val selectedPhrase by viewModel.selectedPhrase.collectAsState()
    val phraseTranslation by viewModel.phraseTranslation.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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

        Spacer(modifier = Modifier.height(16.dp))

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



