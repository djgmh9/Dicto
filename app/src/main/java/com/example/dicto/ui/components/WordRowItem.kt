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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import com.example.dicto.domain.model.WordResult
import com.example.dicto.ui.theme.CardShape

/**
 * WordRowItem - Displays a single word translation in a card format
 *
 * Single Responsibility: Display a word with translation, pronunciation (source language only), and save toggle
 * Features:
 * - Star icon for save/unsave
 * - Speaker icon for Arabic pronunciation only
 * - Translation text (display only, no pronunciation)
 * - Original Arabic word with pronunciation
 * Reusable in: TranslatorContent, SavedWordsContent
 *
 * @param wordResult The word data to display
 * @param onToggleSave Callback when user clicks the star icon
 * @param onPlayAudio Callback to play Arabic pronunciation
 */
@Composable
fun WordRowItem(
    wordResult: WordResult,
    onToggleSave: (String) -> Unit,
    onPlayAudio: (String, String) -> Unit = { _, _ -> },  // Default no-op
    modifier: Modifier = Modifier
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = CardShape,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT: Star + English translation
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                SaveWordIconButton(
                    isSaved = wordResult.isSaved,
                    onToggle = { onToggleSave(wordResult.original) }
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = wordResult.translation,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // RIGHT: Arabic word + pronunciation
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = wordResult.original,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        textDirection = TextDirection.Rtl
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Pronunciation button for Arabic word
                PronunciationIconButton(
                    text = wordResult.original,
                    onPlay = { onPlayAudio(wordResult.original, "original") },
                    contentDescription = "Pronounce Arabic"
                )
            }
        }
    }
}

/**
 * SaveWordIconButton - Reusable star icon button for saving words
 *
 * Single Responsibility: Toggle save state with visual feedback
 *
 * @param isSaved Current save state
 * @param onToggle Callback when clicked
 */
@Composable
private fun SaveWordIconButton(
    isSaved: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onToggle,
        modifier = modifier
    ) {
        Icon(
            imageVector = if (isSaved) Icons.Filled.Star else Icons.Outlined.StarBorder,
            contentDescription = if (isSaved) "Remove from saved" else "Save word",
            tint = if (isSaved) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * PronunciationIconButton - Reusable speaker icon button for pronunciation
 *
 * Single Responsibility: Play audio pronunciation with visual feedback
 *
 * @param text The text to pronounce (not used in button, just for reference)
 * @param onPlay Callback when clicked to play pronunciation
 * @param contentDescription Accessibility description
 */
@Composable
private fun PronunciationIconButton(
    text: String,
    onPlay: () -> Unit,
    contentDescription: String = "Pronounce",
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onPlay,
        modifier = modifier.size(36.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.VolumeUp,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}
