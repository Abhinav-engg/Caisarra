package com.abhinav.caisarra.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.abhinav.caisarra.ui.theme.SignUpButtonDefaultCornerRadius
import com.abhinav.caisarra.ui.theme.SignUpButtonDefaultMaxHeight
import com.caisaara.ui.theme.ButtonGradientEnd
import com.caisaara.ui.theme.ButtonGradientMid
import com.caisaara.ui.theme.ButtonGradientStart

@Composable
fun GeneralButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(SignUpButtonDefaultMaxHeight)
            .alpha(if (enabled) 1f else 0.5f)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(ButtonGradientStart, ButtonGradientMid, ButtonGradientEnd)
                ),
                shape = RoundedCornerShape(SignUpButtonDefaultCornerRadius)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Black
        )
    }
}