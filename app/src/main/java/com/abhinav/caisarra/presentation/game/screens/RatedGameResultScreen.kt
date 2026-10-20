package com.abhinav.caisarra.presentation.game.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.data.remote.dto.AnalysisSummaryStatsDto
import com.abhinav.caisarra.presentation.game.model.GameResult
import com.abhinav.caisarra.presentation.game.model.GameUiState
import com.abhinav.caisarra.presentation.game.model.PieceColor
import com.abhinav.caisarra.presentation.game.viewmodel.GameAnalysisViewModel

private val ScreenBg = Color(0xFF0B1218)
private val Card = Color(0xFF121A22)
private val Border = Color(0xFF2A3848)
private val TextMain = Color(0xFFF4F7FB)
private val Muted = Color(0xFF8FA0B3)
private val Cyan = Color(0xFF3AD6F0)
private val Win = Color(0xFF45E5AF)
private val Loss = Color(0xFFF08D96)
private val Draw = Color(0xFF57CDEA)

private enum class ResultOutcome { VICTORY, DEFEAT, DRAW }

@Composable
fun RatedGameResultScreen(
    state: GameUiState,
    result: GameResult,
    rematchBusy: Boolean,
    onClose: () -> Unit,
    onShare: () -> Unit,
    onStartAnalysis: () -> Unit,
    onRematch: () -> Unit,
    onNewOpponent: () -> Unit,
    onSavePgn: () -> Unit,
    analysisViewModel: GameAnalysisViewModel
) {
    val analysis by analysisViewModel.state.collectAsState()
    LaunchedEffect(Unit) { analysisViewModel.start() }

    val outcome = outcomeFor(result, state.myColor)
    val accent = when (outcome) {
        ResultOutcome.VICTORY -> Win
        ResultOutcome.DEFEAT -> Loss
        ResultOutcome.DRAW -> Draw
    }
    val title = when (outcome) {
        ResultOutcome.VICTORY -> "Victory!"
        ResultOutcome.DEFEAT -> "Defeat"
        ResultOutcome.DRAW -> "Draw"
    }
    val icon = when (outcome) {
        ResultOutcome.VICTORY -> Icons.Default.EmojiEvents
        ResultOutcome.DEFEAT -> Icons.Default.Flag
        ResultOutcome.DRAW -> Icons.Default.Handshake
    }
    val previousRating = state.playerRating?.let { current ->
        state.ratingChange?.let { current - it }
    }
    val iAmWhite = state.myColor != PieceColor.BLACK
    val myChange = state.ratingChange
    val opponentChange = myChange?.let { -it }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
            .systemBarsPadding()
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleIconButton(Icons.Default.Close, "Close", onClose)
            CircleIconButton(Icons.Default.Share, "Share", onShare)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .border(1.dp, accent.copy(alpha = 0.55f), CircleShape)
                    .background(accent.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = accent, modifier = Modifier.size(30.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(title, color = TextMain, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(14.dp))
            EloPill(
                change = myChange,
                current = state.playerRating,
                previous = previousRating,
                accent = accent
            )
            Spacer(Modifier.height(18.dp))
            PlayersRow(
                leftName = if (iAmWhite) state.whitePlayerName else state.blackPlayerName,
                leftRating = if (iAmWhite) state.whiteRating else state.blackRating,
                leftChange = myChange,
                leftHighlight = true,
                rightName = if (iAmWhite) state.blackPlayerName else state.whitePlayerName,
                rightRating = if (iAmWhite) state.blackRating else state.whiteRating,
                rightChange = opponentChange,
                accent = accent
            )
            Spacer(Modifier.height(14.dp))
            StatsRow(analysis.summary?.summaryStats, iAmWhite)
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Cyan)
                    .clickable(onClick = onStartAnalysis),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Start Game Analysis",
                        color = Color(0xFF082028),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.size(8.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFF082028)
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlineAction(
                    label = if (rematchBusy) "Inviting..." else "Rematch",
                    icon = Icons.Default.Refresh,
                    modifier = Modifier.weight(1f),
                    enabled = !rematchBusy,
                    onClick = onRematch
                )
                OutlineAction(
                    label = "New Opponent",
                    icon = Icons.Default.PersonSearch,
                    modifier = Modifier.weight(1f),
                    onClick = onNewOpponent
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp, top = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextAction(Icons.Default.Download, "Save PGN", onSavePgn)
                TextAction(Icons.Default.Share, "Share", onShare)
            }
        }
    }
}

