
package com.abhinav.caisarra.presentation.game.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.R
import com.abhinav.caisarra.data.local.entity.GameEntity
import com.abhinav.caisarra.presentation.theme.JetBrainsMono
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val HistoryCardColor = Color(0xFF071821)
private val HistoryBorderColor = Color(0xFF173541)
private val HistoryWhite = Color(0xFFF5F7FA)
private val HistorySecondary = Color(0xFFA6C1D4)
private val HistoryAccent = Color(0xFF00DCCB)

private val HistoryDateFormatter = DateTimeFormatter
    .ofPattern("dd MMM yyyy", Locale.ENGLISH)
    .withZone(ZoneId.systemDefault())

private val HistoryTimeFormatter = DateTimeFormatter
    .ofPattern("h:mm a", Locale.ENGLISH)
    .withZone(ZoneId.systemDefault())

@Composable
fun GameHistorySection(
    games: List<GameEntity>,
    onReviewGame: (String) -> Unit,
    modifier: Modifier = Modifier,
    showViewAll: Boolean = true,
    onViewAll: () -> Unit = {},
    isLoading: Boolean = false
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.white_castle),
                contentDescription = "Game history",
                modifier = Modifier.size(27.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "GAME HISTORY",
                color = HistoryWhite,
                fontFamily = JetBrainsMono,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
        }

        when {
            isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            HistoryCardColor,
                            RoundedCornerShape(20.dp)
                        )
                        .border(
                            1.dp,
                            HistoryBorderColor,
                            RoundedCornerShape(20.dp)
                        )
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        color = HistoryAccent,
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 3.dp
                    )

                    Text(
                        text = "Loading game history...",
                        color = HistorySecondary,
                        fontFamily = JetBrainsMono,
                        fontSize = 12.sp
                    )
                }
            }

            games.isEmpty() -> {
                Text(
                    text = "No completed games yet.",
                    color = HistorySecondary,
                    fontFamily = JetBrainsMono,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            HistoryCardColor,
                            RoundedCornerShape(18.dp)
                        )
                        .border(
                            1.dp,
                            HistoryBorderColor,
                            RoundedCornerShape(18.dp)
                        )
                        .padding(20.dp)
                )
            }

            else -> {
                val visibleGames =
                    if (showViewAll) games.take(5) else games

                visibleGames.forEach { game ->
                    GameHistoryCard(
                        game = game,
                        onReview = { onReviewGame(game.id) }
                    )
                }
            }
        }

        if (showViewAll && !isLoading && games.isNotEmpty()) {
            OutlinedButton(
                onClick = onViewAll,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(15.dp)
            ) {
                Text(
                    text = "VIEW ALL GAMES     →",
                    color = HistoryAccent,
                    fontFamily = JetBrainsMono,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@Composable
private fun GameHistoryCard(
    game: GameEntity,
    onReview: () -> Unit
) {
    val resultText = when (game.result) {
        "WHITE_WINS" -> "WHITE WINS"
        "BLACK_WINS" -> "BLACK WINS"
        "DRAW" -> "DRAW"
        else -> "FINISHED"
    }

    val completedAt = game.endedAt?.let {
        Instant.ofEpochMilli(it)
    }

    val dateText = completedAt?.let {
        HistoryDateFormatter.format(it)
    } ?: "Date unavailable"

    val timeText = completedAt?.let {
        HistoryTimeFormatter.format(it)
    } ?: "--:--"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                HistoryCardColor,
                RoundedCornerShape(20.dp)
            )
            .border(
                1.dp,
                HistoryBorderColor,
                RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 12.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Both players stay in the same row.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.white_king),
                    contentDescription = "White king",
                    modifier = Modifier.size(28.dp)
                )

                Text(
                    text = game.whiteName,
                    color = HistoryWhite,
                    fontFamily = JetBrainsMono,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Text(
                    text = "VS",
                    color = HistorySecondary,
                    fontFamily = JetBrainsMono,
                    fontSize = 10.sp
                )

                Image(
                    painter = painterResource(R.drawable.black_king),
                    contentDescription = "Black king",
                    modifier = Modifier.size(28.dp)
                )

                Text(
                    text = game.blackName,
                    color = HistoryWhite,
                    fontFamily = JetBrainsMono,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }

            // Date followed by time.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = dateText,
                    color = HistorySecondary,
                    fontFamily = JetBrainsMono,
                    fontSize = 10.sp,
                    maxLines = 1
                )

                Text(
                    text = "•",
                    color = HistoryAccent,
                    fontSize = 12.sp
                )

                Text(
                    text = timeText,
                    color = HistorySecondary,
                    fontFamily = JetBrainsMono,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }

            Text(
                text = "$resultText  •  ${game.timeControlMinutes ?: 0} min",
                color = HistoryAccent,
                fontFamily = JetBrainsMono,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }

        Spacer(Modifier.width(8.dp))

        OutlinedButton(
            onClick = onReview,
            modifier = Modifier.widthIn(
                min = 76.dp,
                max = 94.dp
            ),
            contentPadding = PaddingValues(
                horizontal = 5.dp,
                vertical = 10.dp
            ),
            shape = RoundedCornerShape(13.dp)
        ) {
            Text(
                text = "▶ REVIEW",
                color = HistoryWhite,
                fontFamily = JetBrainsMono,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

