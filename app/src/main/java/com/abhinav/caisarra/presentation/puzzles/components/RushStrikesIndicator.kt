package com.abhinav.caisarra.presentation.puzzles.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.caisaara.ui.theme.RedNormal
import com.caisaara.ui.theme.StatsCyan

@Composable
fun RushStrikesIndicator(
    strikes: Int,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    totalStrikes: Int = 3
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalStrikes) { index ->
            val struck = index < strikes
            val color by animateColorAsState(if (struck) RedNormal else StatsCyan, label = "strike")
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape)
                    .border(1.5.dp, color.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(if (struck) Icons.Outlined.Close else icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
        }
    }
}
