package com.example.dicto.utils.clipboard

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.dicto.presentation.screens.translator.TranslatorViewModel
import com.example.dicto.utils.AppLogger
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
    Log.d("CLIPBOARD_MGR", "[COMPOSE] ClipboardMonitoringManager called: selectedTab=$selectedTab, isEnabled=$isEnabled")
    AppLogger.logAppEvent("ClipboardMonitoringManager", "[COMPOSE] Called: selectedTab=$selectedTab, isEnabled=$isEnabled")

    // Only create and manage clipboard monitoring if it's actually enabled
    if (!isEnabled) {
        Log.d("CLIPBOARD_MGR", "[EARLY_RETURN] isEnabled=false, returning without creating anything")
        AppLogger.logAppEvent("ClipboardMonitoringManager", "[EARLY_RETURN] isEnabled=false, not creating manager")
        return  // Don't create anything if disabled
    }

    Log.d("CLIPBOARD_MGR", "[REMEMBER] Creating ClipboardMonitor instance")
    AppLogger.logAppEvent("ClipboardMonitoringManager", "[REMEMBER] About to create ClipboardMonitor in remember block")

    // Lazy initialize ClipboardMonitor with ONE_TIME mode - only checks once when monitoring is enabled
    val clipboardMonitor = androidx.compose.runtime.remember {
        Log.d("CLIPBOARD_MGR", "[REMEMBER_BLOCK] ClipboardMonitor instance being created NOW")
        AppLogger.logAppEvent("ClipboardMonitoringManager", "[REMEMBER_BLOCK] ClipboardMonitor instance created in remember block")
        ClipboardMonitor(context, lifecycleOwner.lifecycleScope, ClipboardMonitor.MonitoringMode.ONE_TIME)
    }

    Log.d("CLIPBOARD_MGR", "[DISPOSABLE_EFFECT] Setting up DisposableEffect with dependencies: lifecycleOwner, selectedTab=$selectedTab")
    AppLogger.logAppEvent("ClipboardMonitoringManager", "[DISPOSABLE_EFFECT] Setting up with selectedTab=$selectedTab")

    // Observe clipboard when conditions are met
    DisposableEffect(lifecycleOwner, selectedTab) {
        Log.d("CLIPBOARD_MGR", "[DISPOSE_SETUP] DisposableEffect setup block executing")
        AppLogger.logAppEvent("ClipboardMonitoringManager", "[DISPOSE_SETUP] DisposableEffect setup running")

        val observer = LifecycleEventObserver { _, event ->
            Log.d("CLIPBOARD_MGR", "[LIFECYCLE_EVENT] Event=$event, selectedTab=$selectedTab")
            AppLogger.logAppEvent("ClipboardMonitoringManager", "[LIFECYCLE_EVENT] $event, selectedTab=$selectedTab")

            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    Log.d("CLIPBOARD_MGR", "[ON_RESUME] Event fired, checking if selectedTab=0")
                    AppLogger.logAppEvent("ClipboardMonitoringManager", "[ON_RESUME] Checking conditions")

                    // Only monitor when on translator tab (0)
                    if (selectedTab == 0) {
                        Log.d("CLIPBOARD_MGR", "[ON_RESUME_PROCEED] selectedTab=0 is TRUE, starting monitoring")
                        AppLogger.logAppEvent("ClipboardMonitoringManager", "[ON_RESUME_PROCEED] selectedTab=0 is true, STARTING monitoring")

                        lifecycleOwner.lifecycleScope.launch {
                            Log.d("CLIPBOARD_MGR", "[LAUNCH_DELAY] Delaying 300ms before startMonitoring()")
                            AppLogger.logAppEvent("ClipboardMonitoringManager", "[LAUNCH_DELAY] Waiting 300ms")

                            delay(300)

                            Log.d("CLIPBOARD_MGR", "[START_MONITORING] Calling clipboardMonitor.startMonitoring()")
                            AppLogger.logAppEvent("ClipboardMonitoringManager", "[START_MONITORING] Calling startMonitoring() NOW")

                            clipboardMonitor.startMonitoring { text ->
                                Log.d("CLIPBOARD_MGR", "[TEXT_FOUND] Clipboard text found: $text")
                                AppLogger.logAppEvent("ClipboardMonitoringManager", "[TEXT_FOUND] Text=$text")
                                viewModel.onClipboardTextFound(text)
                            }
                        }
                    } else {
                        Log.d("CLIPBOARD_MGR", "[ON_RESUME_SKIP] selectedTab=$selectedTab (NOT 0), skipping monitoring")
                        AppLogger.logAppEvent("ClipboardMonitoringManager", "[ON_RESUME_SKIP] selectedTab=$selectedTab is not 0, skipping")
                    }
                }
                Lifecycle.Event.ON_PAUSE -> {
                    Log.d("CLIPBOARD_MGR", "[ON_PAUSE] Stopping monitoring")
                    AppLogger.logAppEvent("ClipboardMonitoringManager", "[ON_PAUSE] Stopping monitoring")
                    clipboardMonitor.stopMonitoring()
                }
                else -> {
                    Log.d("CLIPBOARD_MGR", "[OTHER_EVENT] Other lifecycle event: $event")
                }
            }
        }

        Log.d("CLIPBOARD_MGR", "[ADD_OBSERVER] Adding lifecycle observer")
        AppLogger.logAppEvent("ClipboardMonitoringManager", "[ADD_OBSERVER] Adding to lifecycle")
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            Log.d("CLIPBOARD_MGR", "[ON_DISPOSE] Dispose block executing")
            AppLogger.logAppEvent("ClipboardMonitoringManager", "[ON_DISPOSE] Disposing")
            lifecycleOwner.lifecycle.removeObserver(observer)
            clipboardMonitor.stopMonitoring()
        }
    }

    Log.d("CLIPBOARD_MGR", "[COMPOSE_END] ClipboardMonitoringManager composition complete")
}

