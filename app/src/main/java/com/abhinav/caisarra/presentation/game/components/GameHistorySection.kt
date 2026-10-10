package com.abhinav.caisarra.presentation.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.data.local.entity.GameEntity
import com.abhinav.caisarra.presentation.theme.JetBrainsMono
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val CardColor = Color(0xFF151F2B)

private val BorderColor = Color(0xFF304154)

private val White = Color(0xFFF5F5F5)

private val Secondary = Color(0xFF8C98A5)

private val Accent = Color(0xFF10B981)

private val DateFormatter =
    DateTimeFormatter
        .ofPattern("dd MMM yyyy • HH:mm",
            Locale.ENGLISH
        )
        .withZone(ZoneId.systemDefault())

@Composable
fun GameHistorySection(
    games: List<GameEntity>,
    onReviewGame: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement =
            Arrangement.spacedBy(10.dp)
    ) {

        Text(
            text = "GAME HISTORY",
            color = White,
            fontFamily = JetBrainsMono,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        if (games.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        CardColor,
                        RoundedCornerShape(14.dp)
                    )
                    .border(
                        1.dp,
                        BorderColor,
                        RoundedCornerShape(14.dp)
                    )
                    .padding(16.dp)
            ) {

                Text(
                    text = "No completed games yet.",
                    color = Secondary,
                    fontFamily = JetBrainsMono,
                    fontSize = 12.sp
                )
            }
            return
        }

        games.forEach { game -> GameHistoryCard(game = game,
                onReview = {onReviewGame(game.id)}
            )
        }
    }
}

@Composable
private fun GameHistoryCard(
    game: GameEntity,
    onReview: () -> Unit
) {

    val moveCount =
        game.moves
            .split(" ")
            .count {
                it.isNotBlank()
            }

    val resultText = when {
        game.status == com.abhinav.caisarra.data.local.entity.RecordStatus.InProgress -> "IN PROGRESS"
        game.result == "WHITE_WINS" -> "WHITE WINS"
        game.result == "BLACK_WINS" -> "BLACK WINS"
        game.result == "DRAW" -> "DRAW"
        else -> "FINISHED"
    }

    val dateText = when {
        game.status == com.abhinav.caisarra.data.local.entity.RecordStatus.InProgress -> "In progress"
        game.endedAt != null ->
            DateFormatter.format(
                Instant.ofEpochMilli(game.endedAt)
            )
        else -> "Finished"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardColor,
                RoundedCornerShape(14.dp)
            )
            .border(1.dp,
                BorderColor,
                RoundedCornerShape(14.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(modifier = Modifier.weight(1f)
        ) {

            Text(text = "${game.whiteName}vs ${game.blackName}",
                color = White,
                fontFamily = JetBrainsMono,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))

            Text(text = dateText,
                color = Secondary,
                fontFamily = JetBrainsMono,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = "$resultText  •  ${game.timeControlMinutes ?: 0} min",
                color = if (resultText == "DRAW") {
                        Secondary
                    } else {
                        Accent
                    },
                fontFamily = JetBrainsMono,
                fontSize = 10.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))
        OutlinedButton(
            onClick = onReview,
            shape =
                RoundedCornerShape(10.dp)
        ) {
            Text(
                text = "REVIEW",
                color = White,
                fontFamily = JetBrainsMono,
                fontSize = 10.sp
            )
        }
    }
}