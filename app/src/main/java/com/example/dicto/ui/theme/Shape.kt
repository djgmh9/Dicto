package com.example.dicto.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * App-wide shape definitions for Material Design 3
 *
 * - SearchBarShape: Google-style rounded search bar (28dp corners)
 * - BottomNavShape: Pill-shaped bottom navigation bar
 */

val SearchBarShape = RoundedCornerShape(28.dp)
val BottomNavShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

