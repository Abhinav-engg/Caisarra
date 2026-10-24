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
fun AttemptsIndicator(
    attemptsLeft: Int,
    totalAttempts: Int = 3,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalAttempts) { index ->
            val isActive = index < attemptsLeft
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = if (isActive) RedNormal else StatsPieceCircleDark,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
