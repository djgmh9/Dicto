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
import com.example.dicto.ui.theme.WhiteCard
import com.example.dicto.ui.theme.CardShape

/**
 * PhraseBuilderSection - Allows users to select multiple words and build a phrase
 *
 * Single Responsibility: Word selection UI with phrase building logic
 * Features:
 * - RTL support for Arabic words
 * - Multiple word selection
 * - Automatic phrase ordering
 *
 * @param words List of words available for selection
 * @param onPhraseChanged Callback with selected words in order
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
            "Tap words to form a phrase:",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(8.dp))

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
                            val sortedIndices = newSelection.sorted()
                            val sortedWords = sortedIndices.map { words[it] }

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
        label = { Text(word) },
        shape = CardShape,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = WhiteCard,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

