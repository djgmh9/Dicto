package com.example.dicto.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp

/**
 * PhraseResultCard - Displays the result of a phrase builder selection
 *
 * Single Responsibility: Show phrase translation with save and pronunciation (source language only)
 * Features:
 * - Display Arabic phrase with pronunciation button
 * - Display English translation (display only, no TTS)
 * - Save/unsave functionality
 * Used in: TranslatorContent phrase builder section
 *
 * @param original The original phrase in Arabic
 * @param translation The translated phrase in English (display only)
 * @param isSaved Whether the phrase is currently saved
 * @param onSave Callback when user toggles save
 * @param onPlayAudio Callback to play Arabic pronunciation
 */
@Composable
fun PhraseResultCard(
    original: String,
    translation: String?,
    isSaved: Boolean,
    onSave: () -> Unit,
    onPlayAudio: (String, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    if (original.isBlank()) {
        return // Don't show card if no phrase is selected
    }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with title and save button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Phrase",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                IconButton(
                    onClick = onSave,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = if (isSaved) "Remove phrase from saved" else "Save phrase",
                        tint = if (isSaved)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Original phrase (Arabic) with pronunciation button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    text = original,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        textDirection = TextDirection.Rtl
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { onPlayAudio(original, "original") },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.VolumeUp,
                        contentDescription = "Pronounce Arabic phrase",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Translation (English) - Display only, no pronunciation
            if (!translation.isNullOrBlank()) {
                Text(
                    text = translation,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                Text(
                    text = "Translating...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

