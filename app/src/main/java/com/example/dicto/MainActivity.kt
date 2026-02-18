package com.example.dicto

import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dicto.domain.FloatingWindowManager
import com.example.dicto.ui.AppBottomNavigation
import com.example.dicto.ui.theme.DictoTheme
import com.example.dicto.utils.AppLogger

/**
 * MainActivity - Application entry point
 *
 * Responsibilities:
 * - Initialize and configure the activity
 * - Enable edge-to-edge layout
 * - Set the theme and compose content
 * - Log app lifecycle events
 */
class MainActivity : ComponentActivity() {

    private var floatingWindowManager: FloatingWindowManager? = null
    private var floatingWindowPreferenceEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.util.Log.d("DICTO_FLOATING", ">>> MainActivity.onCreate - App starting, floatingWindowPreferenceEnabled=$floatingWindowPreferenceEnabled")

        floatingWindowManager = FloatingWindowManager(this)

        enableEdgeToEdge()

        setContent {
            DictoTheme {
                MainContent { enabled ->
                    // Callback to receive preference updates from Compose
                    android.util.Log.d("DICTO_FLOATING", ">>> Preference callback received: enabled=$enabled")
                    floatingWindowPreferenceEnabled = enabled
                    android.util.Log.d("DICTO_FLOATING", ">>> Updated floatingWindowPreferenceEnabled to $floatingWindowPreferenceEnabled")
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        android.util.Log.d("DICTO_FLOATING", ">>> MainActivity.onStart - floatingWindowPreferenceEnabled=$floatingWindowPreferenceEnabled")
    }

    override fun onResume() {
        super.onResume()
        android.util.Log.d("DICTO_FLOATING", ">>> MainActivity.onResume - App returned to foreground, hiding floating button, floatingWindowPreferenceEnabled=$floatingWindowPreferenceEnabled")
        // Always hide floating button when inside Dicto
        floatingWindowManager?.stopFloatingWindow()
        android.util.Log.d("DICTO_FLOATING", ">>> MainActivity.onResume - stopFloatingWindow called")
    }

    override fun onPause() {
        super.onPause()
        android.util.Log.d("DICTO_FLOATING", ">>> MainActivity.onPause - App going to background, floatingWindowPreferenceEnabled=$floatingWindowPreferenceEnabled")
        // Show floating button when leaving Dicto - but only if preference is enabled
        if (floatingWindowPreferenceEnabled) {
            android.util.Log.d("DICTO_FLOATING", ">>> MainActivity.onPause - Preference is ENABLED, calling startFloatingWindow()")
            floatingWindowManager?.startFloatingWindow()
            android.util.Log.d("DICTO_FLOATING", ">>> MainActivity.onPause - startFloatingWindow called")
        } else {
            android.util.Log.d("DICTO_FLOATING", ">>> MainActivity.onPause - Preference is DISABLED, NOT showing button")
        }
    }

    override fun onStop() {
        super.onStop()
        android.util.Log.d("DICTO_FLOATING", ">>> MainActivity.onStop - App stopped")
    }

    override fun onDestroy() {
        super.onDestroy()
        android.util.Log.d("DICTO_FLOATING", ">>> MainActivity.onDestroy - App being destroyed")
    }
}

/**
 * MainContent - Root composable for app navigation and layout
 *
 * Responsibilities:
 * - Manage tab navigation state
 * - Provide Scaffold layout with bottom navigation
 * - Coordinate clipboard monitoring
 * - Delegate to screen content based on selected tab
 * - Request runtime permission for floating window
 * - Notify parent activity of preference changes
 */
@Composable
private fun MainContent(onFloatingWindowPreferenceChanged: (Boolean) -> Unit = {}) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val viewModel: DictionaryViewModel = viewModel()

    // Navigation state
    var selectedTab by remember { mutableIntStateOf(0) }

    // Observe clipboard monitoring preference
    val clipboardMonitoringEnabled by viewModel.clipboardMonitoringEnabled.collectAsState()

    // Observe floating window preference
    val floatingWindowEnabled by viewModel.floatingWindowEnabled.collectAsState()

    // Floating window manager for restoring state on app launch
    val floatingWindowManager = remember { FloatingWindowManager(context) }

    // Notify MainActivity of preference changes
    LaunchedEffect(floatingWindowEnabled) {
        android.util.Log.d("DICTO_FLOATING", ">>> MainContent LaunchedEffect triggered: floatingWindowEnabled=$floatingWindowEnabled")
        onFloatingWindowPreferenceChanged(floatingWindowEnabled)
        android.util.Log.d("DICTO_FLOATING", ">>> MainContent callback invoked with enabled=$floatingWindowEnabled")
    }

    // Restore floating window state on app launch with permission check
    LaunchedEffect(Unit) {
        AppLogger.logAppEvent("MainContent.LaunchedEffect", "Checking floating window state on startup")

        if (floatingWindowEnabled) {
            // Check if we have permission to draw over other apps
            if (Settings.canDrawOverlays(context)) {
                // Permission granted, start floating window
                AppLogger.logServiceState("FloatingWindow", "STARTING", "Permission granted")
                floatingWindowManager.startFloatingWindow()
            } else {
                // Permission not granted, disable the preference
                AppLogger.logServiceState("FloatingWindow", "BLOCKED", "Permission denied")
                viewModel.toggleFloatingWindow()
            }
        } else {
            AppLogger.logServiceState("FloatingWindow", "DISABLED", "Preference is off")
        }
    }

    // Manage clipboard monitoring lifecycle
    ClipboardMonitoringManager(
        context = context,
        lifecycleOwner = lifecycleOwner,
        viewModel = viewModel,
        selectedTab = selectedTab,
        isEnabled = clipboardMonitoringEnabled
    )

    // Main layout with Scaffold and bottom navigation
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            AppBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        DictionaryScreen(
            modifier = Modifier.padding(innerPadding),
            selectedTab = selectedTab,
            viewModel = viewModel
        )
    }
}

