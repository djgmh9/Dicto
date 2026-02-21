package com.example.dicto.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.dicto.ui.theme.CardShape
import androidx.compose.material3.MaterialTheme

/**
 * PhraseBuilderSection - Allows users to select words to build a phrase
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PhraseBuilderSection(
    words: List<String>,
    onPhraseChanged: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    // Track which word indices are currently selected
    var selectedIndices by remember { mutableStateOf(setOf<Int>()) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            "Tap words to form a phrase",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        // RTL layout support for Arabic words
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                words.forEachIndexed { index, word ->
                    val isSelected = selectedIndices.contains(index)

                    WordFilterChip(
                        word = word,
                        isSelected = isSelected,
                        onClick = {
                            // Toggle selection: add or remove this index
                            val newSelection = if (isSelected) {
                                selectedIndices - index
                            } else {
                                selectedIndices + index
                            }
                            selectedIndices = newSelection

                            // Sort indices to maintain phrase order (Word 1 + Word 2, not Word 2 + Word 1)
                            val sortedWords = newSelection.sorted().map { words[it] }

                            onPhraseChanged(sortedWords)
                        }
                    )
                }
            }
        }
    }
}

/**
 * WordFilterChip - Reusable word selection chip
 *
 * Single Responsibility: Display a selectable word chip
 *
 * @param word The word to display
 * @param isSelected Whether the word is currently selected
 * @param onClick Callback when chip is clicked
 */
@Composable
private fun WordFilterChip(
    word: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                word,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        shape = CardShape,
        colors = FilterChipDefaults.filterChipColors(
            // Unselected: blend with background
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            // Selected: primary brand color
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = MaterialTheme.colorScheme.outlineVariant,
            selectedBorderColor = MaterialTheme.colorScheme.primary
        )
    )
}
