package com.example.dicto.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import com.example.dicto.WordResult

/**
 * WordRowItem - Displays a single word translation in a card format
 *
 * Single Responsibility: Display a word with its translation and save toggle
 * Reusable in: TranslatorContent, SavedWordsContent
 *
 * @param wordResult The word data to display
 * @param onToggleSave Callback when user clicks the star icon
 */
@Composable
fun WordRowItem(
    wordResult: WordResult,
    onToggleSave: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT: Star Icon + English Translation
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
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        textDirection = TextDirection.Ltr
                    )
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // RIGHT: Arabic Text
            Text(
                text = wordResult.original,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    textDirection = TextDirection.Rtl
                ),
                modifier = Modifier.weight(1f)
            )
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
            tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    }
}

