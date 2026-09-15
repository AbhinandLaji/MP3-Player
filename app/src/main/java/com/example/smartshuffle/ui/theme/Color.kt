package com.example.smartshuffle.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

// ── Cyberpunk Canvas ──
val NeonBg = Color(0xFF070B14)
val NeonSurface = Color(0xFF0D1420)
val NeonSurfaceHi = Color(0xFF16202F)
val GridLine = Color(0x14FF2A4D)

// ── Blue: Primary / Active ──
val NeonBlue = Color(0xFF00E5FF)
val NeonBlueDim = Color(0xFF0090A8)
val BlueOnDark = Color(0xFFB3F4FF)

// ── Red: Alert / Destructive ──
val NeonRed = Color(0xFFFF2A4D)
val NeonRedDim = Color(0xFFB3122E)
val RedOnDark = Color(0xFFFFB3C0)

// ── Text ──
val TextPrimary = Color(0xFFE8F6FF)
val TextSecondary = Color(0xFF7E93A8)

val CyberDarkScheme = darkColorScheme(
    primary = NeonBlue,
    onPrimary = Color(0xFF00232A),
    primaryContainer = NeonBlueDim,
    onPrimaryContainer = BlueOnDark,
    secondary = NeonRed, // Danger slot
    onSecondary = Color(0xFF3A0008),
    secondaryContainer = NeonRedDim,
    onSecondaryContainer = RedOnDark,
    tertiary = NeonRed,
    background = NeonBg,
    surface = NeonSurface,
    surfaceVariant = NeonSurfaceHi,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = TextSecondary.copy(alpha = 0.4f),
    error = NeonRed,
    onError = Color(0xFF3A0008)
)
