package com.example.dicto.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.dicto.DictionaryViewModel
import com.example.dicto.ui.components.TranslatorUI

/**
 * TranslatorContent - Main translator interface for the app's translator tab
 *
 * Single Responsibility: Provide translator UI for main tab
 * DRY Principle: Delegates to reusable TranslatorUI component
 *
 * This is a thin wrapper that uses the shared TranslatorUI component.
 */
@Composable
fun TranslatorContent(viewModel: DictionaryViewModel) {
    TranslatorUI(viewModel = viewModel)
}

