package com.example.smartshuffle.ui.theme

import androidx.compose.foundation.shape.GenericShape

val CutCornerShape6 = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    val cut = 16f // Adjust this value to make the cut more or less aggressive

    moveTo(cut, 0f)
    lineTo(w, 0f)
    lineTo(w, h - cut)
    lineTo(w - cut, h)
    lineTo(0f, h)
    lineTo(0f, cut)
    close()
}
