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
    capturedPieces: List<Piece> = emptyList()
) {
    val daily = isDailyMode || timeMillis >= DAY_MILLIS

    val timeText = if (daily) {
        if (timeMillis <= 0L) {
            "0 days"
        } else {
            val remainingDays = ((timeMillis - 1L) / DAY_MILLIS) + 1L
            if (remainingDays == 1L) "1 day" else "$remainingDays days"
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
                    maxLines = 1
                )

                if (capturedPieces.isNotEmpty()) {
                    CapturedPieces(
                        pieces = capturedPieces
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

    }
}
