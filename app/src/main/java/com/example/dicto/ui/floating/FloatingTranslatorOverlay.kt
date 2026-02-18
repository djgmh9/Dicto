package com.example.dicto.ui.floating

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.dicto.DictionaryViewModel

/**
 * FloatingTranslatorOverlay - Full translator interface as overlay
 *
 * Displays translator content in a floating window overlay
 * Can be shown over any app
 */
@Composable
fun FloatingTranslatorOverlay(
    viewModel: DictionaryViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val searchQuery by viewModel.searchQuery.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

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
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.8f),
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
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Translator",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Filled.Close, contentDescription = "Close")
                        }
                    }

                    HorizontalDivider()

                    // Search input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onQueryChanged(it) },
                        label = { Text("أدخل جملة") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    )

                    // Results
                    when (uiState) {
                        is com.example.dicto.DictionaryUiState.Idle -> {
                            Text("Enter text to translate")
                        }
                        is com.example.dicto.DictionaryUiState.Loading -> {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        }
                        is com.example.dicto.DictionaryUiState.Error -> {
                            val errorState = uiState as com.example.dicto.DictionaryUiState.Error
                            Text("Error: ${errorState.message}", color = MaterialTheme.colorScheme.error)
                        }
                        is com.example.dicto.DictionaryUiState.Success -> {
                            val successState = uiState as com.example.dicto.DictionaryUiState.Success
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Text(
                                    "Translation:",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Text(
                                    successState.fullTranslation,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}




