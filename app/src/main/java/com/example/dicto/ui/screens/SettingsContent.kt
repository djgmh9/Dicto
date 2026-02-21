package com.example.dicto.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dicto.presentation.screens.settings.SettingsViewModel
import com.example.dicto.utils.AppLogger
import com.example.dicto.utils.PermissionHelper
import com.example.dicto.utils.logging.FloatingWindowLogger

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
fun SettingsContent(
    viewModel: SettingsViewModel
) {
    val context = LocalContext.current
    val clipboardMonitoringEnabled by viewModel.clipboardMonitoringEnabled.collectAsStateWithLifecycle()
    val floatingWindowEnabled by viewModel.floatingWindowEnabled.collectAsStateWithLifecycle()

    // Log the current state when composing
    FloatingWindowLogger.settingsContentRecomposed(floatingWindowEnabled)

    // Note: Do NOT control FloatingWindowService here
    // MainActivity.onPause() and onResume() handle all service lifecycle

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
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
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
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
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
                                    FloatingWindowLogger.settingsToggleClicked(floatingWindowEnabled)
                                    if (!floatingWindowEnabled) {
                                        // User wants to enable - check permission first
                                        FloatingWindowLogger.userWantsToEnable()
                                        AppLogger.logUserAction("Floating Translator Toggle", "Toggling ON")
                                        if (PermissionHelper.canDrawOverlays(context)) {
                                            // Permission granted, toggle preference only
                                            FloatingWindowLogger.permissionGranted()
                                            AppLogger.logServiceState("FloatingWindow", "PERMISSION_GRANTED")
                                            viewModel.toggleFloatingWindow()
                                            FloatingWindowLogger.toggleFloatingWindowCalled("settings enable")
                                            // MainActivity.onPause() will handle starting service
                                        } else {
                                            // Permission not granted, open settings to request
                                            FloatingWindowLogger.permissionDenied()
                                            AppLogger.logUserAction("Floating Translator", "Permission not granted, opening settings")
                                            PermissionHelper.requestOverlayPermission(context)
                                        }
                                    } else {
                                        // User wants to disable - toggle preference only
                                        FloatingWindowLogger.userWantsToDisable()
                                        AppLogger.logUserAction("Floating Translator Toggle", "Toggling OFF")
                                        viewModel.toggleFloatingWindow()
                                        FloatingWindowLogger.toggleFloatingWindowCalled("settings disable")
                                        // MainActivity.onPause() will handle stopping service
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
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
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

