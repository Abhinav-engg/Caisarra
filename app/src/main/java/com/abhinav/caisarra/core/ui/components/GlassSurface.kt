package com.abhinav.caisarra.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import com.abhinav.caisarra.core.ui.theme.GlassBorderWidth
import com.abhinav.caisarra.core.ui.theme.GlassShadowElevation
import com.caisaara.ui.theme.GlassHighlight
import com.caisaara.ui.theme.GlassShadow
import com.caisaara.ui.theme.GlassTint
import com.caisaara.ui.theme.PuzzleDarkCard

fun Modifier.glassSurface(
    shape: Shape,
    tint: Color = GlassTint,
    base: Color = PuzzleDarkCard
): Modifier {
    val highlight = Brush.verticalGradient(listOf(GlassHighlight, Color.Transparent))
    return this
        .shadow(GlassShadowElevation, shape, ambientColor = GlassShadow, spotColor = GlassShadow)
        .background(base, shape)
        .background(tint, shape)
        .background(highlight, shape)
        .border(GlassBorderWidth, highlight, shape)
}
