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
import androidx.compose.foundation.layout.width
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
fun ListRowSkeleton(
    modifier: Modifier = Modifier,
    brush: Brush = rememberShimmerBrush()
) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(StatsCardBg)
            .border(1.dp, StatsCardBorder, shape)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SkeletonBox(Modifier.size(40.dp), shape = CircleShape, brush = brush)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SkeletonBox(Modifier.fillMaxWidth(0.55f).height(14.dp), brush = brush)
            SkeletonBox(Modifier.fillMaxWidth(0.85f).height(11.dp), brush = brush)
        }
        SkeletonBox(Modifier.width(40.dp).height(18.dp), brush = brush)
    }
}
