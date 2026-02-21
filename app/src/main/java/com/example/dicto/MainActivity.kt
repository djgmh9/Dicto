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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.dicto.domain.manager.FloatingWindowManager
import com.example.dicto.presentation.screens.settings.SettingsViewModel
import com.example.dicto.presentation.screens.translator.TranslatorViewModel
import com.example.dicto.ui.components.AppBottomNavigation
import com.example.dicto.ui.screens.DictionaryScreen
import com.example.dicto.ui.theme.DictoTheme
import com.example.dicto.utils.AppLogger
import com.example.dicto.utils.clipboard.ClipboardMonitoringManager
import com.example.dicto.utils.logging.FloatingWindowLogger
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity - Application entry point
 *
 * Responsibilities:
 * - Initialize and configure the activity
 * - Enable edge-to-edge layout
 * - Set the theme and compose content
 * - Log app lifecycle events
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var floatingWindowManager: FloatingWindowManager? = null
    private var floatingWindowPreferenceEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FloatingWindowLogger.mainActivityOnCreate()

        floatingWindowManager = FloatingWindowManager(this)

        enableEdgeToEdge()

        setContent {
            DictoTheme {
                MainContent { enabled ->
                    // Callback to receive preference updates from Compose
                    FloatingWindowLogger.preferenceCallbackReceived(enabled)
                    floatingWindowPreferenceEnabled = enabled
                    FloatingWindowLogger.preferenceUpdated(enabled)
                }
            }
        }

        // ==================== LIFECYCLE HANDLING ====================
        // Use DefaultLifecycleObserver for lifecycle events (Google recommended approach)
        // This replaces the old onStart(), onResume(), onPause(), onStop(), onDestroy() overrides
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                FloatingWindowLogger.mainActivityOnStart()
            }

            override fun onResume(owner: LifecycleOwner) {
                FloatingWindowLogger.mainActivityOnResume()
                // Always hide floating button when inside Dicto
                floatingWindowManager?.stopFloatingWindow()
                FloatingWindowLogger.mainActivityOnResumeStopFloatingWindow()
            }

            override fun onPause(owner: LifecycleOwner) {
                FloatingWindowLogger.mainActivityOnPause()
                // Show floating button when leaving Dicto - but only if preference is enabled
                if (floatingWindowPreferenceEnabled) {
                    FloatingWindowLogger.mainActivityOnPauseShowButton()
                    floatingWindowManager?.startFloatingWindow()
                } else {
                    FloatingWindowLogger.warn("Floating window preference is DISABLED, NOT showing button")
                }
            }

            override fun onStop(owner: LifecycleOwner) {
                FloatingWindowLogger.mainActivityOnStop()
            }
        })
    }

    // ==================== LIFECYCLE METHODS REMOVED ====================
    // The following methods have been removed and replaced with a LifecycleObserver
    // added in onCreate():
    //
    // ❌ override fun onStart()
    // ❌ override fun onResume()
    // ❌ override fun onPause()
    // ❌ override fun onStop()
    // ❌ override fun onDestroy()
    //
    // Why? Google Architecture Recommendations state:
    // "Do not override lifecycle methods in Activities or Fragments.
    //  Use DefaultLifecycleObserver instead."
    //
    // Benefits of this approach:
    // • Cleaner separation of concerns
    // • Easier to test (observer logic separated from Activity)
    // • Follows modern Android best practices
    // • Clearer code organization
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

    // Navigation state
    var selectedTab by remember { mutableIntStateOf(0) }

    // Create ViewModels for each screen feature
    val translatorViewModel: TranslatorViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()

    // Observe floating window preference from settings view model
    val floatingWindowEnabled by settingsViewModel.floatingWindowEnabled.collectAsState()

    // Floating window manager for restoring state on app launch
    val floatingWindowManager = remember { FloatingWindowManager(context) }

    // Notify MainActivity of preference changes
    LaunchedEffect(floatingWindowEnabled) {
        FloatingWindowLogger.mainContentLaunchedEffect(floatingWindowEnabled)
        onFloatingWindowPreferenceChanged(floatingWindowEnabled)
        FloatingWindowLogger.mainContentCallbackInvoked(floatingWindowEnabled)
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
                settingsViewModel.toggleFloatingWindow()
            }
        } else {
            AppLogger.logServiceState("FloatingWindow", "DISABLED", "Preference is off")
        }
    }

    // Get clipboard monitoring preference
    val clipboardMonitoringEnabled by settingsViewModel.clipboardMonitoringEnabled.collectAsState()

    // Manage clipboard monitoring lifecycle for translator screen only
    // Only create the manager when user is on translator tab
    if (selectedTab == 0 && clipboardMonitoringEnabled) {
        ClipboardMonitoringManager(
            context = context,
            lifecycleOwner = lifecycleOwner,
            viewModel = translatorViewModel,
            selectedTab = selectedTab,
            isEnabled = clipboardMonitoringEnabled
        )
    }

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
            translatorViewModel = translatorViewModel,
            settingsViewModel = settingsViewModel
        )
    }
}

