package com.abhinav.caisarra.presentation.game.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import kotlin.math.hypot

@Composable
fun GameOverReveal(
    delayMillis: Long = 450,
    modifier: Modifier = Modifier.fillMaxSize(),
    content: @Composable () -> Unit
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(delayMillis)
        progress.animateTo(1f, tween(750, easing = FastOutSlowInEasing))
    }
    Box(
        modifier = modifier
            .graphicsLayer {
                val p = progress.value
                alpha = (p * 2f).coerceAtMost(1f)
                scaleX = 0.94f + 0.06f * p
                scaleY = scaleX
            }
            .drawWithContent {
                val radius = hypot(size.width, size.height) / 2f * progress.value
                val circle = Path().apply {
                    addOval(Rect(Offset(size.width / 2f, size.height / 2f), radius))
                }
                clipPath(circle) { this@drawWithContent.drawContent() }
            }
    ) { content() }
}
