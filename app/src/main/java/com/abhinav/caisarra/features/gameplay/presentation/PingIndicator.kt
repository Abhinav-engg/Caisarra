package com.abhinav.caisarra.features.gameplay.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.abhinav.caisarra.features.gameplay.domain.PingQuality

@Composable
fun PingIndicator(pingMs: Long?, connected: Boolean, modifier: Modifier = Modifier) {
    val quality = PingQuality.of(pingMs, connected)
    val color by animateColorAsState(quality.color(), animationSpec = tween(400), label = "pingColor")
    val pulse by rememberInfiniteTransition(label = "pingPulse").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(if (connected) 1600 else 800, easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = "pulse"
    )
    Row(
        modifier = modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(50))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .graphicsLayer {
                        scaleX = 1f + pulse * 1.6f
                        scaleY = 1f + pulse * 1.6f
                        alpha = 1f - pulse
                    }
                    .background(color, CircleShape)
            )
            Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        }
        AnimatedContent(
            targetState = if (connected) pingMs?.let { "$it ms" } ?: "-- ms" else "Disconnected",
            transitionSpec = {
                (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut())
            },
            label = "pingText"
        ) { text ->
            Text(
                text = text,
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = JetBrainsMono
            )
        }
        if (connected) SignalBars(activeBars = quality.bars, color = color)
    }
}
