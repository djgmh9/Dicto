package com.example.dicto.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * LoadingStateIndicator - Shows loading progress
 *
 * Single Responsibility: Display loading UI
 * Used in: TranslatorContent
 */
@Composable
fun LoadingStateIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * ErrorStateDisplay - Shows error messages
 *
 * Single Responsibility: Display error information
 * Used in: TranslatorContent
 *
 * @param message Error message to display
 */
@Composable
fun ErrorStateDisplay(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * EmptyStateDisplay - Shows empty state message
 *
 * Single Responsibility: Display empty state UI
 * Used in: TranslatorContent, SavedWordsContent
 *
 * @param message Message to display
 */
@Composable
fun EmptyStateDisplay(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

