package com.example.dicto

import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
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

/**
 * MainActivity - Application entry point
 *
 * Responsibilities:
 * - Initialize and configure the activity
 * - Enable edge-to-edge layout
 * - Set the theme and compose content
 * - Check and request runtime permissions for floating window
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
 * MainContent - Root composable for app navigation and layout
 *
 * Responsibilities:
 * - Manage tab navigation state
 * - Provide Scaffold layout with bottom navigation
 * - Coordinate clipboard monitoring
 * - Delegate to screen content based on selected tab
 * - Request runtime permission for floating window
 */
@Composable
private fun MainContent() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val viewModel: DictionaryViewModel = viewModel()

    // Navigation state
    var selectedTab by remember { mutableIntStateOf(0) }

    // Observe clipboard monitoring preference
    val clipboardMonitoringEnabled by viewModel.clipboardMonitoringEnabled.collectAsState()

    // Observe floating window preference for app startup restoration
    val floatingWindowEnabled by viewModel.floatingWindowEnabled.collectAsState()

    // Floating window manager for restoring state on app launch
    val floatingWindowManager = remember { FloatingWindowManager(context) }

    // Restore floating window state on app launch with permission check
    LaunchedEffect(Unit) {
        if (floatingWindowEnabled) {
            // Check if we have permission to draw over other apps
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(context)) {
                    // Permission granted, start floating window
                    floatingWindowManager.startFloatingWindow()
                    Log.d("MainActivity", "Floating window permission granted, starting service")
                } else {
                    // Permission not granted, disable the preference
                    viewModel.toggleFloatingWindow()
                    Log.w("MainActivity", "Floating window permission not granted, disabling feature")
                }
            } else {
                // Android versions before M don't require this permission
                floatingWindowManager.startFloatingWindow()
            }
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

