package com.abhinav.caisarra.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.presentation.screens.components.SkeletonBox
import com.abhinav.caisarra.presentation.screens.components.rememberShimmerBrush
import com.caisaara.ui.theme.StatsCardBg
import com.caisaara.ui.theme.StatsCardBorder

@Composable
fun HeaderCardSkeleton(brush: Brush = rememberShimmerBrush()) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(StatsCardBg)
            .border(1.dp, StatsCardBorder, shape)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SkeletonBox(Modifier.size(72.dp), shape = CircleShape, brush = brush)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SkeletonBox(Modifier.fillMaxWidth(0.6f).height(20.dp), brush = brush)
                SkeletonBox(Modifier.fillMaxWidth(0.4f).height(13.dp), brush = brush)
            }
        }
        SkeletonBox(Modifier.fillMaxWidth().height(36.dp), shape = RoundedCornerShape(10.dp), brush = brush)
    }
}
