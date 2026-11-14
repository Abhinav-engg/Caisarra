package com.abhinav.caisarra.features.gameplay.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.caisaara.ui.theme.PlaceholderColor

@Composable
fun SignalBars(activeBars: Int, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.height(12.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        repeat(3) { index ->
            val active = index < activeBars
            val barColor by animateColorAsState(if (active) color else PlaceholderColor, label = "barColor")
            val barHeight by animateDpAsState(
                targetValue = if (active) (5 + index * 3.5f).dp else 4.dp,
                animationSpec = spring(dampingRatio = 0.5f),
                label = "barHeight"
            )
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(barHeight)
                    .background(barColor, RoundedCornerShape(1.5.dp))
            )
        }
    }
}
