package com.example.smartshuffle.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.smartshuffle.R

val Orbitron = FontFamily(Font(R.font.orbitron))
val ChakraPetch = FontFamily(Font(R.font.chakra_petch))

val CyberTypography = Typography(
    displayLarge = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Normal, fontSize = 57.sp),
    headlineLarge = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Normal, fontSize = 32.sp),
    titleLarge = TextStyle(fontFamily = Orbitron, fontWeight = FontWeight.Medium, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = ChakraPetch, fontWeight = FontWeight.Medium, fontSize = 16.sp),
    bodyLarge = TextStyle(fontFamily = ChakraPetch, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    labelLarge = TextStyle(fontFamily = ChakraPetch, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelSmall = TextStyle(
        fontFamily = Orbitron,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 2.sp // Wide tracking for CP2077 HUD style
    )
)
