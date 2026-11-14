package com.abhinav.caisarra.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.abhinav.caisarra.core.ui.theme.FeedContentMaxWidth

fun Modifier.centeredContentWidth(maxWidth: Dp = FeedContentMaxWidth): Modifier =
    fillMaxWidth().wrapContentWidth().widthIn(max = maxWidth).fillMaxWidth()
