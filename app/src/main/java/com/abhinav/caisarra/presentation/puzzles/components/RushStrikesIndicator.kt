package com.abhinav.caisarra.presentation.puzzles.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.caisaara.ui.theme.RedNormal
import com.caisaara.ui.theme.StatsPieceCircleDark

@Composable
fun RushStrikesIndicator(
    strikes: Int,
    totalStrikes: Int = 3,
    modifier: Modifier = Modifier
) {
    val livesRemaining = (totalStrikes - strikes).coerceAtLeast(0)
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalStrikes) { index ->
            val isActive = index < livesRemaining
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = if (isActive) RedNormal else StatsPieceCircleDark,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
