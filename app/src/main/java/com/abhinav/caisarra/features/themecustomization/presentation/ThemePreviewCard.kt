package com.abhinav.caisarra.features.themecustomization.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.abhinav.caisarra.core.ui.components.SettingsCard
import com.abhinav.caisarra.features.themecustomization.domain.PreviewPosition
import com.abhinav.caisarra.presentation.game.components.ChessBoard
import com.abhinav.caisarra.presentation.game.model.BoardTheme
import com.abhinav.caisarra.presentation.game.model.PieceTheme

@Composable
fun ThemePreviewCard(theme: BoardTheme, pieceTheme: PieceTheme) {
    SettingsCard {
        Box(modifier = Modifier.aspectRatio(1f)) {
            ChessBoard(
                board = PreviewPosition.board,
                selectedSquare = PreviewPosition.selected,
                legalMoves = PreviewPosition.legalMoves,
                lastMoveFrom = PreviewPosition.lastMoveFrom,
                lastMoveTo = PreviewPosition.lastMoveTo,
                isWhiteInCheck = false,
                isBlackInCheck = false,
                isFlipped = false,
                boardTheme = theme,
                pieceTheme = pieceTheme,
                onSquareClick = {}
            )
        }
    }
}
