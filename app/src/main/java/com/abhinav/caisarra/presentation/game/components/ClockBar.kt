package com.abhinav.caisarra.presentation.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.presentation.game.model.Piece
import com.caisaara.ui.theme.EmeraldNormal
import com.caisaara.ui.theme.White

private const val DAY_MILLIS = 86_400_000L

fun formatDailyTime(timeMillis: Long): String {
    if (timeMillis <= 0L) return "0s"
    val totalSec = timeMillis / 1000L
    val days = totalSec / 86400L
    val hours = (totalSec % 86400L) / 3600L
    val minutes = (totalSec % 3600L) / 60L
    val seconds = totalSec % 60L

    return when {
        days > 0L -> "${days}d ${hours}h"
        hours > 0L -> "${hours}h ${minutes}m"
        minutes > 0L -> "${minutes}m ${seconds}s"
        else -> "${seconds}s"
    }
}

@Composable
fun ClockBar(
    playerName: String,
    timeMillis: Long,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    isDailyMode: Boolean = false,
    rating: Int? = null,
    capturedPieces: List<Piece> = emptyList(),
    advantage: Int = 0
) {
    val daily = isDailyMode || timeMillis >= DAY_MILLIS

    val timeText = if (daily) {
        formatDailyTime(timeMillis)
    } else {
        val totalSeconds = timeMillis.coerceAtLeast(0L) / 1000L
        "%02d:%02d".format(totalSeconds / 60L, totalSeconds % 60L)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (isActive) Color(0xFF263B30) else Color(0xFF151F2B),
                RoundedCornerShape(12.dp)
            )
            .border(
                1.dp,
                Color(0xFF304154),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = playerName,
                    color = White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (capturedPieces.isNotEmpty() || advantage > 0) {
                    CapturedPieces(
                        pieces = capturedPieces,
                        advantage = advantage
                    )
                }
            }

            rating?.let {
                Text(
                    text = "Rating $it",
                    color = Color(0xFF9FB0C3),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = timeText,
            color = if (isActive) {
                EmeraldNormal
            } else {
                White
            },
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
