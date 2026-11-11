package com.abhinav.caisarra.core.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.caisaara.ui.theme.CardBorderNavy
import com.caisaara.ui.theme.CardSurface
import com.caisaara.ui.theme.StatsCyan

@Composable
fun SearchPulse(icon: Painter, size: Dp, modifier: Modifier = Modifier) {
    val progress by rememberInfiniteTransition(label = "searchPulse").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing)),
        label = "searchPulseProgress"
    )
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = 0.75f + 0.4f * progress
                    scaleY = scaleX
                    alpha = 1f - progress
                }
                .border(1.5.dp, StatsCyan, CircleShape)
        )
        Box(Modifier.fillMaxSize().border(1.5.dp, StatsCyan, CircleShape))
        Box(
            Modifier
                .fillMaxSize(0.75f)
                .background(CardSurface, CircleShape)
                .border(1.dp, CardBorderNavy, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = StatsCyan, modifier = Modifier.fillMaxSize(0.35f))
        }
    }
}
