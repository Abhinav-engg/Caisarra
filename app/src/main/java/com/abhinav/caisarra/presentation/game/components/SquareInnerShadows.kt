package com.abhinav.caisarra.presentation.game.components

import android.graphics.BlurMaskFilter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import com.abhinav.caisarra.presentation.game.model.InnerShadow

private const val DESIGN_SQUARE_SIZE = 25.35f

fun Modifier.squareInnerShadows(shadows: List<InnerShadow>): Modifier = if (shadows.isEmpty()) this else drawBehind {
    val scale = size.width / DESIGN_SQUARE_SIZE
    clipRect {
        drawIntoCanvas { canvas ->
            shadows.forEach { shadow ->
                val blur = shadow.blur * scale
                val inner = Rect(0f, shadow.offsetY * scale, size.width, size.height + shadow.offsetY * scale)
                val path = Path().apply {
                    fillType = PathFillType.EvenOdd
                    addRect(inner.inflate(blur * 2 + size.height))
                    addRect(inner)
                }
                val paint = Paint().apply {
                    color = shadow.color
                    asFrameworkPaint().maskFilter = BlurMaskFilter((blur / 2 - 0.5f).coerceAtLeast(0.1f) / 0.57735f, BlurMaskFilter.Blur.NORMAL)
                }
                canvas.drawPath(path, paint)
            }
        }
    }
}
