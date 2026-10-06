package com.abhinav.caisarra.presentation.game.model

sealed interface GameIntent {

    data class SquareClicked(
        val square: Square
    ) : GameIntent

    data class Promote(
        val pieceType: PieceType
    ) : GameIntent

    data object Undo : GameIntent

    data object Resign : GameIntent

    data object OfferDraw : GameIntent

    data object FlipBoard : GameIntent

    data object DismissResult : GameIntent
}