package com.example.dicto

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.TextStyle

@Composable
fun DictionaryScreen(
    modifier: Modifier = Modifier,
    viewModel: DictionaryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var textInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- INPUT SECTION ---
        Text("Arabic -> English Dictionary", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = textInput,
            onValueChange = {
                textInput = it
                viewModel.onQueryChanged(it)
            },
            label = {
                // We can also align the label to the right
                Text("أدخل جملة (Enter sentence)")
            },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,

            // --- NEW: FORCE RTL SUPPORT ---
            textStyle = TextStyle(
                textDirection = TextDirection.Rtl, // Forces text to start from the Right
                fontSize = MaterialTheme.typography.bodyLarge.fontSize
            ),
            // Optional: If you want the label to always align right too:
            // colors = OutlinedTextFieldDefaults.colors(),
            // but usually just the input text being RTL is enough.
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.translate() },
            enabled = uiState !is DictionaryUiState.Loading,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Translate")
        }

        Spacer(modifier = Modifier.height(24.dp))

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
                    // Item 1: The Full Sentence Translation Header
                    item {
                        Text("Full Translation:", style = MaterialTheme.typography.labelLarge)
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = state.fullTranslation,
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text("Word by Word:", style = MaterialTheme.typography.labelLarge)
                    }

                    // Item 2...N: The list of individual words
                    items(state.wordTranslations) { wordItem ->
                        WordRowItem(wordItem)
                    }
                }
            }
        }
    }
}

// Helper composable for a single row
@Composable
fun WordRowItem(wordResult: WordResult) {
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
            // ENGLISH (Left Side): Force LTR
            Text(
                text = wordResult.translation,
                color = MaterialTheme.colorScheme.primary,
                // MERGE: Take bodyLarge AND apply LTR direction
                style = MaterialTheme.typography.bodyLarge.copy(
                    textDirection = TextDirection.Ltr
                )
            )

            // ARABIC (Right Side): Force RTL
            Text(
                text = wordResult.original,
                // MERGE: Take bodyLarge AND apply Bold AND RTL direction
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    textDirection = TextDirection.Rtl
                )
            )
        }
    }
}