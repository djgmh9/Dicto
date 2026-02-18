package com.example.dicto

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.dicto.ui.theme.DictoTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.dicto.utils.ClipboardMonitor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * MainActivity - Application entry point
 *
 * Responsibilities:
 * - Manage lifecycle and navigation
 * - Delegate clipboard monitoring to ClipboardMonitor utility
 * - Provide DI and context to composed screens
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DictoTheme {
                MainContent()
            }
        }
    }
}

/**
 * MainContent - Composable root for the app navigation
 * Handles navigation state and lifecycle management
 */
@Composable
private fun MainContent() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val viewModel: DictionaryViewModel = viewModel()

    // Navigation state
    var selectedTab by remember { mutableIntStateOf(0) }

    // Observe clipboard monitoring preference - waits for DataStore to load the actual saved value
    val clipboardMonitoringEnabled by viewModel.clipboardMonitoringEnabled.collectAsState()

    // Lazy initialize ClipboardMonitor only once
    val clipboardMonitor = remember {
        ClipboardMonitor(context, lifecycleOwner.lifecycleScope)
    }

    // --- LIFECYCLE-AWARE CLIPBOARD MONITORING ---
    DisposableEffect(lifecycleOwner, selectedTab, clipboardMonitoringEnabled) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    // Only monitor when on translator tab (0) and monitoring is enabled
                    if (selectedTab == 0 && clipboardMonitoringEnabled) {
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
            clipboardMonitoringEnabled) {
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

    // Main layout with tab navigation
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Translator") },
                    label = { Text("Translator") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Favorite, contentDescription = "Saved Words") },
                    label = { Text("Saved") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
            }
        }
    ) { innerPadding ->
        DictionaryScreen(
            modifier = Modifier.padding(innerPadding),
            selectedTab = selectedTab,
            viewModel = viewModel
        )
    }
}