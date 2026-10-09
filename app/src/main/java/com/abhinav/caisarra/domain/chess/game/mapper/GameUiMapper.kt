package com.abhinav.caisarra.domain.chess.game.mapper

import com.abhinav.caisarra.domain.chess.model.BoardPiece
import com.abhinav.caisarra.domain.chess.model.GameStatus
import com.abhinav.caisarra.domain.chess.model.PieceColor as DomainColor
import com.abhinav.caisarra.domain.chess.model.PieceType as DomainType
import com.abhinav.caisarra.domain.chess.model.Square as DomainSquare
import com.abhinav.caisarra.presentation.game.model.GameResult
import com.abhinav.caisarra.presentation.game.model.MoveUi
import com.abhinav.caisarra.presentation.game.model.Piece
import com.abhinav.caisarra.presentation.game.model.PieceColor
import com.abhinav.caisarra.presentation.game.model.PieceType
import com.abhinav.caisarra.presentation.game.model.Square

fun DomainSquare.toUi() = Square(row = 7 - rank, column = file)

fun Square.toDomain() = DomainSquare(file = column, rank = 7 - row)

fun DomainColor.toUi() = if (this == DomainColor.White) PieceColor.WHITE else PieceColor.BLACK

fun PieceColor.toDomain() = if (this == PieceColor.WHITE) DomainColor.White else DomainColor.Black

fun DomainType.toUi() = when (this) {
    DomainType.Pawn -> PieceType.PAWN
    DomainType.Knight -> PieceType.KNIGHT
    DomainType.Bishop -> PieceType.BISHOP
    DomainType.Rook -> PieceType.ROOK
    DomainType.Queen -> PieceType.QUEEN
    DomainType.King -> PieceType.KING
}

fun PieceType.toDomain() = when (this) {
    PieceType.PAWN -> DomainType.Pawn
    PieceType.KNIGHT -> DomainType.Knight
    PieceType.BISHOP -> DomainType.Bishop
    PieceType.ROOK -> DomainType.Rook
    PieceType.QUEEN -> DomainType.Queen
    PieceType.KING -> DomainType.King
}

fun BoardPiece.toUiPiece() = Piece(type = type.toUi(), color = color.toUi())

fun List<BoardPiece>.toUiBoard(): Map<Square, Piece> =
    associate { it.square.toUi() to it.toUiPiece() }

fun List<String>.toMoveUi(): List<MoveUi> =
    chunked(2).mapIndexed { index, pair ->
        MoveUi(
            moveNumber = index + 1,
            whiteMove = pair[0],
            blackMove = pair.getOrNull(1)
        )
    }

private fun DomainColor.toWinResult() =
    if (this == DomainColor.White) GameResult.WHITE_WINS else GameResult.BLACK_WINS

fun GameStatus.toResult(): GameResult? = when (this) {
    is GameStatus.Checkmate -> winner.toWinResult()
    is GameStatus.Resigned -> winner.toWinResult()
    is GameStatus.Timeout -> winner.toWinResult()
    GameStatus.Stalemate,
    GameStatus.DrawFiftyMove,
    GameStatus.DrawRepetition,
    GameStatus.DrawInsufficientMaterial,
    GameStatus.DrawAgreement -> GameResult.DRAW
    GameStatus.Ongoing,
    GameStatus.Check -> null
}

fun GameStatus.toReason(): String? = when (this) {
    is GameStatus.Checkmate -> "Checkmate"
    is GameStatus.Resigned -> "Resignation"
    is GameStatus.Timeout -> "Time ran out"
    GameStatus.Stalemate -> "Stalemate"
    GameStatus.DrawFiftyMove -> "Fifty-move rule"
    GameStatus.DrawRepetition -> "Threefold repetition"
    GameStatus.DrawInsufficientMaterial -> "Insufficient material"
    GameStatus.DrawAgreement -> "Draw by agreement"
    GameStatus.Ongoing,
    GameStatus.Check -> null
}

fun GameResult.toNotation() = when (this) {
    GameResult.WHITE_WINS -> "1-0"
    GameResult.BLACK_WINS -> "0-1"
    GameResult.DRAW -> "1/2-1/2"
}