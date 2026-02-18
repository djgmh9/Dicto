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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dicto.DictionaryViewModel
import com.example.dicto.MainActivity
import com.example.dicto.ui.theme.DictoTheme

/**
 * FloatingTranslatorActivity - Displays translator as overlay on top of other apps
 *
 * Lightweight activity that shows translator interface as transparent overlay
 * Appears on top of current app without taking it out of focus
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
                    var shouldClose by remember { mutableStateOf(false) }

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

    override fun onBackPressed() {
        Log.d("FloatingTranslatorActivity", "Back pressed")
        finish()
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("FloatingTranslatorActivity", "Activity destroyed - restoring floating button")
        // Send broadcast to service to restore floating button
        val intent = Intent("com.example.dicto.RESTORE_FLOATING_BUTTON")
        sendBroadcast(intent)
    }
}



