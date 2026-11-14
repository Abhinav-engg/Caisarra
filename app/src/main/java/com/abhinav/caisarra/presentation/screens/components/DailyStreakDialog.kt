package com.abhinav.caisarra.presentation.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.abhinav.caisarra.R
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.abhinav.caisarra.data.remote.dto.DailyStreakResponse
import com.abhinav.caisarra.data.remote.dto.StreakDayDto
import com.abhinav.caisarra.features.mascot.domain.StreakMascotLines
import com.abhinav.caisarra.features.mascot.presentation.BubbleTail
import com.abhinav.caisarra.features.mascot.presentation.Mascot
import com.abhinav.caisarra.features.mascot.presentation.MascotSpeechBubble
import com.caisaara.ui.theme.PuzzleCardBorder
import com.caisaara.ui.theme.StatsBg
import com.caisaara.ui.theme.StatsCyan
import com.caisaara.ui.theme.ThemeSlateAccent

@Composable
fun DailyStreakDialog(
    streakData: DailyStreakResponse?,
    currentStreak: Int,
    puzzleStreak: Int,
    onDismiss: () -> Unit,
    onOpenDailyPuzzle: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(StatsBg)
                .border(1.dp, PuzzleCardBorder, RoundedCornerShape(20.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val bestStreak = maxOf(streakData?.bestStreak ?: 0, streakData?.maxStreak ?: 0, currentStreak)
            val mascotReaction = StreakMascotLines.forStreak(
                currentStreak = currentStreak,
                bestStreak = bestStreak,
                todayCompleted = streakData?.todayCompleted == true,
                status = streakData?.streakStatus.orEmpty()
            )

            Mascot(
                mood = mascotReaction.mood,
                reactionKey = mascotReaction.key,
                size = 104.dp
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "$currentStreak Day Streak",
                color = Color.White,
                fontFamily = JetBrainsMono,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(4.dp))

            Spacer(Modifier.height(10.dp))

            MascotSpeechBubble(
                reaction = mascotReaction,
                tail = BubbleTail.TOP,
                centered = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StreakMetricPill(
                    label = "Best Streak",
                    value = "$bestStreak days"
                )
                StreakMetricPill(
                    label = "Freezes",
                    value = "${streakData?.freezesAvailable ?: 0} left"
                )
            }

            Spacer(Modifier.height(18.dp))

            val weekDays = streakData?.week?.takeIf { it.isNotEmpty() } ?: listOf(
                StreakDayDto(weekday = "M"),
                StreakDayDto(weekday = "T"),
                StreakDayDto(weekday = "W"),
                StreakDayDto(weekday = "T"),
                StreakDayDto(weekday = "F"),
                StreakDayDto(weekday = "S"),
                StreakDayDto(weekday = "S")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                weekDays.forEach { day ->
                    StreakDayItem(day = day)
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E293B).copy(alpha = 0.6f))
                    .border(1.dp, PuzzleCardBorder, RoundedCornerShape(12.dp))
                    .clickable {
                        onDismiss()
                        onOpenDailyPuzzle()
                    }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_puzzle_piece),
                        contentDescription = null,
                        tint = StatsCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Puzzle Streak",
                            color = Color.White,
                            fontFamily = JetBrainsMono,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "$puzzleStreak days",
                            color = ThemeSlateAccent,
                            fontFamily = JetBrainsMono,
                            fontSize = 11.sp
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Play",
                        color = StatsCyan,
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = StatsCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1E293B))
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Close",
                    color = Color.White,
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun StreakMetricPill(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1E293B).copy(alpha = 0.5f))
            .border(1.dp, PuzzleCardBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = ThemeSlateAccent,
            fontFamily = JetBrainsMono,
            fontSize = 10.sp
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            color = Color.White,
            fontFamily = JetBrainsMono,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StreakDayItem(day: StreakDayDto) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = day.weekday.take(1).uppercase(),
            color = ThemeSlateAccent,
            fontFamily = JetBrainsMono,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )

        val (bgColor, borderColor, iconTint) = when (day.status.lowercase()) {
            "played" -> Triple(
                Color(0xFFE28339).copy(alpha = 0.2f),
                Color(0xFFE28339),
                Color(0xFFE28339)
            )
            "frozen" -> Triple(
                StatsCyan.copy(alpha = 0.2f),
                StatsCyan,
                StatsCyan
            )
            else -> Triple(
                Color(0xFF1E293B),
                PuzzleCardBorder,
                ThemeSlateAccent.copy(alpha = 0.4f)
            )
        }

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(bgColor)
                .border(1.dp, borderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            when (day.status.lowercase()) {
                "played" -> Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
                "frozen" -> Icon(
                    imageVector = Icons.Default.AcUnit,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
                else -> Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(iconTint)
                )
            }
        }
    }
}
