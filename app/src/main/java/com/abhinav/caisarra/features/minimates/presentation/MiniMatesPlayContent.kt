package com.abhinav.caisarra.features.minimates.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.core.ui.components.CaisaaraTopBar
import com.abhinav.caisarra.core.ui.components.GeneralButton
import com.abhinav.caisarra.presentation.game.components.ChessBoard
import com.abhinav.caisarra.presentation.game.model.Square
import com.caisaara.ui.theme.StatsBg
import com.caisaara.ui.theme.TextSizeSubhead
import com.caisaara.ui.theme.White

@Composable
fun MiniMatesPlayContent(
    state: MiniMatesUiState,
    onSquareClick: (Square) -> Unit,
    onMoveAnimationStarted: (Long) -> Unit,
    onHint: () -> Unit,
    onRetry: () -> Unit,
    onNext: () -> Unit,
    onLevels: () -> Unit,
    onBack: () -> Unit
) {
    val level = state.level ?: return
    val viewport = level.viewport
    val message = when (state.status) {
        MiniMatesStatus.PLAYING -> state.note ?: "Mate in ${level.targetMoves - state.moveIndex}"
        MiniMatesStatus.THINKING -> "Checking…"
        MiniMatesStatus.SOLVED -> "Checkmate! ${starsText(state.stars)}"
        MiniMatesStatus.FAILED -> "Not the mate. Try again"
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StatsBg)
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CaisaaraTopBar(title = level.title.uppercase(), onBack = onBack) {
            IconButton(onClick = onLevels) { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Levels", tint = White) }
        }
        AnimatedContent(targetState = message, label = "message") { text -> Text(
            text = text,
            color = White,
            fontSize = TextSizeSubhead,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) }
        Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.aspectRatio(viewport.cols / viewport.rows.toFloat())) {
                ChessBoard(
                    board = state.board,
                    selectedSquare = state.selected,
                    legalMoves = state.targets,
                    lastMoveFrom = state.lastFrom,
                    lastMoveTo = state.lastTo,
                    isWhiteInCheck = state.whiteInCheck,
                    isBlackInCheck = state.blackInCheck,
                    isFlipped = level.playerColor == "black",
                    boardSize = viewport.rows,
                    columns = viewport.cols,
                    origin = Square(row = 8 - viewport.endRank, column = viewport.startFile.first() - 'a'),
                    moveAnimationEvent = state.moveAnimation,
                    moveAnimationConsumedId = state.moveAnimationConsumedId,
                    onMoveAnimationStarted = onMoveAnimationStarted,
                    moveFeedback = state.moveFeedback,
                    onSquareClick = onSquareClick
                )
            }
        }
        val buttonModifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        AnimatedContent(targetState = state.status, contentKey = { it.takeUnless { s -> s == MiniMatesStatus.THINKING } ?: MiniMatesStatus.PLAYING }, label = "action") { status ->
            when (status) {
                MiniMatesStatus.PLAYING, MiniMatesStatus.THINKING -> GeneralButton(text = "Hint", onClick = onHint, modifier = buttonModifier, enabled = status == MiniMatesStatus.PLAYING)
                MiniMatesStatus.SOLVED -> GeneralButton(text = if (state.nextLevel != null) "Next Level" else "All Levels", onClick = onNext, modifier = buttonModifier)
                MiniMatesStatus.FAILED -> GeneralButton(text = "Retry", onClick = onRetry, modifier = buttonModifier)
            }
        }
    }
}
