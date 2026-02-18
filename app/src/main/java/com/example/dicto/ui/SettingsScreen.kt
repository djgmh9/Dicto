package com.example.dicto.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dicto.DictionaryViewModel

/**
 * SettingsScreen - Handles all application settings
 * Follows modern separation of concerns pattern
 */
@Composable
fun SettingsScreen(
    viewModel: DictionaryViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Header with back button
        SettingsHeader(onBackClick = onBackClick)

        // Settings content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            item {
                ClipboardMonitoringSettings(viewModel = viewModel)
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
            }

            item {
                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            item {
                AboutCard()
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

/**
 * SettingsHeader - Reusable header component with back button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsHeader(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text("Settings") },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        modifier = modifier
    )
}

/**
 * ClipboardMonitoringSettings - Isolated clipboard monitoring configuration
 * Single Responsibility: Only handles clipboard monitoring UI
 */
@Composable
private fun ClipboardMonitoringSettings(
    viewModel: DictionaryViewModel,
    modifier: Modifier = Modifier
) {
    val clipboardMonitoringEnabled by viewModel.clipboardMonitoringEnabled.collectAsState()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title with icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ContentPaste,
                        contentDescription = "Clipboard monitoring",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Auto-Translate from Clipboard",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Automatically translate text copied to clipboard",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = clipboardMonitoringEnabled,
                    onCheckedChange = { viewModel.toggleClipboardMonitoring() },
                    modifier = Modifier.padding(start = 12.dp)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Description
            Text(
                text = buildString {
                    append("When enabled, Dicto monitors your clipboard and automatically ")
                    append("translates any text you copy from other applications.\n\n")
                    append("Currently: ")
                    append(if (clipboardMonitoringEnabled) "ON" else "OFF")
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * AboutCard - Application information
 */
@Composable
private fun AboutCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Dicto",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Version 1.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Arabic to English Dictionary with Auto-Translate",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

