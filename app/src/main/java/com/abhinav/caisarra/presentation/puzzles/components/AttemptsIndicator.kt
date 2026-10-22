package com.abhinav.caisarra.presentation.puzzles.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AttemptsIndicator(
    attemptsLeft: Int,
    totalAttempts: Int = 3,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalAttempts) { index ->
            val isActive = index < attemptsLeft
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        color = if (isActive) Color(0xFF00D6A0) else Color(0xFF334155),
                        shape = CircleShape
                    )
                    .border(
                        width = 1.dp,
                        color = if (isActive) Color(0xFF10B981) else Color(0xFF475569),
                        shape = CircleShape
                    )
            )
        }
    }
}