@Composable
private fun CircleIconButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .border(1.dp, Border, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = label, tint = TextMain, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun EloPill(change: Int?, current: Int?, previous: Int?, accent: Color) {
    val changeText = when {
        change == null -> "Rated"
        change > 0 -> "↑ +$change Elo"
        change < 0 -> "↓ $change Elo"
        else -> "Elo unchanged"
    }
    Row(
        modifier = Modifier
            .background(Card, RoundedCornerShape(24.dp))
            .border(1.dp, Border, RoundedCornerShape(24.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(changeText, color = accent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Box(Modifier.size(width = 1.dp, height = 12.dp).background(Border))
        Text(current?.toString() ?: "—", color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        if (previous != null && previous != current) {
            Text(
                previous.toString(),
                color = Muted,
                fontSize = 12.sp,
                textDecoration = TextDecoration.LineThrough
            )
        }
    }
}

@Composable
private fun PlayersRow(
    leftName: String,
    leftRating: Int?,
    leftChange: Int?,
    leftHighlight: Boolean,
    rightName: String,
    rightRating: Int?,
    rightChange: Int?,
    accent: Color
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PlayerCard(
                name = leftName,
                rating = leftRating,
                change = leftChange,
                highlight = leftHighlight,
                accent = accent,
                modifier = Modifier.weight(1f)
            )
            PlayerCard(
                name = rightName,
                rating = rightRating,
                change = rightChange,
                highlight = false,
                accent = accent,
                modifier = Modifier.weight(1f)
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(28.dp)
                .background(Color(0xFF1B2530), CircleShape)
                .border(1.dp, Border, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("VS", color = Muted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PlayerCard(
    name: String,
    rating: Int?,
    change: Int?,
    highlight: Boolean,
    accent: Color,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .background(Card, RoundedCornerShape(16.dp))
            .border(1.dp, if (highlight) accent.copy(alpha = 0.7f) else Border, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Text(
            name,
            color = TextMain,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(4.dp))
        Text(
            buildString {
                append(rating?.toString() ?: "—")
                when {
                    change == null -> Unit
                    change > 0 -> append(" (+$change)")
                    else -> append(" ($change)")
                }
            },
            color = if (highlight) accent else Muted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun StatsRow(stats: AnalysisSummaryStatsDto?, iAmWhite: Boolean) {
    val accuracy = stats?.let { if (iAmWhite) it.accuracyWhite else it.accuracyBlack }
    val best = stats?.let { if (iAmWhite) it.bestWhite else it.bestBlack }
    val brilliant = stats?.let { if (iAmWhite) it.brilliantWhite else it.brilliantBlack }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatCard("ACCURACY", accuracy?.let { "%.1f%%".format(it) } ?: "—", Win, Modifier.weight(1f))
        StatCard("BRILLIANT", brilliant?.toString() ?: "—", Win, Modifier.weight(1f))
        StatCard("BEST MOVES", best?.toString() ?: "—", TextMain, Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(label: String, value: String, valueColor: Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .background(Card, RoundedCornerShape(16.dp))
            .border(1.dp, Border, RoundedCornerShape(16.dp))
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp)
        Spacer(Modifier.height(6.dp))
        Text(value, color = valueColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun OutlineAction(
    label: String,
    icon: ImageVector,
    modifier: Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Border, RoundedCornerShape(16.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (!enabled) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Cyan)
        } else {
            Icon(icon, contentDescription = null, tint = TextMain, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.size(8.dp))
        Text(label, color = TextMain, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun TextAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Muted, modifier = Modifier.size(16.dp))
        Text(label, color = Muted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

private fun outcomeFor(result: GameResult, playerColor: PieceColor?): ResultOutcome = when (result) {
    GameResult.DRAW -> ResultOutcome.DRAW
    GameResult.WHITE_WINS -> if (playerColor == PieceColor.BLACK) ResultOutcome.DEFEAT else ResultOutcome.VICTORY
    GameResult.BLACK_WINS -> if (playerColor == PieceColor.BLACK) ResultOutcome.VICTORY else ResultOutcome.DEFEAT
}