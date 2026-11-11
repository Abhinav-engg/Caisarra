package com.abhinav.caisarra.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.caisaara.ui.theme.CyanLight
import com.caisaara.ui.theme.IndicatorInactive
import androidx.compose.ui.unit.dp

@Composable
fun PageIndicatorDots(
    count: Int,
    selectedIndex: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) { index ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(if (index == selectedIndex) 7.dp else 4.dp)
                    .clip(CircleShape)
                    .background(if (index == selectedIndex) CyanLight else IndicatorInactive)
            )
        }
    }
}
