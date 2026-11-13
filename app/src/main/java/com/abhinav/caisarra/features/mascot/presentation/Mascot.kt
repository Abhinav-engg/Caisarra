package com.abhinav.caisarra.features.mascot.presentation

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.features.mascot.domain.MascotMood
import kotlinx.coroutines.launch

@Composable
fun Mascot(
    mood: MascotMood,
    reactionKey: Any,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp
) {
    val density = LocalDensity.current
    val idle = rememberInfiniteTransition(label = "mascotIdle")
    val bob by idle.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "mascotBob"
    )
    val squash = remember { Animatable(1f) }
    val hop = remember { Animatable(0f) }

    LaunchedEffect(reactionKey) {
        launch {
            squash.snapTo(0.88f)
            squash.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMediumLow))
        }
        if (mood.isCelebration) {
            repeat(2) {
                hop.animateTo(-1f, tween(170, easing = FastOutSlowInEasing))
                hop.animateTo(0f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium))
            }
        }
    }

    val bobPx = with(density) { (size * 0.03f).toPx() }
    val hopPx = with(density) { (size * 0.16f).toPx() }

    Crossfade(
        targetState = mood,
        animationSpec = tween(220),
        label = "mascotMood",
        modifier = modifier
            .size(size)
            .graphicsLayer {
                transformOrigin = TransformOrigin(0.5f, 1f)
                translationY = bob * bobPx + hop.value * hopPx
                scaleX = 2f - squash.value
                scaleY = squash.value
            }
    ) { current ->
        Image(
            painter = painterResource(current.drawableRes),
            contentDescription = null,
            modifier = Modifier.size(size)
        )
    }
}
