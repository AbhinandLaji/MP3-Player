package com.example.smartshuffle.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun SmartShuffleTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CyberDarkScheme,
        typography = CyberTypography,
        content = content
    )
}
