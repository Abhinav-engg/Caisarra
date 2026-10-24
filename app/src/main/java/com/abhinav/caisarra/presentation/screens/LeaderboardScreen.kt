package com.abhinav.caisarra.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.abhinav.caisarra.core.ui.theme.StatsCardCornerRadius
import com.abhinav.caisarra.core.ui.theme.StatsCardItemSpacing
import com.abhinav.caisarra.core.ui.theme.StatsScreenPadding
import com.abhinav.caisarra.core.ui.theme.StatsSectionSpacing
import com.caisaara.ui.theme.BronzeColor
import com.caisaara.ui.theme.GoldColor
import com.caisaara.ui.theme.LeaderboardRowBg
import com.caisaara.ui.theme.SilverColor
import com.caisaara.ui.theme.StatsBg
import com.caisaara.ui.theme.StatsCardBg
import com.caisaara.ui.theme.StatsCardBorder
import com.caisaara.ui.theme.StatsCyan
import com.caisaara.ui.theme.StatsTabBg
import com.caisaara.ui.theme.StatsTabBorder
import com.caisaara.ui.theme.StatsTextMuted
import com.caisaara.ui.theme.StatsTextWhite
import com.caisaara.ui.theme.TextSizeBadge
import com.caisaara.ui.theme.TextSizeBody
import com.caisaara.ui.theme.TextSizeHeading
import com.caisaara.ui.theme.TextSizeMedium

@Composable
fun LeaderboardScreen(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StatsBg)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(StatsTabBg)
                    .border(
                        width = 1.dp,
                        color = StatsTabBorder,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = StatsTextWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = "Leaderboard",
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Bold,
                fontSize = TextSizeHeading,
                color = StatsTextWhite
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(StatsScreenPadding),
            verticalArrangement = Arrangement.spacedBy(StatsSectionSpacing)
        ) {
            Text(
                text = "Top ranked players across all competitive modes.",
                fontFamily = JetBrainsMono,
                fontSize = TextSizeBadge,
                color = StatsTextMuted
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(StatsCardCornerRadius))
                    .background(StatsCardBg)
                    .border(1.dp, StatsCardBorder, RoundedCornerShape(StatsCardCornerRadius))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(StatsCardItemSpacing)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = GoldColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Global Standings",
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Bold,
                            fontSize = TextSizeMedium,
                            color = StatsTextWhite
                        )
                    }

                    LeaderboardRow(rank = "1", name = "GrandmasterAlex", rating = "2480", badgeColor = GoldColor)
                    LeaderboardRow(rank = "2", name = "TacticsTitan", rating = "2395", badgeColor = SilverColor)
                    LeaderboardRow(rank = "3", name = "KnightRider99", rating = "2310", badgeColor = BronzeColor)
                    LeaderboardRow(rank = "4", name = "CheckmateQueen", rating = "2250", badgeColor = StatsTextMuted)
                    LeaderboardRow(rank = "5", name = "EndgameGuru", rating = "2180", badgeColor = StatsTextMuted)
                }
            }
        }
    }
}

@Composable
private fun LeaderboardRow(
    rank: String,
    name: String,
    rating: String,
    badgeColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(LeaderboardRowBg)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(badgeColor.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = rank,
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Bold,
                fontSize = TextSizeBadge,
                color = badgeColor
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = name,
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Medium,
            fontSize = TextSizeBody,
            color = StatsTextWhite,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = rating,
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Bold,
            fontSize = TextSizeBody,
            color = StatsCyan
        )
    }
}
