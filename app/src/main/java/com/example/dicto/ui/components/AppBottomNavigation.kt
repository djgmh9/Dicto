package com.example.dicto.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
 * - Flat design without shadow
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
        modifier = Modifier.clip(BottomNavShape),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        NavigationBar(
            modifier = Modifier.padding(horizontal = 16.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            val items = listOf(
                Triple(0, Icons.Rounded.Home, "Translator"),
                Triple(1, Icons.Rounded.BookmarkAdd, "Saved"),
                Triple(2, Icons.Rounded.Settings, "Settings"),
            )
            items.forEach { (index, icon, label) ->
                NavigationBarItem(
                    icon = { Icon(icon, contentDescription = label) },
                    label = { Text(label) },
                    selected = selectedTab == index,
                    onClick = { onTabSelected(index) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    }
}
