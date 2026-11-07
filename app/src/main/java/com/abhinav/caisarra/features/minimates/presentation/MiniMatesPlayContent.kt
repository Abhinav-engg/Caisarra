package com.abhinav.caisarra.features.minimates.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.core.ui.components.CaisaaraTopBar
import com.abhinav.caisarra.core.ui.components.GeneralButton
import com.abhinav.caisarra.presentation.game.components.ChessBoard
import com.abhinav.caisarra.features.minimates.domain.MiniMatesEngine
import com.abhinav.caisarra.presentation.game.model.PieceColor
import com.abhinav.caisarra.presentation.game.model.Square
import com.caisaara.ui.theme.StatsBg
import com.caisaara.ui.theme.TextSizeSubhead
import com.caisaara.ui.theme.White

@Composable
fun MiniMatesPlayContent(
    state: MiniMatesUiState,
    onSquareClick: (Square) -> Unit,
    onRetry: () -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val position = state.position ?: return
    val message = when (state.status) {
        MiniMatesStatus.PLAYING -> "White to mate in ${state.movesLeft}"
        MiniMatesStatus.THINKING -> "Black is thinking…"
        MiniMatesStatus.SOLVED -> "Checkmate! Solved"
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
        CaisaaraTopBar(title = "LEVEL ${state.level?.number}", onBack = onBack)
        Text(text = message, color = White, fontSize = TextSizeSubhead, fontWeight = FontWeight.SemiBold)
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).aspectRatio(1f)) {
            ChessBoard(
                board = position.pieces,
                selectedSquare = state.selected,
                legalMoves = state.targets,
                lastMoveFrom = state.lastMove?.from,
                lastMoveTo = state.lastMove?.to,
                isWhiteInCheck = false,
                isBlackInCheck = MiniMatesEngine.isInCheck(position, PieceColor.BLACK),
                isFlipped = false,
                boardSize = position.size,
                onSquareClick = onSquareClick
            )
        }
        when (state.status) {
            MiniMatesStatus.SOLVED -> GeneralButton(text = "Next Level", onClick = onNext, modifier = Modifier.padding(horizontal = 16.dp))
            MiniMatesStatus.FAILED -> GeneralButton(text = "Retry", onClick = onRetry, modifier = Modifier.padding(horizontal = 16.dp))
            else -> Unit
        }
    }
}
