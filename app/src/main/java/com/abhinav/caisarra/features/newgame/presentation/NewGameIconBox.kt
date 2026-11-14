package com.abhinav.caisarra.features.newgame.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.caisaara.ui.theme.StatsBg
import com.caisaara.ui.theme.StatsCyan

@Composable
fun NewGameIconBox(icon: ImageVector) {
    Box(
        modifier = Modifier.size(40.dp).background(StatsBg, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = StatsCyan, modifier = Modifier.size(20.dp))
    }
}
