package com.example.smartshuffle.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Alignment
import com.example.smartshuffle.ui.theme.CutCornerShape6
import kotlinx.coroutines.launch
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
            style = MaterialTheme.typography.labelLarge.copy(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = color,
                    blurRadius = 12f
                )
            ),
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 12.dp)
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

@Composable
fun EqBars(isPlaying: Boolean, color: Color = MaterialTheme.colorScheme.primary) {
    val heights = remember { List(4) { Animatable(0.3f) } }
    
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            heights.forEachIndexed { i, anim ->
                launch {
                    while (true) {
                        anim.animateTo(Random.nextFloat(), tween(120 + i * 40))
                    }
                }
            }
        } else {
            // Snap back to a low resting state when paused
            heights.forEach { launch { it.animateTo(0.2f, tween(300)) } }
        }
    }
    
    androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.height(24.dp)) {
        heights.forEach { h ->
            Box(
                Modifier
                    .width(4.dp)
                    .fillMaxHeight(h.value)
                    .background(color)
            )
            androidx.compose.foundation.layout.Spacer(Modifier.width(2.dp))
        }
    }
}

@Composable
fun NeonIconButton(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    iconSize: Float = 32f,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CutCornerShape6, 
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        modifier = modifier.size(64.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = color,
            modifier = Modifier
                .padding(16.dp)
        )
    }
}
