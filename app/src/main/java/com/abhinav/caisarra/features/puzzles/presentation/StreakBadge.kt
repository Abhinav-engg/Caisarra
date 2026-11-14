package com.abhinav.caisarra.features.puzzles.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.components.RollingNumber
import com.caisaara.ui.theme.GoldColor

@Composable
fun StreakBadge(streak: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(end = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.LocalFireDepartment, contentDescription = "Streak", tint = GoldColor, modifier = Modifier.size(20.dp))
        RollingNumber(streak, 18.sp)
    }
}
