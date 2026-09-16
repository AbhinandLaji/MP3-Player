package com.example.smartshuffle.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.activeTrackCyberBorder(
    borderColor: Color = Color(0xFFFF2A4D),
    backgroundColor: Color = Color(0xFFFF2A4D).copy(alpha = 0.10f),
    cutSize: Dp = 10.dp,
    strokeWidth: Dp = 1.5.dp
): Modifier = this.then(
    Modifier.drawBehind {
        val cut = cutSize.toPx()
        val stroke = strokeWidth.toPx()
        val w = size.width
        val h = size.height

        // 1. Octagonal Chamfered Path
        val borderPath = Path().apply {
            moveTo(cut, 0f)
            lineTo(w - cut, 0f)
            lineTo(w, cut)
            lineTo(w, h - cut)
            lineTo(w - cut, h)
            lineTo(cut, h)
            lineTo(0f, h - cut)
            lineTo(0f, cut)
            close()
        }

        // Fill translucent background
        drawPath(path = borderPath, color = backgroundColor)

        // Outer border stroke
        drawPath(
            path = borderPath,
            color = borderColor,
            style = Stroke(width = stroke)
        )

        // 2. Corner Accent Notches (Top-Left and Bottom-Right diagonal ticks)
        val tickOffset = cut * 0.45f
        val tickLength = cut * 0.7f

        // Top-Left accent tick
        drawLine(
            color = borderColor,
            start = Offset(tickOffset, cut),
            end = Offset(cut, tickOffset),
            strokeWidth = stroke
        )

        // Bottom-Right accent tick
        drawLine(
            color = borderColor,
            start = Offset(w - cut, h - tickOffset),
            end = Offset(w - tickOffset, h - cut),
            strokeWidth = stroke
        )
    }
)
