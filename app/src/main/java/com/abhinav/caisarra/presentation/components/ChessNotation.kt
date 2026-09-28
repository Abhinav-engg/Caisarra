package com.abhinav.caisarra.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp

@Composable
fun ChessNotation(
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    Canvas(modifier = modifier
    ) {
        val notationColor = Color(0xFF00606A).copy(alpha = 0.30f)
        val notationStyle = TextStyle(color = notationColor, fontSize = 14.sp)
        drawText(textMeasurer = textMeasurer,
            text = "1. e4",
            topLeft = Offset(
                x = size.width * 0.62f,
                y = size.height * 0.08f
            ),
            style = notationStyle
        )
        drawText(textMeasurer = textMeasurer,
            text = "e5",
            topLeft = Offset(
                x = size.width * 0.88f,
                y = size.height * 0.08f
            ),
            style = notationStyle
        )
        val bottomNotationY = size.height * 0.90f
        drawText(textMeasurer = textMeasurer,
            text = "1. e4",
            topLeft = Offset(
                x = size.width * 0.06f,
                y = bottomNotationY
            ),
            style = notationStyle
        )

        drawText(textMeasurer = textMeasurer,
            text = "e5",
            topLeft = Offset(
                x = size.width * 0.35f,
                y = bottomNotationY
            ),
            style = notationStyle
        )

        drawText(
            textMeasurer = textMeasurer,
            text = "2. Nf3",
            topLeft = Offset(
                x = size.width * 0.63f,
                y = bottomNotationY
            ),
            style = notationStyle
        )

        drawText(textMeasurer = textMeasurer,
            text = "Nc6",
            topLeft = Offset(
                x = size.width * 0.88f,
                y = bottomNotationY
            ),
            style = notationStyle
        )
    }
}