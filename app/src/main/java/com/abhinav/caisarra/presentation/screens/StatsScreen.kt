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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono

private val BackgroundColor = Color(0xFF080D13)
private val CardBg = Color(0xFF0F1722)
private val CardBorderColor = Color(0xFF1B2838)
private val TextWhiteColor = Color(0xFFF1F5F9)
private val TextMutedColor = Color(0xFF82909E)
private val CyanColor = Color(0xFF23CDE8)

@Composable
fun StatsScreen(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
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
                    .background(Color(0xFF101924))
                    .border(
                        width = 1.dp,
                        color = Color(0xFF202E3E),
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
                        tint = TextWhiteColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = "Stats",
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = TextWhiteColor
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mode Rating Cards Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Rapid",
                    rating = "1200",
                    games = "0 games",
                    icon = Icons.Default.Timer,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Blitz",
                    rating = "1200",
                    games = "0 games",
                    icon = Icons.Default.Bolt,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Bullet",
                    rating = "1200",
                    games = "0 games",
                    icon = Icons.Default.Speed,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Puzzles",
                    rating = "1500",
                    games = "0 solved",
                    icon = Icons.Default.Extension,
                    modifier = Modifier.weight(1f)
                )
            }

            // Overview Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardBg)
                    .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoGraph,
                            contentDescription = null,
                            tint = CyanColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Performance Summary",
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextWhiteColor
                        )
                    }

                    Text(
                        text = "Detailed rating progression charts and win/loss breakdowns will be populated as you play ranked games.",
                        fontFamily = JetBrainsMono,
                        fontSize = 13.sp,
                        color = TextMutedColor,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        SummaryPill(label = "Won", value = "0", color = Color(0xFF34D399))
                        SummaryPill(label = "Drawn", value = "0", color = Color(0xFF94A4B8))
                        SummaryPill(label = "Lost", value = "0", color = Color(0xFFF87171))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    rating: String,
    games: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CardBg)
            .border(1.dp, CardBorderColor, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontFamily = JetBrainsMono,
                    fontSize = 14.sp,
                    color = TextMutedColor,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CyanColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = rating,
                fontFamily = JetBrainsMono,
                fontSize = 22.sp,
                color = TextWhiteColor,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = games,
                fontFamily = JetBrainsMono,
                fontSize = 11.sp,
                color = TextMutedColor
            )
        }
    }
}

@Composable
private fun SummaryPill(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = color
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontFamily = JetBrainsMono,
            fontSize = 11.sp,
            color = TextMutedColor
        )
    }
}
