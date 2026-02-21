package com.example.dicto.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// Brand / Primary — Blue-Teal (Google Translate-inspired)
// ============================================================================
val Brand500 = Color(0xFF1A73E8)   // Google Blue — primary actions, icons
val Brand400 = Color(0xFF4285F4)   // Lighter blue — hover / pressed states
val Brand600 = Color(0xFF1557B0)   // Darker blue — dark mode primary
val Brand100 = Color(0xFFD2E3FC)   // Very light blue — containers in light mode
val Brand900 = Color(0xFF0D47A1)   // Deep navy — containers in dark mode

// ============================================================================
// Light Mode Surface Stack
// ============================================================================
val LightBackground  = Color(0xFFF8F9FA)   // Google light grey — page background
val LightSurface     = Color(0xFFFFFFFF)   // Pure white — cards / nav bar
val LightSurface2    = Color(0xFFF1F3F4)   // Subtle grey — input fields, chips
val LightOutline     = Color(0xFFDFE1E5)   // Dividers & borders

// ============================================================================
// Light Mode Text
// ============================================================================
val LightOnBackground = Color(0xFF202124)  // Near-black — primary text
val LightOnSurface    = Color(0xFF202124)  // Near-black — card primary text
val LightOnSurface2   = Color(0xFF5F6368)  // Mid-grey  — secondary / captions

// ============================================================================
// Dark Mode Surface Stack (AMOLED-optimised)
// ============================================================================
val DarkBackground    = Color(0xFF000000)  // True black — AMOLED saving
val DarkSurface       = Color(0xFF111111)  // Cards / nav bar
val DarkSurface2      = Color(0xFF1C1C1C)  // Input fields, chips, elevated cards
val DarkOutline       = Color(0xFF2D2D2D)  // Subtle dividers

// ============================================================================
// Dark Mode Text
// ============================================================================
val DarkOnBackground  = Color(0xFFE8EAED)  // Soft white — primary text
val DarkOnSurface     = Color(0xFFE8EAED)  // Soft white — card text
val DarkOnSurface2    = Color(0xFF9AA0A6)  // Mid-grey   — secondary / captions

// ============================================================================
// Semantic — same tokens work in both modes via colorScheme
// ============================================================================
val ErrorRed          = Color(0xFFD93025)  // Google red
val ErrorRedContainer = Color(0xFFFCE8E6)
val ErrorRedDark      = Color(0xFFEA4335)
val ErrorRedContainerDark = Color(0xFF3B1212)
