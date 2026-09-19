package com.example.smartshuffle.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
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

fun Modifier.cyberGridCardBorder(
    primaryColor: Color = Color(0xFFFF2A4D), // NeonRed
    cutSize: Dp = 12.dp,
    strokeWidth: Dp = 1.5.dp
): Modifier = this.then(
    Modifier.drawWithContent {
        // Draw album art and inner contents first
        drawContent()

        val cut = cutSize.toPx()
        val stroke = strokeWidth.toPx()
        val w = size.width
        val h = size.height

        // 1. Chamfered Perimeter Path
        val framePath = Path().apply {
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

        // Draw thin outer boundary
        drawPath(
            path = framePath,
            color = primaryColor.copy(alpha = 0.85f),
            style = Stroke(width = stroke)
        )

        // 2. Top Inset Trapezoid Tab
        val tabWidth = w * 0.44f
        val tabHeight = 4.dp.toPx()
        val tabStartX = (w - tabWidth) / 2f
        val tabInset = 3.dp.toPx()

        val topTabPath = Path().apply {
            moveTo(tabStartX, 0f)
            lineTo(tabStartX + tabWidth, 0f)
            lineTo(tabStartX + tabWidth - tabInset, tabHeight)
            lineTo(tabStartX + tabInset, tabHeight)
            close()
        }
        drawPath(path = topTabPath, color = primaryColor)

        // 3. Bottom Inset Trapezoid Tab
        val bottomTabPath = Path().apply {
            moveTo(tabStartX + tabInset, h - tabHeight)
            lineTo(tabStartX + tabWidth - tabInset, h - tabHeight)
            lineTo(tabStartX + tabWidth, h)
            lineTo(tabStartX, h)
            close()
        }
        drawPath(path = bottomTabPath, color = primaryColor)

        // 4. Tech Pip Accent (Middle-Left edge)
        drawCircle(
            color = primaryColor,
            radius = 1.8.dp.toPx(),
            center = Offset(stroke * 1.5f, h * 0.5f)
        )
    }
)
