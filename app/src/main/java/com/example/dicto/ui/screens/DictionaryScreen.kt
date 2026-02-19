package com.example.dicto.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dicto.presentation.screens.DictionaryViewModel

/**
 * DictionaryScreen - Main container for navigation between tabs
 *
 * Follows separation of concerns:
 * - Handles tab navigation logic only
 * - Delegates to specific content screens
 * - Does not handle business logic or screen content
 *
 * Tab Structure:
 * - Tab 0: Translator (input & translation)
 * - Tab 1: Saved Words (vocabulary library)
 * - Tab 2: Settings (app settings)
 */
@Composable
fun DictionaryScreen(
    modifier: Modifier = Modifier,
    selectedTab: Int,
    viewModel: DictionaryViewModel = viewModel()
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (selectedTab) {
            0 -> TranslatorContent(viewModel)
            1 -> SavedWordsContent(viewModel)
            2 -> SettingsContent(viewModel)
        }
    }
}

