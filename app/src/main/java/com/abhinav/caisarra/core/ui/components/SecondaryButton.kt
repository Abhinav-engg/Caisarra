package com.abhinav.caisarra.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.theme.SignUpButtonDefaultCornerRadius
import com.abhinav.caisarra.core.ui.theme.SignUpButtonDefaultMaxHeight
import com.abhinav.caisarra.presentation.common.singleClick
import com.caisaara.ui.theme.CardSurface
import com.caisaara.ui.theme.StatsTextWhite

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(SignUpButtonDefaultCornerRadius)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(SignUpButtonDefaultMaxHeight)
            .alpha(if (enabled) 1f else 0.5f)
            .clip(shape)
            .background(CardSurface)
            .glassBorder(shape)
            .clickable(enabled = enabled, onClick = singleClick(onClick)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = StatsTextWhite)
    }
}
