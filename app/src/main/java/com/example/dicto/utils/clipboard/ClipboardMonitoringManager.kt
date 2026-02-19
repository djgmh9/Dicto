package com.example.dicto.utils.clipboard

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.dicto.presentation.screens.translator.TranslatorViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ClipboardMonitoringManager - Handles clipboard monitoring lifecycle
 *
 * Single Responsibility: Manage clipboard monitoring based on app state
 * Features:
 * - Starts/stops monitoring based on selected tab and preference
 * - Lifecycle-aware (respects app pause/resume)
 * - Delegates to ClipboardMonitor utility
 *
 * Parameters:
 * - context: Android context for ClipboardMonitor
 * - lifecycleOwner: For lifecycle observation
 * - viewModel: To access preferences and handle text found
 * - selectedTab: Currently selected tab (only monitors on tab 0)
 * - isEnabled: Whether user has enabled clipboard monitoring
 */
@Composable
fun ClipboardMonitoringManager(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    viewModel: TranslatorViewModel,
    selectedTab: Int,
    isEnabled: Boolean
) {
    // Lazy initialize ClipboardMonitor with ONE_TIME mode - only checks once on app start
    val clipboardMonitor = androidx.compose.runtime.remember {
        ClipboardMonitor(context, lifecycleOwner.lifecycleScope, ClipboardMonitor.MonitoringMode.ONE_TIME)
    }

    // Observe clipboard when conditions are met
    DisposableEffect(lifecycleOwner, selectedTab, isEnabled) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    // Only monitor when on translator tab (0) and monitoring is enabled
                    if (selectedTab == 0 && isEnabled) {
                        lifecycleOwner.lifecycleScope.launch {
                            delay(300)
                            clipboardMonitor.startMonitoring { text ->
                                viewModel.onClipboardTextFound(text)
                            }
                        }
                    }
                }
                Lifecycle.Event.ON_PAUSE -> {
                    clipboardMonitor.stopMonitoring()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        // Initial state: Start monitoring if conditions are met
        if (selectedTab == 0 &&
            lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) &&
            isEnabled) {
            lifecycleOwner.lifecycleScope.launch {
                delay(300)
                clipboardMonitor.startMonitoring { text ->
                    viewModel.onClipboardTextFound(text)
                }
            }
        } else {
            clipboardMonitor.stopMonitoring()
        }

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            clipboardMonitor.stopMonitoring()
        }
    }
}

