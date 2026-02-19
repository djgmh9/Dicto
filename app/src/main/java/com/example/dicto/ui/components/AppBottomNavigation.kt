package com.example.dicto.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

/**
 * AppBottomNavigation - Reusable bottom navigation bar
 *
 * Single Responsibility: Display bottom navigation tabs
 * Features:
 * - 3 tabs: Translator, Saved Words, Settings
 * - Visual feedback for selected tab
 * - Customizable tab selection callback
 *
 * Parameters:
 * - selectedTab: Currently selected tab (0=Translator, 1=Saved, 2=Settings)
 * - onTabSelected: Callback when user selects a tab
 */
@Composable
fun AppBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Translator") },
            label = { Text("Translator") },
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Favorite, contentDescription = "Saved Words") },
            label = { Text("Saved") },
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) }
        )
    }
}

