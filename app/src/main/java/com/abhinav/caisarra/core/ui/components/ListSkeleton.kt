package com.abhinav.caisarra.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.presentation.screens.components.rememberShimmerBrush

@Composable
fun ListSkeleton(
    modifier: Modifier = Modifier,
    rows: Int = 7,
    showHeader: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(16.dp)
) {
    val brush = rememberShimmerBrush()
    Column(
        modifier = modifier.fillMaxSize().padding(contentPadding).fadeInOnAppear(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (showHeader) HeaderCardSkeleton(brush)
        repeat(rows) { ListRowSkeleton(brush = brush) }
    }
}
