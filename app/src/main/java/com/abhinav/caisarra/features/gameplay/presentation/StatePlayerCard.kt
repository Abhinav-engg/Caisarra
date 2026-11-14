package com.abhinav.caisarra.features.gameplay.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.abhinav.caisarra.presentation.game.model.GameUiState
import com.abhinav.caisarra.presentation.game.model.PieceColor

@Composable
fun StatePlayerCard(
    state: GameUiState,
    isWhite: Boolean,
    modifier: Modifier = Modifier,
    myAvatarUrl: String? = null,
    opponentAvatarUrl: String? = null,
    trailing: @Composable () -> Unit = {
        if (state.hasClock) {
            ClockPill(
                timeMillis = if (isWhite) state.whiteTimeMillis else state.blackTimeMillis,
                isActive = state.isWhiteTurn == isWhite,
                isDailyMode = state.isDailyMode
            )
        }
    }
) {
    val isMine = state.myColor == if (isWhite) PieceColor.WHITE else PieceColor.BLACK
    PlayerCard(
        username = if (isWhite) state.whitePlayerName else state.blackPlayerName,
        avatarUrl = if (isMine) myAvatarUrl else opponentAvatarUrl,
        rating = if (isWhite) state.whiteRating else state.blackRating,
        countryCode = if (isWhite) state.whiteCountry else state.blackCountry,
        capturedPieces = if (isWhite) state.capturedBlackPieces else state.capturedWhitePieces,
        advantage = if (isWhite) state.whiteAdvantage else state.blackAdvantage,
        trailing = trailing,
        modifier = modifier
    )
}
