package com.example.dicto.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp

/**
 * TranslationResultHeader - Displays full sentence translation
 *
 * Single Responsibility: Show translated full sentence
 * Used in: TranslatorContent
 *
 * @param translation The translated text
 */
@Composable
fun TranslationResultHeader(
    translation: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            "Full Translation:",
            style = MaterialTheme.typography.labelLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            translation,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
    }
}

/**
 * WordByWordHeader - Section header for word translations
 *
 * Single Responsibility: Display section header
 * Used in: TranslatorContent
 */
@Composable
fun WordByWordHeader(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider()

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Word by Word:",
            style = MaterialTheme.typography.labelLarge
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

