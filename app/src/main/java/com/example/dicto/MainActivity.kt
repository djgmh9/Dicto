package com.example.dicto

import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.ui.tooling.preview.Preview
import com.example.dicto.ui.theme.DictoTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.Icons

import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive

class MainActivity : ComponentActivity() {
    private var clipboardMonitorJob: Job? = null
    private var lastClipboardText: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DictoTheme {
                val context = LocalContext.current
                val lifecycleOwner = LocalLifecycleOwner.current
                val viewModel: DictionaryViewModel = viewModel()
                var selectedTab by remember { mutableIntStateOf(0) }

                // --- CONTINUOUS CLIPBOARD MONITORING ---
                DisposableEffect(lifecycleOwner, selectedTab) {
                    val observer = LifecycleEventObserver { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_RESUME -> {
                                if (selectedTab == 0) {
                                    // Start continuous clipboard monitoring when on translator tab
                                    lifecycleScope.launch {
                                        delay(300) // Small delay for app to be ready
                                        startClipboardMonitoring(context, viewModel)
                                    }
                                }
                            }
                            Lifecycle.Event.ON_PAUSE -> {
                                // Stop monitoring when app is paused
                                stopClipboardMonitoring()
                            }
                            else -> {}
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)

                    // Start monitoring immediately if on translator tab and app is resumed
                    if (selectedTab == 0 && lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        lifecycleScope.launch {
                            delay(300)
                            startClipboardMonitoring(context, viewModel)
                        }
                    } else {
                        // Stop monitoring when switching away from translator tab
                        stopClipboardMonitoring()
                    }

                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                        stopClipboardMonitoring()
                    }
                }
                // ---------------------------------                // Scaffold provides the structure
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
                        }
                    }
                ) { innerPadding ->
                    // innerPadding calculates the safe area (avoiding status bar and navigation bar)
                    // Pass the viewModel down so the screen displays the result
                    DictionaryScreen(
                        modifier = Modifier.padding(innerPadding),
                        selectedTab = selectedTab,
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    private fun startClipboardMonitoring(context: Context, viewModel: DictionaryViewModel) {
        Log.d("MainActivity", "Starting clipboard monitoring")
        // Cancel any existing monitoring job
        stopClipboardMonitoring()

        // Check immediately on start
        checkClipboardForTranslation(context, viewModel)

        // Start continuous monitoring
        clipboardMonitorJob = lifecycleScope.launch {
            while (isActive) {
                delay(1000) // Check every second
                checkClipboardForTranslation(context, viewModel)
            }
        }
    }

    private fun stopClipboardMonitoring() {
        Log.d("MainActivity", "Stopping clipboard monitoring")
        clipboardMonitorJob?.cancel()
        clipboardMonitorJob = null
    }

    private fun checkClipboardForTranslation(context: Context, viewModel: DictionaryViewModel) {
        try {
            // Check if monitoring is enabled in ViewModel
            val isEnabled = viewModel.clipboardMonitoringEnabled.value
            Log.d("MainActivity", "Checking clipboard - monitoring enabled: $isEnabled")

            if (!isEnabled) {
                return
            }

            val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

            // Check if clipboard has any content
            if (clipboard.hasPrimaryClip()) {
                val clip = clipboard.primaryClip
                val clipDescription = clipboard.primaryClipDescription

                Log.d("MainActivity", "Clipboard has content. Item count: ${clip?.itemCount}, Description: ${clipDescription?.label}")

                // Try to get text from clipboard - be more lenient with MIME types
                if (clip != null && clip.itemCount > 0) {
                    val item = clip.getItemAt(0)

                    // Try multiple ways to get text
                    val text = when {
                        item.text != null -> item.text.toString()
                        item.coerceToText(context) != null -> item.coerceToText(context).toString()
                        else -> null
                    }

                    Log.d("MainActivity", "Clipboard text: '$text', Last: '$lastClipboardText', Current query: '${viewModel.searchQuery.value}'")

                    // Only trigger translation if:
                    // 1. Text is not blank
                    // 2. Text is different from last processed text
                    // 3. Text is different from current search query (to avoid loops)
                    if (!text.isNullOrBlank() &&
                        text != lastClipboardText &&
                        text != viewModel.searchQuery.value) {
                        Log.d("MainActivity", "✓ Found new clipboard text, triggering translation: $text")
                        lastClipboardText = text
                        viewModel.onClipboardTextFound(text)
                    } else {
                        Log.d("MainActivity", "⊘ Skipping clipboard text - no new content or duplicate")
                    }
                } else {
                    Log.d("MainActivity", "Clipboard item is null or empty")
                }
            } else {
                Log.d("MainActivity", "No primary clip in clipboard")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Clipboard error: ${e.message}", e)
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DictoTheme {
        Greeting("Android")
    }
}