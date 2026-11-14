package com.abhinav.caisarra.features.gameplay.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.presentation.common.singleClickable
import com.caisaara.ui.theme.PuzzleCardBorder
import com.caisaara.ui.theme.PuzzleHeaderBg
import com.caisaara.ui.theme.StatsCyan
import com.caisaara.ui.theme.StatsTextMuted

@Composable
fun GameActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
    badgeCount: Int = 0,
    iconRotation: Float = 0f
) {
    val shape = RoundedCornerShape(16.dp)
    val tint by animateColorAsState(if (selected) StatsCyan else StatsTextMuted, label = "actionTint")
    val border by animateColorAsState(if (selected) StatsCyan else PuzzleCardBorder, label = "actionBorder")
    val rotation by animateFloatAsState(iconRotation, spring(dampingRatio = 0.55f), label = "actionRotation")
    Column(
        modifier = modifier
            .alpha(if (enabled) 1f else 0.4f)
            .clip(shape)
            .background(PuzzleHeaderBg)
            .border(1.dp, border, shape)
            .singleClickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box {
            Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(24.dp).rotate(rotation))
            if (badgeCount > 0) {
                Text(
                    text = "$badgeCount",
                    color = Color.Black,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 8.dp, y = (-6).dp)
                        .background(StatsCyan, CircleShape)
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                )
            }
        }
        Text(text = label, color = tint, fontSize = 13.sp, maxLines = 1)
    }
}
