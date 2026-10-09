package com.abhinav.caisarra.presentation.game.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.local.entity.GameEntity
import com.abhinav.caisarra.data.repository.GameRepository
import com.abhinav.caisarra.domain.chess.game.PassAndPlayGame
import com.abhinav.caisarra.domain.chess.model.PieceColor
import com.abhinav.caisarra.domain.chess.model.toMove
import com.abhinav.caisarra.presentation.game.mapper.toUi
import com.abhinav.caisarra.presentation.game.mapper.toUiBoard
import com.abhinav.caisarra.presentation.game.model.MoveUi
import com.abhinav.caisarra.presentation.game.model.Piece
import com.abhinav.caisarra.presentation.game.model.Square
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GameHistoryReviewState(
    val isLoading: Boolean = true,

    val game: GameEntity? = null,

    val currentPly: Int = 0,

    val totalPlies: Int = 0,

    val board: Map<Square, Piece> =
        emptyMap(),

    val whiteTimeMillis: Long = 0L,

    val blackTimeMillis: Long = 0L,

    val whitePlayerName: String = "White",

    val blackPlayerName: String = "Black",

    val moves: List<MoveUi> =
        emptyList(),

    val lastMoveFrom: Square? = null,

    val lastMoveTo: Square? = null,

    val isWhiteInCheck: Boolean = false,

    val isBlackInCheck: Boolean = false,

    val isWhiteTurn: Boolean = true,

    val error: String? = null
)

class GameHistoryReviewViewModel(
    private val repository: GameRepository,
    private val gameId: String
) : ViewModel() {

    private val _state =
        MutableStateFlow(
            GameHistoryReviewState()
        )

    val state =
        _state.asStateFlow()

    private var gameEntity: GameEntity? =
        null

    private var moves: List<String> =
        emptyList()

    private var moveTimes:
            List<Pair<Long, Long>> =
        emptyList()

    init {
        loadGame()
    }

    private fun loadGame() {

        viewModelScope.launch {

            val savedGame =
                repository.getById(gameId)

            if (savedGame == null) {

                _state.update {
                    it.copy(
                        isLoading = false,
                        error =
                            "Game history could not be found."
                    )
                }

                return@launch
            }

            gameEntity = savedGame

            moves =
                repository.movesOf(savedGame)

            moveTimes =
                repository.moveTimesOf(savedGame)

            _state.update {
                it.copy(
                    isLoading = false,
                    game = savedGame,
                    currentPly = moves.size,
                    totalPlies = moves.size,
                    whitePlayerName =
                        savedGame.whiteName,
                    blackPlayerName =
                        savedGame.blackName
                )
            }

            renderPosition(moves.size)
        }
    }

    fun previousMove() {

        val current =
            _state.value.currentPly

        if (current > 0) {
            renderPosition(current - 1)
        }
    }

    fun nextMove() {

        val current =
            _state.value.currentPly

        if (current < moves.size) {
            renderPosition(current + 1)
        }
    }

    fun jumpToMove(
        ply: Int
    ) {

        renderPosition(
            ply.coerceIn(
                0,
                moves.size
            )
        )
    }

    private fun renderPosition(
        ply: Int
    ) {

        val savedGame =
            gameEntity ?: return

        val replay =
            PassAndPlayGame()

        moves
            .take(ply)
            .forEach { uci ->

                runCatching {

                    replay.play(
                        uci.toMove()
                    )
                }
            }

        val lastMove =
            replay.lastMove

        val checkSquare =
            replay.checkSquare

        val time =
            when {

                ply == 0 -> {

                    val initial =
                        (savedGame.timeControlMinutes
                            ?: 0) * 60_000L

                    initial to initial
                }

                ply == moves.size -> {

                    savedGame.whiteTimeMs to
                            savedGame.blackTimeMs
                }

                ply <= moveTimes.size -> {

                    moveTimes[ply - 1]
                }

                else -> {

                    savedGame.whiteTimeMs to
                            savedGame.blackTimeMs
                }
            }

        _state.update {

            it.copy(

                currentPly = ply,

                totalPlies = moves.size,

                board =
                    replay.pieces.toUiBoard(),

                whiteTimeMillis =
                    time.first,

                blackTimeMillis =
                    time.second,

                lastMoveFrom =
                    lastMove?.from?.toUi(),

                lastMoveTo =
                    lastMove?.to?.toUi(),

                isWhiteInCheck =
                    checkSquare != null &&
                            replay.turn ==
                            PieceColor.White,

                isBlackInCheck =
                    checkSquare != null &&
                            replay.turn ==
                            PieceColor.Black,

                isWhiteTurn =
                    replay.turn ==
                            PieceColor.White,

                moves =
                    createMoveList(moves),

                error = null
            )
        }
    }

    private fun createMoveList(
        rawMoves: List<String>
    ): List<MoveUi> {

        return rawMoves
            .chunked(2)
            .mapIndexed { index, pair ->

                MoveUi(

                    moveNumber =
                        index + 1,

                    whiteMove =
                        pair
                            .getOrNull(0)
                            ?.let(::formatMove),

                    blackMove =
                        pair
                            .getOrNull(1)
                            ?.let(::formatMove)
                )
            }
    }

    private fun formatMove(
        uci: String
    ): String {

        if (uci.length < 4) {
            return uci
        }

        return buildString {

            append(
                uci.substring(0, 2)
            )

            append("-")

            append(
                uci.substring(2, 4)
            )

            if (uci.length > 4) {

                append("=")

                append(
                    when (
                        uci[4]
                            .lowercaseChar()
                    ) {
                        'q' -> "Q"
                        'r' -> "R"
                        'b' -> "B"
                        'n' -> "N"
                        else -> uci[4]
                    }
                )
            }
        }
    }
}