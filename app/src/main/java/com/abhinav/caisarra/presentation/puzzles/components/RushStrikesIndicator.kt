package com.abhinav.caisarra.presentation.puzzles.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun RushStrikesIndicator(
    strikes: Int,
    totalStrikes: Int = 3,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalStrikes) { index ->
            val isStruck = index < strikes
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(
                        color = if (isStruck) Color(0xFF7F1D1D) else Color(0xFF1E293B),
                        shape = CircleShape
                    )
                    .border(
                        width = 1.dp,
                        color = if (isStruck) Color(0xFFEF4444) else Color(0xFF334155),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isStruck) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Color(0xFFF87171),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
