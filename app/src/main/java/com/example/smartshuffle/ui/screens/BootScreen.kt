package com.example.smartshuffle.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random
import com.example.smartshuffle.ui.theme.*
import com.example.smartshuffle.ui.components.CyberpunkOverlay

@Composable
fun CyberAudioBootScreen(onFinished: () -> Unit) {
    var isBooting by remember { mutableStateOf(false) }

    // 1. Smooth, continuous progress (exactly 2 seconds)
    val progress by animateFloatAsState(
        targetValue = if (isBooting) 1f else 0f,
        animationSpec = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
        label = "boot_progress"
    )

    // 2. Smooth breathing glow for the logo
    val glowAlpha by rememberInfiniteTransition(label = "glow").animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "alpha"
    )

    // Trigger the animation on launch, wait for it to finish, then hand off
    LaunchedEffect(Unit) {
        isBooting = true
        delay(2200) // Slightly longer than the tween so it breathes for a moment
        onFinished()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(NeonBg),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            
            // --- THE LOGO (Breathing, not glitching) ---
            Text(
                text = "SYNTH-DECK",
                fontFamily = Orbitron,
                fontSize = 36.sp,
                color = NeonBlue.copy(alpha = glowAlpha),
                modifier = Modifier.graphicsLayer { 
                    // Using text shadow instead of graphicsLayer shadowRadius!
                },
                style = androidx.compose.ui.text.TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = NeonBlue,
                        blurRadius = 16f * glowAlpha
                    )
                )
            )

            Spacer(Modifier.height(12.dp))

            // --- AUDIO STATUS TEXT ---
            Text(
                text = "SYNCING AUDIO CHANNELS...",
                fontFamily = ChakraPetch,
                fontSize = 12.sp,
                color = TextSecondary,
                letterSpacing = 4.sp
            )

            Spacer(Modifier.height(48.dp))

            // --- SMOOTH NEON TUBE PROGRESS ---
            Box(
                Modifier
                    .fillMaxWidth(0.65f)
                    .height(2.dp) // Ultra-thin, sleek line
                    .background(NeonSurfaceHi)
            ) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(
                            Brush.horizontalGradient(
                                // Fades from blue to a hot red tip
                                colors = listOf(NeonBlueDim, NeonBlue, NeonRed) 
                            )
                        )
                        // Note: For a Box background, graphicsLayer shadow might need a shape or we use a custom drawBehind.
                        // I will omit the graphicsLayer shadowRadius=8f here since we learned it doesn't work well directly on raw Box without shape.
                )
            }

            Spacer(Modifier.height(32.dp))

            // --- SMOOTH WARM-UP EQUALIZER ---
            // The bars expand outward and bounce as the progress increases
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                val numBars = 11
                for (i in 0 until numBars) {
                    // Create a bell curve effect so center bars are taller
                    val distanceFromCenter = Math.abs(i - (numBars / 2))
                    val maxHeight = 1f - (distanceFromCenter * 0.15f)
                    
                    // Bars only "wake up" as the progress sweeps past them
                    val barWakeUpThreshold = i / numBars.toFloat()
                    val isActive = progress > barWakeUpThreshold
                    
                    val heightModifier = if (isActive) {
                        // Smooth random pulse once active
                        val bounce by rememberInfiniteTransition(label = "eq").animateFloat(
                            initialValue = maxHeight * 0.4f,
                            targetValue = maxHeight,
                            animationSpec = infiniteRepeatable(tween(Random.nextInt(300, 600)), RepeatMode.Reverse),
                            label = "height"
                        )
                        bounce
                    } else {
                        0.1f // Resting state
                    }

                    Box(
                        Modifier
                            .width(4.dp)
                            .fillMaxHeight(heightModifier)
                            .background(if (isActive) NeonBlue else NeonSurfaceHi)
                    )
                }
            }
        }

        // Keep the CRT scanlines so it still feels like a Cyberpunk display
        CyberpunkOverlay()
    }
}
