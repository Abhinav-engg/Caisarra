package com.abhinav.caisarra.core.ui.backgrounds

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

fun Modifier.focusGlow(focused: Boolean, shape: Shape, color: Color): Modifier =
    if (focused) {
        this.shadow(
            elevation = 12.dp,
            shape = shape,
            ambientColor = color,
            spotColor = color
        )
    } else this