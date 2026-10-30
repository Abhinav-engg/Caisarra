package com.abhinav.caisarra.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.abhinav.caisarra.presentation.viewmodel.LeaderboardType
import com.abhinav.caisarra.presentation.viewmodel.LeaderboardViewModel
import com.abhinav.caisarra.core.ui.components.CaisaaraTopBar
private val BoardBg = Color(0xFF10151F)
private val BoardCard = Color(0xFF1A2230)
private val BoardBorder = Color(0xFF303B4D)
private val BoardCyan = Color(0xFF54D6E8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    onBack: () -> Unit,
    viewModel: LeaderboardViewModel,
    onPlayerClick: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.retry() },
        modifier = Modifier
            .fillMaxSize()
            .background(BoardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BoardBg)
                .statusBarsPadding()
        ) {

            CaisaaraTopBar(
                title = "LEADERBOARD",
                onBack = onBack,
                backgroundColor = BoardBg,
                titleColor = Color.White,
                iconTint = Color.White,
                titleFontSize = 18.sp
            )

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
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            fontSize = 12.sp,
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
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        SectionHeading(
                            title = "GLOBAL RANKINGS",
                            subtitle = "Ranked by ${state.globalMode.replaceFirstChar { it.uppercase() }} rating"
                        )

                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("rapid", "blitz", "bullet", "daily").forEach { mode ->
                                StyledModeChip(
                                    label = mode.replaceFirstChar { it.uppercase() },
                                    selected = state.globalMode == mode,
                                    onClick = { viewModel.selectGlobalMode(mode) }
                                )
                            }
                        }
                    }
                }

                LeaderboardType.CLUB -> {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SectionHeading(
                            title = "CLUB STANDINGS",
                            subtitle = if (state.selectedClub != null) {
                                "Viewing ${state.selectedClub?.name.orEmpty()} (${state.clubMode.replaceFirstChar { it.uppercase() }})"
                            } else {
                                "Compare rankings with club members"
                            }
                        )


                        if (state.myClubs.isNotEmpty()) {
                            Text(
                                "My Clubs",
                                color = Color.LightGray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                state.myClubs.forEach { club ->
                                    val isSelected = state.selectedClub?.effectiveId == club.effectiveId ||
                                            state.clubId.equals(club.effectiveId, ignoreCase = true) ||
                                            state.clubId.equals(club.shareCode, ignoreCase = true)

                                    Surface(
                                        onClick = { viewModel.selectClub(club) },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) BoardCyan.copy(alpha = 0.18f) else BoardCard,
                                        border = BorderStroke(1.dp, if (isSelected) BoardCyan else BoardBorder)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Groups,
                                                contentDescription = null,
                                                tint = if (isSelected) BoardCyan else Color.LightGray,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                club.name,
                                                color = if (isSelected) BoardCyan else Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }


                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("rapid", "blitz", "bullet", "daily").forEach { mode ->
                                StyledModeChip(
                                    label = mode.replaceFirstChar { it.uppercase() },
                                    selected = state.clubMode == mode,
                                    onClick = { viewModel.selectClubMode(mode) }
                                )
                            }
                        }


                        var isSearchExpanded by remember { mutableStateOf(false) }

                        if (!isSearchExpanded && state.myClubs.isNotEmpty()) {
                            TextButton(
                                onClick = { isSearchExpanded = true },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = BoardCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "Search another club by ID or share code",
                                    color = BoardCyan,
                                    fontSize = 12.sp
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = state.clubId,
                                    onValueChange = viewModel::setClubId,
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    label = { Text("Club ID, Code, or Name", color = Color.LightGray, fontSize = 12.sp) },
                                    placeholder = { Text("e.g. 12 or SHARECODE", color = Color.Gray, fontSize = 12.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        cursorColor = BoardCyan,
                                        focusedBorderColor = BoardCyan,
                                        unfocusedBorderColor = BoardBorder
                                    )
                                )

                                Button(
                                    onClick = { viewModel.loadClubLeaderboard() },
                                    colors = ButtonDefaults.buttonColors(containerColor = BoardCyan, contentColor = Color.Black),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Load", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                LeaderboardType.PUZZLE_RUSH -> {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        SectionHeading(
                            title = "PUZZLE RUSH CHAMPIONS",
                            subtitle = "Highest scores in speed puzzle solving"
                        )

                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "3min" to "3 Minutes",
                                "5min" to "5 Minutes",
                                "survival" to "Survival"
                            ).forEach { (mode, label) ->
                                StyledModeChip(
                                    label = label,
                                    selected = state.puzzleMode == mode,
                                    onClick = { viewModel.selectPuzzleMode(mode) }
                                )
                            }
                        }
                    }
                }
            }


            if (state.isLoading) {
                Box(
                    modifier = Modifier
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
                        text = state.error.orEmpty(),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = viewModel::retry,
                        colors = ButtonDefaults.buttonColors(containerColor = BoardCyan, contentColor = Color.Black)
                    ) {
                        Text("Retry", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                when (state.selectedType) {
                    LeaderboardType.GLOBAL -> {
                        if (state.globalEntries.isEmpty()) {
                            EmptyLeaderboard(
                                message = "No players ranked in ${state.globalMode.replaceFirstChar { it.uppercase() }} mode yet.",
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(
                                    state.globalEntries,
                                    key = { index, item ->
                                        "${item.userId}-$index"
                                    }
                                ) { index, entry ->
                                    val rank = if (entry.rank > 0) entry.rank else index + 1
                                    RankingRow(
                                        rank = rank,
                                        username = entry.username,
                                        score = entry.rating,
                                        label = state.globalMode.uppercase(),
                                        onClick = {
                                            if (entry.username.isNotBlank()) {
                                                onPlayerClick(entry.username)
                                            }
                                        }
                                    )
                                }

                                if (state.hasMoreGlobal) {
                                    item {
                                        OutlinedButton(
                                            onClick = viewModel::loadMoreGlobalLeaderboard,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp),
                                            enabled = !state.isLoadingMore,
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BoardCyan),
                                            border = BorderStroke(1.dp, BoardCyan)
                                        ) {
                                            if (state.isLoadingMore) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(18.dp),
                                                    color = BoardCyan
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
                            val emptyMsg = if (state.clubId.isBlank() && state.myClubs.isEmpty()) {
                                "You haven't joined any clubs yet.\nEnter a club ID or share code to view rankings."
                            } else {
                                "No rankings available for this club in ${state.clubMode.replaceFirstChar { it.uppercase() }} mode."
                            }
                            EmptyLeaderboard(
                                message = emptyMsg,
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(
                                    state.clubEntries,
                                    key = { index, item ->
                                        "${item.userId}-$index"
                                    }
                                ) { index, entry ->
                                    val rank = if (entry.rank > 0) entry.rank else index + 1
                                    RankingRow(
                                        rank = rank,
                                        username = entry.username,
                                        score = entry.rating,
                                        label = state.clubMode.uppercase(),
                                        onClick = {
                                            if (entry.username.isNotBlank()) {
                                                onPlayerClick(entry.username)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    LeaderboardType.PUZZLE_RUSH -> {
                        if (state.puzzleRushEntries.isEmpty()) {
                            EmptyLeaderboard(
                                message = "No puzzle rush entries yet for this mode.",
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(
                                    state.puzzleRushEntries,
                                    key = { index, item ->
                                        "${item.userId}-$index"
                                    }
                                ) { index, entry ->
                                    RankingRow(
                                        rank = index + 1,
                                        username = entry.username,
                                        score = entry.score,
                                        label = "SCORE",
                                        onClick = {
                                            if (entry.username.isNotBlank()) {
                                                onPlayerClick(entry.username)
                                            }
                                        }
                                    )
                                }
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
    Column {
        Text(
            title,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = JetBrainsMono
        )
        Spacer(Modifier.height(2.dp))
        Text(subtitle, color = Color.LightGray, fontSize = 12.sp)
    }
}

@Composable
private fun StyledModeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) BoardCyan.copy(alpha = 0.2f) else BoardCard)
            .border(
                1.dp,
                if (selected) BoardCyan else BoardBorder,
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) BoardCyan else Color.LightGray
        )
    }
}

@Composable
private fun RankingRow(
    rank: Int,
    username: String,
    score: Int,
    label: String,
    onClick: (() -> Unit)? = null
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
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
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
            if (rank in 1..3) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = "Rank $rank",
                    tint = rankColor,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    rank.toString(),
                    color = rankColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
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
            Text(label, color = Color.LightGray, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun EmptyLeaderboard(
    message: String = "No leaderboard entries available yet.",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = Color.LightGray,
            textAlign = TextAlign.Center,
            fontSize = 14.sp
        )
    }
}


