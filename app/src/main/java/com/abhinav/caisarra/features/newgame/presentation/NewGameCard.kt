package com.abhinav.caisarra.features.newgame.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.core.ui.components.glassBorder
import com.abhinav.caisarra.presentation.common.singleClick
import com.caisaara.ui.theme.CardSurface
import com.caisaara.ui.theme.StatsCyan

@Composable
fun NewGameCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    Box(
        modifier = modifier
            .shadow(if (pressed) 12.dp else 0.dp, shape, ambientColor = StatsCyan, spotColor = StatsCyan)
            .clip(shape)
            .background(CardSurface)
            .then(if (pressed) Modifier.border(1.5.dp, StatsCyan, shape) else Modifier.glassBorder(shape))
            .clickable(interactionSource, ripple(color = StatsCyan), onClick = singleClick(onClick))
            .padding(16.dp)
    ) {
        content()
    }
}
