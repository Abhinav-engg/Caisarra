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

private const val DAY_MILLIS = 86_400_000L

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
        when {
            timeMillis <= 0L -> "0 days"
            timeMillis >= DAY_MILLIS -> {
                val remainingDays = ((timeMillis - 1L) / DAY_MILLIS) + 1L
                if (remainingDays == 1L) "1 day" else "$remainingDays days"
            }
            timeMillis >= 3600_000L -> {
                val hours = (timeMillis + 3599_999L) / 3600_000L
                if (hours == 1L) "1 hr" else "$hours hrs"
            }
            else -> {
                val minutes = ((timeMillis + 59_999L) / 60_000L).coerceAtLeast(1L)
                if (minutes == 1L) "1 min" else "$minutes min"
            }
        }
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
                    color = Color.White,
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
                Color(0xFF10B981)
            } else {
                Color.White
            },
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
