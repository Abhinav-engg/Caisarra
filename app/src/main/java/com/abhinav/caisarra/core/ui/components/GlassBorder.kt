package com.abhinav.caisarra.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.caisaara.ui.theme.CardBorderNavy
import com.caisaara.ui.theme.CyanLight
import com.caisaara.ui.theme.White

val GlassBorderStroke = BorderStroke(
    width = 1.5.dp,
    brush = Brush.linearGradient(
        0f to CyanLight.copy(alpha = 0.5f),
        0.5f to CardBorderNavy.copy(alpha = 0.2f),
        1f to White.copy(alpha = 0.1f)
    )
)

fun Modifier.glassBorder(shape: Shape): Modifier = border(GlassBorderStroke, shape)
