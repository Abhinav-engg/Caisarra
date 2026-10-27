package com.abhinav.caisarra.presentation.watch.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.abhinav.caisarra.data.remote.dto.ActiveGameDto
import com.caisaara.ui.theme.StatsCardBg
import com.caisaara.ui.theme.StatsCardBorder
import com.caisaara.ui.theme.StatsCyan
import com.caisaara.ui.theme.StatsGreen
import com.caisaara.ui.theme.StatsTextMuted
import com.caisaara.ui.theme.StatsTextWhite
import com.caisaara.ui.theme.ThemeNeonDeep

@Composable
fun ActiveGameCard(
    game: ActiveGameDto,
    onWatchClick: (ActiveGameDto) -> Unit,
    modifier: Modifier = Modifier
) {
    val whiteName = game.whiteUsername?.takeIf { it.isNotBlank() } ?: "Player #${game.whitePlayerId}"
    val blackName = game.blackUsername?.takeIf { it.isNotBlank() } ?: "Player #${game.blackPlayerId}"
    val timeControlText = "${game.timeControlMinutes} min" + if (game.incrementSeconds > 0) " +${game.incrementSeconds}s" else ""

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(StatsCardBg)
            .border(1.dp, StatsCardBorder, RoundedCornerShape(14.dp))
            .clickable { onWatchClick(game) }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .background(Color(0xFF133224), RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(StatsGreen, CircleShape)
                    )
                    Text(
                        text = "LIVE",
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = StatsGreen,
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = timeControlText,
                    fontFamily = JetBrainsMono,
                    fontSize = 11.sp,
                    color = StatsTextMuted
                )

                if (game.rated) {
                    Text(
                        text = "• Rated",
                        fontFamily = JetBrainsMono,
                        fontSize = 11.sp,
                        color = StatsTextMuted
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    tint = StatsCyan,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "${game.spectatorCount}",
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = StatsCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color.White, CircleShape)
                            .border(1.dp, Color(0xFF64748B), CircleShape)
                    )
                    Text(
                        text = whiteName,
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = StatsTextWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                game.whiteRating?.let {
                    Text(
                        text = "Rating $it",
                        fontFamily = JetBrainsMono,
                        fontSize = 11.sp,
                        color = StatsTextMuted,
                        modifier = Modifier.padding(start = 16.dp, top = 2.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .background(Color(0xFF1E2D3D), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "VS",
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = StatsCyan
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = blackName,
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = StatsTextWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color(0xFF1E293B), CircleShape)
                            .border(1.dp, Color(0xFF94A3B8), CircleShape)
                    )
                }
                game.blackRating?.let {
                    Text(
                        text = "Rating $it",
                        fontFamily = JetBrainsMono,
                        fontSize = 11.sp,
                        color = StatsTextMuted,
                        modifier = Modifier.padding(end = 16.dp, top = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val turnText = if (game.currentTurn.equals("white", ignoreCase = true)) {
                "$whiteName's turn"
            } else {
                "$blackName's turn"
            }

            Text(
                text = turnText,
                fontFamily = JetBrainsMono,
                fontSize = 11.sp,
                color = StatsTextMuted
            )

            Button(
                onClick = { onWatchClick(game) },
                colors = ButtonDefaults.buttonColors(containerColor = ThemeNeonDeep),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text(
                    text = "WATCH",
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = StatsCyan
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = StatsCyan,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
