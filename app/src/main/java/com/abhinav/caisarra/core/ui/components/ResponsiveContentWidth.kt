package com.abhinav.caisarra.core.ui.components

import androidx.compose.ui.unit.Dp
import com.abhinav.caisarra.core.ui.theme.ScreenContentMaxWidth
import com.abhinav.caisarra.core.ui.theme.ScreenHorizontalPadding

fun responsiveContentWidth(maxWidth: Dp, panes: Int = 1): Dp =
    minOf(maxWidth - ScreenHorizontalPadding * 2, ScreenContentMaxWidth * panes)
