package com.example.dicto.ui.floating

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.dicto.presentation.screens.translator.TranslatorViewModel
import com.example.dicto.ui.components.TranslatorUI

/**
 * FloatingTranslatorOverlay - Full translator interface as overlay
 *
 * Single Responsibility: Provide dialog container for translator UI
 * DRY Principle: Reuses TranslatorUI component (no duplication)
 *
 * This is a thin wrapper that adds:
 * - Dialog container with close button
 * - Transparent background
 * - Proper sizing for overlay display
 */
@Composable
fun FloatingTranslatorOverlay(
    viewModel: TranslatorViewModel,
    onDismiss: () -> Unit
) {
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
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.9f),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header with close button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Floating Translator",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Rounded.Close, contentDescription = "Close")
                        }
                    }

                    HorizontalDivider()

                    // Reuse the complete translator UI
                    TranslatorUI(
                        viewModel = viewModel,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
