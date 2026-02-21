package com.example.dicto.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.example.dicto.ui.theme.BottomNavShape

/**
 * AppBottomNavigation - Reusable bottom navigation bar
 *
 * Single Responsibility: Display bottom navigation tabs
 * Features:
 * - 3 tabs: Translator, Saved Words, Settings
 * - Visual feedback for selected tab
 * - Customizable tab selection callback
 * - Pill-shaped design with rounded top corners
 * - Elevated appearance
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
    Surface(
        modifier = Modifier
            .shadow(
                elevation = 0.dp,
                shape = BottomNavShape,
                clip = false
            )
            .clip(BottomNavShape),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        NavigationBar(
            modifier = Modifier.padding(horizontal = 16.dp),
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            tonalElevation = 0.dp
        ) {
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
}

