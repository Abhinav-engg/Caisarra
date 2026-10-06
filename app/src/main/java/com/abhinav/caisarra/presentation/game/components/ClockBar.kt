package com.abhinav.caisarra.presentation.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ClockBar(
    playerName: String,
    timeMillis: Long,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val totalSeconds = (timeMillis / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    val timeText = "%02d:%02d".format(minutes, seconds)

    val background = if (isActive) Color(0xFF263B30)
        else Color(0xFF151F2B)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                background,
                RoundedCornerShape(12.dp)
            )
            .border(
                1.dp,
                Color(0xFF304154),
                RoundedCornerShape(12.dp)
            )
            .padding(
                horizontal = 16.dp,
                vertical = 10.dp
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = playerName,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Text(text = timeText,
            color = if (isActive) {
                Color(0xFF10B981)
            } else {
                Color.White
            },
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}