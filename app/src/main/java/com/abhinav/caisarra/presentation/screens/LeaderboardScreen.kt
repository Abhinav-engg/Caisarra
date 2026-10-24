
package com.abhinav.caisarra.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abhinav.caisarra.presentation.viewmodel.LeaderboardType
import com.abhinav.caisarra.presentation.viewmodel.LeaderboardViewModel
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.caisaara.ui.theme.*

private val BoardBg = Color(0xFF10151F)
private val BoardCard = Color(0xFF1A2230)
private val BoardBorder = Color(0xFF303B4D)
private val BoardCyan = Color(0xFF54D6E8)

@Composable
fun LeaderboardScreen(
    onBack: () -> Unit,
    viewModel: LeaderboardViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BoardBg)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Column(Modifier.weight(1f)) {
                Text(
                    "Leaderboard",
                    fontFamily = JetBrainsMono,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "Compete. Improve. Climb.",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }

            IconButton(onClick = viewModel::retry) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = BoardCyan
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                LeaderboardType.GLOBAL to "Global",
                LeaderboardType.CLUB to "Club",
                LeaderboardType.PUZZLE_RUSH to "Puzzle Rush"
            ).forEach { (type, label) ->
                val selected = state.selectedType == type

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (selected) BoardCyan.copy(alpha = 0.15f)
                            else BoardCard
                        )
                        .border(
                            1.dp,
                            if (selected) BoardCyan else BoardBorder,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { viewModel.selectType(type) }
                        .padding(vertical = 13.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) BoardCyan else Color.LightGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        when (state.selectedType) {
            LeaderboardType.GLOBAL -> {
                SectionHeading(
                    title = "GLOBAL DAILY RATINGS",
                    subtitle = "Ranked by daily-mode rating"
                )
            }

            LeaderboardType.CLUB -> {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "CLUB STANDINGS",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )


                    OutlinedTextField(
                        value = state.clubId,
                        onValueChange = viewModel::setClubId,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = {
                            Text(
                                "Club ID or Share Code",
                                color = Color(0xFFB69CFF)
                            )
                        },
                        placeholder = {
                            Text("Enter club ID or share code")
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Ascii
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = BoardCyan,
                            focusedBorderColor = Color(0xFF9B78E8),
                            unfocusedBorderColor = BoardBorder
                        )
                    )


                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        listOf("bullet", "blitz", "rapid", "daily").forEach { mode ->
                            FilterChip(
                                selected = state.clubMode == mode,
                                onClick = {
                                    viewModel.loadClubLeaderboard(
                                        state.clubId,
                                        mode
                                    )
                                },
                                label = { Text(mode) }
                            )
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.loadClubLeaderboard()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Load Club Rankings")
                    }
                }
            }

            LeaderboardType.PUZZLE_RUSH -> {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "PUZZLE RUSH CHAMPIONS",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("3min", "5min", "survival").forEach { mode ->
                            FilterChip(
                                selected = state.puzzleMode == mode,
                                onClick = {
                                    viewModel.loadPuzzleRushLeaderboard(mode)
                                },
                                label = {
                                    Text(
                                        when (mode) {
                                            "3min" -> "3 Minutes"
                                            "5min" -> "5 Minutes"
                                            else -> "Survival"
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        if (state.isLoading) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BoardCyan)
            }
        } else if (state.error != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    state.error.orEmpty(),
                    color = Color.White
                )
                Spacer(Modifier.height(12.dp))
                Button(onClick = viewModel::retry) {
                    Text("Retry")
                }
            }
        } else {
            when (state.selectedType) {
                LeaderboardType.GLOBAL -> {
                    if (state.globalEntries.isEmpty()) {
                        EmptyLeaderboard()
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(
                                state.globalEntries,
                                key = { index, item ->
                                    "${item.user_id}-$index"
                                }
                            ) { _, entry ->
                                RankingRow(
                                    rank = entry.rank,
                                    username = entry.username,
                                    score = entry.rating,
                                    label = "RATING"
                                )
                            }

                            if (state.hasMoreGlobal) {
                                item {
                                    OutlinedButton(
                                        onClick = viewModel::loadMoreGlobalLeaderboard,
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = !state.isLoadingMore
                                    ) {
                                        if (state.isLoadingMore) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(18.dp)
                                            )
                                        } else {
                                            Text("Load More Players")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                LeaderboardType.CLUB -> {
                    if (state.clubEntries.isEmpty()) {
                        EmptyLeaderboard()
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(
                                state.clubEntries,
                                key = { index, item ->
                                    "${item.user_id}-$index"
                                }
                            ) { _, entry ->
                                RankingRow(
                                    rank = entry.rank,
                                    username = entry.username,
                                    score = entry.rating,
                                    label = state.clubMode.uppercase()
                                )
                            }
                        }
                    }
                }

                LeaderboardType.PUZZLE_RUSH -> {
                    if (state.puzzleRushEntries.isEmpty()) {
                        EmptyLeaderboard()
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(
                                state.puzzleRushEntries,
                                key = { index, item ->
                                    "${item.user_id}-$index"
                                }
                            ) { index, entry ->
                                RankingRow(
                                    rank = index + 1,
                                    username = entry.username,
                                    score = entry.score,
                                    label = "SCORE"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeading(
    title: String,
    subtitle: String
) {
    Column(
        Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            title,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(subtitle, color = Color.LightGray, fontSize = 12.sp)
    }
}

@Composable
private fun RankingRow(
    rank: Int,
    username: String,
    score: Int,
    label: String
) {
    val rankColor = when (rank) {
        1 -> Color(0xFFFFD166)
        2 -> Color(0xFFC5D0DF)
        3 -> Color(0xFFD69A72)
        else -> Color.LightGray
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BoardCard)
            .border(1.dp, BoardBorder, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(rankColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            if (rank <= 3) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = "Rank $rank",
                    tint = rankColor
                )
            } else {
                Text(
                    rank.toString(),
                    color = rankColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Text(
            username.ifBlank { "Unknown player" },
            color = Color.White,
            fontFamily = JetBrainsMono,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Spacer(Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                score.toString(),
                color = BoardCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
            Text(label, color = Color.LightGray, fontSize = 9.sp)
        }
    }
}

@Composable
private fun EmptyLeaderboard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "No leaderboard entries available yet.",
            color = Color.LightGray
        )
    }
}
