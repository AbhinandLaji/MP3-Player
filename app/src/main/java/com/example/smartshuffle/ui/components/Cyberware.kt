package com.example.smartshuffle.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.smartshuffle.ui.theme.CutCornerShape6
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun NeonButton(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary, // Defaults to NeonBlue
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CutCornerShape6,
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color),
        modifier = modifier
    ) {
        Text(
            text = text.uppercase(),
            color = color,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 12.dp)
                // The magic neon glow on the text itself
                .graphicsLayer { shadowRadius = 12f } 
        )
    }
}

@Composable
fun GlitchText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    var trigger by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(Random.nextLong(2500, 6000)) // Glitch every 2-6 seconds
            trigger++
        }
    }
    
    val offsetX by animateDpAsState(
        targetValue = if (trigger % 2 == 0) 0.dp else 3.dp,
        animationSpec = tween(50), // Ultra-fast snap
        label = "glitch_offset"
    )
    
    Text(
        text = text.uppercase(),
        color = color,
        modifier = modifier.offset(x = offsetX),
        style = MaterialTheme.typography.headlineLarge // Uses Orbitron
    )
}

@Composable
fun CyberpunkOverlay(modifier: Modifier = Modifier) {
    // Grab the primary color, but we will make it ultra-transparent
    val scanlineColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val spacing = 4.dp.toPx()
        var y = 0f
        
        while (y < size.height) {
            drawLine(
                color = scanlineColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            y += spacing
        }
    }
}
