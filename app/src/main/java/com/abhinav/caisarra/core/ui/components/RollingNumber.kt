package com.abhinav.caisarra.core.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.caisaara.ui.theme.White

@Composable
fun RollingNumber(value: Int, fontSize: TextUnit) {
    AnimatedContent(
        targetState = value,
        transitionSpec = {
            if (targetState > initialState) {
                (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut())
            } else {
                ContentTransform(fadeIn(), fadeOut())
            }
        },
        label = "RollingNumber"
    ) { number ->
        Text("$number", color = White, fontSize = fontSize, fontWeight = FontWeight.Bold, fontFamily = JetBrainsMono)
    }
}
