package com.abhinav.caisarra.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlinx.coroutines.delay

@Composable
fun AnimatedTickMark(key: Any, color: Color, modifier: Modifier = Modifier, holdMillis: Long = 700) {
    val progress = remember(key) { Animatable(0f) }
    val alpha = remember(key) { Animatable(1f) }
    LaunchedEffect(key) {
        progress.animateTo(1f, tween(350, easing = FastOutSlowInEasing))
        delay(holdMillis)
        alpha.animateTo(0f, tween(250))
    }
    Canvas(modifier.alpha(alpha.value)) {
        drawCircle(color, alpha = progress.value.coerceAtMost(0.25f) * 4)
        val tick = Path().apply {
            moveTo(size.width * 0.28f, size.height * 0.52f)
            lineTo(size.width * 0.44f, size.height * 0.68f)
            lineTo(size.width * 0.74f, size.height * 0.36f)
        }
        val measure = PathMeasure().apply { setPath(tick, false) }
        val drawn = Path().also { measure.getSegment(0f, measure.length * progress.value, it, true) }
        drawPath(drawn, Color.White, style = Stroke(size.minDimension * 0.12f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}
