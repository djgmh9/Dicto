package com.example.dicto

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class) // FlowRow requires this opt-in in some versions
@Composable
fun PhraseBuilderSection(
    words: List<String>,
    modifier: Modifier = Modifier,
    onPhraseChanged: (List<String>) -> Unit
) {
    // We keep track of which indices are selected (e.g., word 0 and word 1)
    var selectedIndices by remember { mutableStateOf(setOf<Int>()) }

    Column(modifier = modifier) {
        Text(
            "Tap words to form a phrase:",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        // This allows RTL layout for the chips (Essential for Arabic)
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                words.forEachIndexed { index, word ->
                    val isSelected = selectedIndices.contains(index)
                    
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            // Toggle selection logic
                            val newSelection = if (isSelected) {
                                selectedIndices - index
                            } else {
                                selectedIndices + index
                            }
                            selectedIndices = newSelection
                            
                            // Sort indices to keep phrase in order (Word 1 + Word 2, not Word 2 + Word 1)
                            val sortedIndices = newSelection.sorted()
                            val sortedWords = sortedIndices.map { words[it] }
                            
                            onPhraseChanged(sortedWords)
                        },
                        label = { Text(word) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun PhraseResultCard(
    original: String,
    translation: String?,
    onSave: () -> Unit
) {
    if (original.isBlank() || translation == null) return

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        ),
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Phrase: $original",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = translation,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Allow saving this phrase too!
            IconButton(onClick = onSave) {
                Icon(Icons.Outlined.StarBorder, contentDescription = "Save Phrase")
            }
        }
    }
}