package com.abhinav.caisarra.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.abhinav.caisarra.ui.theme.SignUpButtonDefaultCornerRadius
import com.abhinav.caisarra.ui.theme.SignUpButtonDefaultMaxHeight
import com.abhinav.caisarra.ui.theme.SignUpButtonDefaultMaxWidth
import com.caisaara.ui.theme.CyanLight
import com.caisaara.ui.theme.EmeraldNormal

@Composable
fun GeneralButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Box(modifier = Modifier
            .width(SignUpButtonDefaultMaxWidth)
            .height(SignUpButtonDefaultMaxHeight)
            .alpha(if (enabled) 1f else 0.5f)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(CyanLight, EmeraldNormal)
                ),
                shape = RoundedCornerShape(SignUpButtonDefaultCornerRadius)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Black
        )

    }
}