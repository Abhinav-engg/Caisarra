package com.abhinav.caisarra.presentation.game.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abhinav.caisarra.presentation.game.components.CapturedPieces
import com.abhinav.caisarra.presentation.game.components.ChessBoard
import com.abhinav.caisarra.presentation.game.components.ClockBar
import com.abhinav.caisarra.presentation.game.components.GameResultDialog
import com.abhinav.caisarra.presentation.game.components.MoveList
import com.abhinav.caisarra.presentation.game.components.PromotionDialog
import com.abhinav.caisarra.presentation.game.model.GameIntent
import com.abhinav.caisarra.presentation.game.viewmodel.FakeGameViewModel

@Composable
fun GameScreen(
    viewModel: FakeGameViewModel = viewModel(),
    onHome: () -> Unit,
    onNewGame: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF080D13))
            .padding(
                horizontal = 12.dp,
                vertical = 10.dp
            )
    ) {

        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            TextButton(onClick = onHome
            ) {
                Text(text = "Home",
                    color = Color.White
                )
            }

            Text(text = "CAISARRA",
                color = Color.White,
                fontSize = 18.sp
            )

            TextButton(
                onClick = {
                    viewModel.onIntent(
                        GameIntent.FlipBoard
                    )
                }
            ) {
                Text(text = "Flip",
                    color = Color.White
                )
            }
        }

        ClockBar(
            playerName = state.blackPlayerName,
            timeMillis = state.blackTimeMillis,
            isActive = !state.isWhiteTurn
        )

        CapturedPieces(
            pieces = state.capturedBlackPieces
        )
        Spacer(modifier = Modifier.height(8.dp))

        ChessBoard(
            board = state.board,
            selectedSquare = state.selectedSquare,
            legalMoves = state.legalMoves,
            lastMoveFrom = state.lastMoveFrom,
            lastMoveTo = state.lastMoveTo,
            isWhiteInCheck = state.isWhiteInCheck,
            isBlackInCheck = state.isBlackInCheck,
            isFlipped = state.isBoardFlipped,
            onSquareClick = { square ->
                viewModel.onIntent(
                    GameIntent.SquareClicked(square)
                )
            }
        )
        Spacer(modifier = Modifier.height(8.dp))

        CapturedPieces(pieces = state.capturedWhitePieces)
        ClockBar(
            playerName = state.whitePlayerName,
            timeMillis = state.whiteTimeMillis,
            isActive = state.isWhiteTurn
        )

        Spacer(modifier = Modifier.height(8.dp))

        MoveList(
            moves = state.moves,
            modifier = Modifier.weight(1f)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceEvenly
        ) {

            TextButton(
                onClick = {
                    viewModel.onIntent(
                        GameIntent.Undo
                    )
                }
            ) {
                Text(
                    text = "Undo",
                    color = Color.White
                )
            }

            TextButton(
                onClick = {
                    viewModel.onIntent(
                        GameIntent.OfferDraw
                    )
                }
            ) {
                Text(
                    text = "Draw",
                    color = Color.White
                )
            }

            TextButton(
                onClick = {
                    viewModel.onIntent(
                        GameIntent.Resign
                    )
                }
            ) {
                Text(
                    text = "Resign",
                    color = Color(0xFFEF4444)
                )
            }
        }
    }

    state.promotionPending?.let {
        PromotionDialog(
            onPieceSelected = { pieceType ->
                viewModel.onIntent(
                    GameIntent.Promote(pieceType)
                )
            }
        )
    }

    state.gameResult?.let { result ->
        GameResultDialog(
            result = result,
            onNewGame = onNewGame,
            onHome = onHome
        )
    }
}