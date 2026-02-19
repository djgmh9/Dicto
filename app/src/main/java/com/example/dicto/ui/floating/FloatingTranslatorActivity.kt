package com.example.dicto.ui.floating

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dicto.presentation.screens.DictionaryViewModel
import com.example.dicto.ui.theme.DictoTheme
import com.example.dicto.utils.ClipboardMonitoringManager

/**
 * FloatingTranslatorActivity - Displays translator as overlay on top of other apps
 *
 * Features:
 * - Transparent overlay that shows translator interface
 * - Auto-translate from clipboard (respects user preference)
 * - Lifecycle-aware clipboard monitoring
 * - Appears on top of current app without taking it out of focus
 */
class FloatingTranslatorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("FloatingTranslatorActivity", "Activity created")

        // Make activity transparent and prevent focus
        window.setBackgroundDrawableResource(android.R.color.transparent)

        enableEdgeToEdge()

        setContent {
            DictoTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent),
                    color = Color.Transparent
                ) {
                    val viewModel: DictionaryViewModel = viewModel()
                    val context = LocalContext.current
                    val lifecycleOwner = LocalLifecycleOwner.current
                    var shouldClose by remember { mutableStateOf(false) }

                    // Get clipboard monitoring preference
                    val clipboardMonitoringEnabled by viewModel.clipboardMonitoringEnabled.collectAsState()

                    // Enable clipboard monitoring for floating translator
                    ClipboardMonitoringManager(
                        context = context,
                        lifecycleOwner = lifecycleOwner,
                        viewModel = viewModel,
                        selectedTab = 0, // Always monitor in floating translator
                        isEnabled = clipboardMonitoringEnabled
                    )

                    if (!shouldClose) {
                        FloatingTranslatorOverlay(
                            viewModel = viewModel,
                            onDismiss = {
                                Log.d("FloatingTranslatorActivity", "Closing overlay")
                                shouldClose = true
                                // Close gracefully without restarting Dicto
                                finish()
                            }
                        )
                    }
                }
            }
        }
    }

    /**
     * Called when user presses home or recent buttons
     * This ensures floating button is restored when user leaves the overlay
     */
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        Log.d("FloatingTranslatorActivity", "User pressed home/recent - closing overlay")
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("FloatingTranslatorActivity", "Activity destroyed - restoring floating button")
        // Send broadcast to service to restore floating button
        val intent = Intent("com.example.dicto.RESTORE_FLOATING_BUTTON")
        sendBroadcast(intent)
    }
}
