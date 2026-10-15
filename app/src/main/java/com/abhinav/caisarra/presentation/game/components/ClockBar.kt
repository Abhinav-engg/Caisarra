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

private const val DAY_MILLIS = 86_400_000L
@Composable
fun ClockBar(
    playerName: String,
    timeMillis: Long,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    isDailyMode: Boolean = false,
    rating: Int? = null
) {
    val daily = isDailyMode || timeMillis >= DAY_MILLIS

    val timeText = if (daily) {
        if (timeMillis <= 0L) {
            "0 days"
        } else {
            val remainingDays =
                ((timeMillis - 1L) / DAY_MILLIS) + 1L

            if (remainingDays == 1L) {
                "1 day"
            } else {
                "$remainingDays days"
            }
        }
    } else {
        val totalSeconds = timeMillis.coerceAtLeast(0L) / 1000L
        val minutes = totalSeconds / 60L
        val seconds = totalSeconds % 60L

        "%02d:%02d".format(minutes, seconds)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = if (isActive) {
                    Color(0xFF263B30)
                } else {
                    Color(0xFF151F2B)
                },
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xFF304154),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(
                horizontal = 16.dp,
                vertical = 10.dp
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = playerName,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            rating?.let {
                Text(
                    text = "Rating $it",
                    color = Color(0xFF9FB0C3),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Text(
            text = timeText,
            color = if (isActive) {
                Color(0xFF10B981)
            } else {
                Color.White
            },
            fontSize = if (daily) 18.sp else 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
