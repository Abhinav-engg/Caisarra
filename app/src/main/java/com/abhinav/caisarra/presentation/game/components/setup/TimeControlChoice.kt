package com.abhinav.caisarra.presentation.game.components.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TimeControlChoice(option: TimeControlOption, selected: Boolean, modifier: Modifier, onSelected: (TimeControlOption) -> Unit) {
    Box(
        modifier = modifier.height(58.dp).clip(RoundedCornerShape(15.dp)).background(SetupColors.Card).clickable { onSelected(option) },
        contentAlignment = Alignment.Center
    ) {
        Text(option.label, color = if (selected) SetupColors.Accent else SetupColors.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}
