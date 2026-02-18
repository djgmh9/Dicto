package com.example.dicto.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.dicto.DictionaryViewModel
import com.example.dicto.domain.FloatingWindowManager
import com.example.dicto.utils.PermissionHelper

/**
 * SettingsContent - Displays application settings as a tab
 *
 * Responsibilities:
 * - Clipboard monitoring toggle
 * - Floating window toggle
 * - About information
 * - All settings in compact tab format
 */
@Composable
fun SettingsContent(viewModel: DictionaryViewModel) {
    val context = LocalContext.current
    val clipboardMonitoringEnabled by viewModel.clipboardMonitoringEnabled.collectAsState()
    val floatingWindowEnabled by viewModel.floatingWindowEnabled.collectAsState()

    val floatingWindowManager = remember { FloatingWindowManager(context) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
    ) {
        Text(
            "Settings",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Clipboard Monitoring Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Auto-Translate Clipboard",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    "Auto-translate copied text",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = clipboardMonitoringEnabled,
                                onCheckedChange = { viewModel.toggleClipboardMonitoring() }
                            )
                        }
                    }
                }
            }

            // Floating Window Toggle Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Floating Translator",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    "Show floating button for quick translation",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = floatingWindowEnabled,
                                onCheckedChange = {
                                    if (!floatingWindowEnabled) {
                                        // User wants to enable - check permission first
                                        if (PermissionHelper.canDrawOverlays(context)) {
                                            // Permission granted, toggle ON and start service
                                            viewModel.toggleFloatingWindow()
                                            floatingWindowManager.startFloatingWindow()
                                        } else {
                                            // Permission not granted, open settings to request
                                            PermissionHelper.requestOverlayPermission(context)
                                        }
                                    } else {
                                        // User wants to disable - toggle OFF and stop service
                                        viewModel.toggleFloatingWindow()
                                        floatingWindowManager.stopFloatingWindow()
                                    }
                                }
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                            "Dicto",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            "Version 1.0",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Arabic to English Dictionary with Auto-Translate",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

