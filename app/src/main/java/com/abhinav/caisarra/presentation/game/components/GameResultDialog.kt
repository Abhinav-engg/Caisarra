package com.abhinav.caisarra.presentation.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.abhinav.caisarra.core.ui.components.GeneralButton
import com.abhinav.caisarra.presentation.game.model.GameResult
import com.abhinav.caisarra.presentation.game.model.PieceColor

private val ResultSurface = Color(0xFF111820)
private val ResultCard = Color(0xFF17212B)
private val ResultBorder = Color(0xFF33414E)
private val ResultMuted = Color(0xFFAAB7C6)
private val ResultWin = Color(0xFF45E5AF)
private val ResultLoss = Color(0xFFF08D96)

@Composable
fun GameResultDialog(
    result: GameResult,
    onNewGame: () -> Unit,
    onHome: () -> Unit,
    playerColor: PieceColor? = null,
    playerRating: Int? = null,
    ratingChange: Int? = null,
    isRated: Boolean = false,
    whitePlayerName: String = "White",
    blackPlayerName: String = "Black"
) {
    val outcome = outcomeFor(result, playerColor)
    val outcomeColor = when (outcome) {
        ResultOutcome.VICTORY -> ResultWin
        ResultOutcome.DEFEAT -> ResultLoss
        ResultOutcome.DRAW -> Color(0xFF57CDEA)
    }
    val title = if (playerColor == null) {
        when (result) {
            GameResult.WHITE_WINS -> "White Wins"
            GameResult.BLACK_WINS -> "Black Wins"
            GameResult.DRAW -> "Draw"
        }
    } else when (outcome) {
        ResultOutcome.VICTORY -> "Victory!"
        ResultOutcome.DEFEAT -> "Defeat"
        ResultOutcome.DRAW -> "Draw"
    }
    val subtitle = when {
        !isRated -> "Friendly game - rating unchanged"
        ratingChange == null -> "Rating updated after this rated game"
        ratingChange > 0 -> "Your rating increased"
        ratingChange < 0 -> "Your rating changed"
        else -> "Your rating is unchanged"
    }

    Dialog(onDismissRequest = {}, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        BoxWithConstraints(Modifier.fillMaxWidth().padding(20.dp)) {
            Column(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .verticalScroll(rememberScrollState())
                    .background(ResultSurface, RoundedCornerShape(28.dp))
                    .border(1.dp, ResultBorder, RoundedCornerShape(28.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(outcomeColor.copy(alpha = 0.12f), CircleShape)
                        .border(1.dp, outcomeColor.copy(alpha = 0.5f), CircleShape)
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (outcome) {
                            ResultOutcome.VICTORY -> Icons.Default.EmojiEvents
                            ResultOutcome.DEFEAT -> Icons.Default.Flag
                            ResultOutcome.DRAW -> Icons.Default.Handshake
                        },
                        contentDescription = null,
                        tint = outcomeColor
                    )
                }
                Text(title, color = Color(0xFFF4F7FB), fontSize = 30.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text(subtitle, color = ResultMuted, textAlign = TextAlign.Center, fontSize = 14.sp)

                ResultPlayers(result, whitePlayerName, blackPlayerName, outcomeColor)

                playerRating?.let {
                    RatingSummary(it, ratingChange, isRated, outcomeColor)
                }

                GeneralButton(text = "NEW GAME", onClick = onNewGame)
                Text(
                    text = "HOME",
                    color = Color(0xFFD5DCE6),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(onClick = onHome).padding(12.dp)
                )
            }
        }
    }
}

@Composable
private fun ResultPlayers(result: GameResult, whiteName: String, blackName: String, highlight: Color) {
    val whiteWon = result == GameResult.WHITE_WINS
    val blackWon = result == GameResult.BLACK_WINS
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ResultCard, RoundedCornerShape(18.dp))
            .border(1.dp, ResultBorder, RoundedCornerShape(18.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ResultPlayer(whiteName, "WHITE", whiteWon, highlight, Modifier.weight(1f))
        Text("VS", color = ResultMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
        ResultPlayer(blackName, "BLACK", blackWon, highlight, Modifier.weight(1f))
    }
}

@Composable
private fun ResultPlayer(name: String, color: String, won: Boolean, highlight: Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .background(if (won) highlight.copy(alpha = 0.10f) else Color.Transparent, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Text(name, color = Color(0xFFF4F7FB), fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(color, color = ResultMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RatingSummary(rating: Int, change: Int?, rated: Boolean, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ResultCard, RoundedCornerShape(18.dp))
            .border(1.dp, ResultBorder, RoundedCornerShape(18.dp))
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("CURRENT RATING", color = ResultMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("$rating", color = Color(0xFFF4F7FB), fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
        Text(
            text = when {
                !rated -> "UNRATED"
                change == null -> "RATED"
                change > 0 -> "+$change"
                else -> change.toString()
            },
            color = if (rated && change != null) color else ResultMuted,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private enum class ResultOutcome { VICTORY, DEFEAT, DRAW }

private fun outcomeFor(result: GameResult, playerColor: PieceColor?): ResultOutcome = when (result) {
    GameResult.DRAW -> ResultOutcome.DRAW
    GameResult.WHITE_WINS -> if (playerColor == PieceColor.WHITE) ResultOutcome.VICTORY else ResultOutcome.DEFEAT
    GameResult.BLACK_WINS -> if (playerColor == PieceColor.BLACK) ResultOutcome.VICTORY else ResultOutcome.DEFEAT
}
