package com.abhinav.caisarra.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.caisaara.ui.theme.CardSurface
import com.caisaara.ui.theme.StatsGreen
import kotlinx.coroutines.launch

@Composable
fun OpponentFoundBurst(icon: Painter, size: Dp, modifier: Modifier = Modifier) {
    val pop = remember { Animatable(0.6f) }
    val burst = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        launch { pop.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMediumLow)) }
        burst.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
    }
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val p = burst.value
                    scaleX = 0.8f + 0.5f * p
                    scaleY = scaleX
                    alpha = 1f - p
                }
                .border(2.dp, StatsGreen, CircleShape)
        )
        Box(
            Modifier
                .fillMaxSize(0.75f)
                .graphicsLayer {
                    scaleX = pop.value
                    scaleY = pop.value
                }
                .background(CardSurface, CircleShape)
                .background(StatsGreen.copy(alpha = 0.14f), CircleShape)
                .border(1.5.dp, StatsGreen, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = StatsGreen, modifier = Modifier.fillMaxSize(0.35f))
        }
    }
}
