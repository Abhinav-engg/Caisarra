package com.abhinav.caisarra.presentation.game.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.repository.AuthRepository
import com.abhinav.caisarra.data.repository.GameRepository
import com.abhinav.caisarra.domain.chess.game.GameClock
import com.abhinav.caisarra.domain.chess.game.PassAndPlayGame
import com.abhinav.caisarra.domain.chess.model.GameStatus
import com.abhinav.caisarra.domain.chess.model.Move
import com.abhinav.caisarra.domain.chess.model.PieceColor as DomainPieceColor
import com.abhinav.caisarra.domain.chess.model.PieceType as DomainPieceType
import com.abhinav.caisarra.domain.chess.model.Square as DomainSquare
import com.abhinav.caisarra.domain.chess.model.toMove
import com.abhinav.caisarra.presentation.game.model.GameIntent
import com.abhinav.caisarra.presentation.game.model.GameResult
import com.abhinav.caisarra.presentation.game.model.GameUiState
import com.abhinav.caisarra.presentation.game.model.MoveUi
import com.abhinav.caisarra.presentation.game.model.Piece
import com.abhinav.caisarra.presentation.game.model.PieceColor as UiPieceColor
import com.abhinav.caisarra.presentation.game.model.PieceType as UiPieceType
import com.abhinav.caisarra.presentation.game.model.Square as UiSquare
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel(
    private val gameRepository: GameRepository,
    private val authRepository: AuthRepository,
    private val resumeGame: Boolean,
    private val newWhiteName: String,
    private val newBlackName: String,
    private val newTimeMinutes: Int,
    private val newBoardFlipped: Boolean,
    private val newUndoEnabled: Boolean
) : ViewModel() {
    private val _state = MutableStateFlow(GameUiState())
    val state = _state.asStateFlow()
    private var game: PassAndPlayGame? = null
    private var gameId: String? = null
    private var ownerId: String = ""
    private var clock: GameClock? = null
    private val moveTimeSnapshots = mutableListOf<Pair<Long, Long>>()
    private val redoTimeSnapshots = mutableListOf<Pair<Long, Long>>()
    private var initialTimeMs = newTimeMinutes * 60_000L
    private var promotionFrom: DomainSquare? = null
    private var promotionTo: DomainSquare? = null
    private val autoFlip = !resumeGame && newBoardFlipped

    init {
        loadGame()
    }

    private fun loadGame() {
        viewModelScope.launch {
            ownerId = authRepository.getUsername()
                ?: authRepository.getGuestId() ?: "local-user"

            if (resumeGame) {
                loadExistingGame()
            } else {
                createNewGame()
            }
        }
    }

    private suspend fun createNewGame() {
        val created =
            gameRepository.startGame(
                ownerId = ownerId,
                whiteName = newWhiteName.ifBlank {
                    "Player 1"
                },
                blackName = newBlackName.ifBlank {
                    "Player 2"
                },
                timeControlMinutes = newTimeMinutes,
                incrementSeconds = 0
            )

        gameId = created.id
        initialTimeMs = created.whiteTimeMs

        moveTimeSnapshots.clear()
        redoTimeSnapshots.clear()
        game = PassAndPlayGame()
        createClock(
            whiteTime = created.whiteTimeMs,
            blackTime = created.blackTimeMs,
            incrementSeconds = created.incrementSeconds
        )
        updateUiState()
        startClock()
    }

    private suspend fun loadExistingGame() {
        val savedGame =
            gameRepository.getUnfinished(ownerId)
        if (savedGame == null) {
            _state.update {
                it.copy(
                    isLoading = false,
                    errorMessage =
                        "No unfinished game found."
                )
            }

            return
        }

        gameId = savedGame.id
        initialTimeMs =
            (savedGame.timeControlMinutes ?: 0) * 60_000L

        moveTimeSnapshots.clear()
        redoTimeSnapshots.clear()

        moveTimeSnapshots +=
            gameRepository.moveTimesOf(savedGame)
        val restoredGame = PassAndPlayGame()
        val savedMoves = gameRepository.movesOf(savedGame)

        for (uci in savedMoves) {
            val move =
                try {
                    uci.toMove()
                } catch (_: Exception) {
                    continue
                }

            restoredGame.play(move)
        }
        game = restoredGame
        createClock(
            whiteTime = savedGame.whiteTimeMs,
            blackTime = savedGame.blackTimeMs,
            incrementSeconds = savedGame.incrementSeconds
        )
        _state.update {
            it.copy(
                isBoardFlipped = false,
                undoEnabled = true
            )
        }
        updateUiState()
        if (!restoredGame.status.isOver) {
            startClock()
        }
    }

    private fun createClock(
        whiteTime: Long,
        blackTime: Long,
        incrementSeconds: Int
    ) {
        clock?.stop()
        val newClock =
            GameClock(
                scope = viewModelScope,
                initialMs = whiteTime,
                incrementMs =
                    incrementSeconds * 1000L,
                onTimeout = { loser ->
                    handleTimeout(loser)
                }
            )
        newClock.restore(
            whiteMs = whiteTime,
            blackMs = blackTime
        )

        clock = newClock
        viewModelScope.launch {
            newClock.times.collect { times ->

                _state.update {
                    it.copy(
                        whiteTimeMillis = times.first,
                        blackTimeMillis = times.second
                    )
                }
            }
        }
    }

    private fun startClock() {
        val currentGame = game ?: return
        if (currentGame.status.isOver) {
            return
        }
        clock?.start(
            currentGame.turn
        )
    }

    fun onIntent(intent: GameIntent) {
        when (intent) {
            is GameIntent.SquareClicked -> {
                onSquareClicked(intent.square)
            }
            is GameIntent.Promote -> {
                promote(intent.pieceType)
            }
            GameIntent.Undo -> {
                undo()
            }
            GameIntent.Redo -> {
                redo()
            }
            GameIntent.Resign -> {
                resign()
            }
            GameIntent.OfferDraw -> {
                offerDraw()
            }
            GameIntent.FlipBoard -> {
                _state.update {
                    it.copy(
                        isBoardFlipped =
                            !it.isBoardFlipped
                    )
                }
            }
            GameIntent.DismissResult -> {
                _state.update {
                    it.copy(
                        gameResult = null
                    )
                }
            }
        }
    }

    private fun onSquareClicked(
        uiSquare: UiSquare
    ) {
        val currentGame = game ?: return
        if (currentGame.status.isOver) {
            return
        }
        val clickedSquare = uiSquare.toDomainSquare()
        val selected = _state.value.selectedSquare

        if (selected == null) {
            val piece =
                currentGame.pieces
                    .firstOrNull {
                        it.square == clickedSquare
                    }

            if (
                piece != null &&
                piece.color == currentGame.turn
            ) {

                val legalMoves =
                    currentGame
                        .legalTargets(clickedSquare)
                        .map {
                            it.toUiSquare()
                        }

                _state.update {
                    it.copy(
                        selectedSquare = uiSquare,
                        legalMoves = legalMoves
                    )
                }
            }

            return
        }

        val selectedDomain =
            selected.toDomainSquare()
        if (selectedDomain == clickedSquare) {
            clearSelection()
            return
        }
        val legalTargets = currentGame
            .legalTargets(selectedDomain)

        if (clickedSquare !in legalTargets) {
            val newPiece =
                currentGame.pieces
                    .firstOrNull {
                        it.square == clickedSquare
                    }

            if (
                newPiece != null &&
                newPiece.color == currentGame.turn
            ) {
                val newLegalMoves =
                    currentGame
                        .legalTargets(clickedSquare)
                        .map {
                            it.toUiSquare()
                        }

                _state.update {
                    it.copy(
                        selectedSquare = uiSquare,
                        legalMoves = newLegalMoves
                    )
                }
            }

            return
        }

        if (currentGame.needsPromotion(
                selectedDomain,
                clickedSquare
            )
        ) {
            promotionFrom = selectedDomain
            promotionTo = clickedSquare
            _state.update {
                it.copy(
                    promotionPending = uiSquare
                )
            }
            return
        }
        playMove(
            Move(
                from = selectedDomain,
                to = clickedSquare
            )
        )
    }

    private fun promote(
        pieceType: UiPieceType
    ) {
        val from = promotionFrom ?: return
        val to = promotionTo ?: return
        val promotion = pieceType.toDomainPieceType()
        playMove(
            Move(from = from,
                to = to,
                promotion = promotion
            )
        )
        promotionFrom = null
        promotionTo = null
    }

    private fun playMove(
        move: Move
    ) {
        val currentGame = game ?: return
        val played = currentGame.play(move)

        if (!played) {
            clearSelection()
            return
        }

        clearSelection()
        redoTimeSnapshots.clear()
        clock?.switchTo(
            currentGame.turn
        )

        clock?.times?.value?.let { times ->
            moveTimeSnapshots += times.first to times.second
        }

        updateUiState()
        persistAfterMove()
    }

    private fun undo() {

        if (!_state.value.undoEnabled) {
            return
        }

        val currentGame = game ?: return
        if (!currentGame.undo()) {
            return
        }

        if (moveTimeSnapshots.isNotEmpty()) {
            redoTimeSnapshots += moveTimeSnapshots.removeAt(
                moveTimeSnapshots.lastIndex
            )
        }

        val previousTimes =
            moveTimeSnapshots.lastOrNull()
                ?: (initialTimeMs to initialTimeMs)

        clock?.restore(
            whiteMs = previousTimes.first,
            blackMs = previousTimes.second
        )

        clock?.revertTo(
            currentGame.turn
        )

        updateUiState()
        persistAfterMove()
    }

    private fun redo() {
        if (!_state.value.canRedo) {
            return
        }

        val currentGame = game ?: return
        if (!currentGame.redo()) {
            return
        }

        val times = redoTimeSnapshots.removeLastOrNull()
        if (times != null) {
            moveTimeSnapshots += times
            clock?.restore(
                whiteMs = times.first,
                blackMs = times.second
            )
        }

        clock?.revertTo(
            currentGame.turn
        )

        updateUiState()
        persistAfterMove()
    }

    private fun resign() {
        val currentGame = game ?: return
        if (currentGame.status.isOver) {
            return
        }
        currentGame.resign()
        clock?.stop()
        updateUiState()
        finishGame(
            status = currentGame.status
        )
    }

    private fun offerDraw() {
        val currentGame = game ?: return
        if (currentGame.status.isOver) {
            return
        }
        currentGame.agreeDraw()
        clock?.stop()
        updateUiState()
        finishGame(
            status = currentGame.status
        )
    }

    private fun handleTimeout(
        loser: DomainPieceColor
    ) {
        val currentGame = game ?: return
        currentGame.timeout(loser)
        updateUiState()
        finishGame(
            status = currentGame.status
        )
    }

    private fun persistAfterMove() {
        val id = gameId ?: return
        val currentGame = game ?: return
        val times = clock?.times?.value ?: return

        viewModelScope.launch {

            if (currentGame.status.isOver) {
                clock?.stop()
                finishGame(
                    status = currentGame.status
                )

            } else {
                gameRepository.saveProgress(
                    id = id,
                    moves = currentGame.moveList,
                    moveTimes = moveTimeSnapshots.toList(),
                    whiteTimeMs = times.first,
                    blackTimeMs = times.second
                )
            }
        }
    }

    private fun finishGame(
        status: GameStatus
    ) {
        val id = gameId ?: return
        val currentGame = game ?: return
        val times = clock?.times?.value ?: return
        val result = status.toUiResult() ?: return

        viewModelScope.launch {
            gameRepository.finishGame(
                id = id,
                moves = currentGame.moveList,
                moveTimes = moveTimeSnapshots.toList(),
                whiteTimeMs = times.first,
                blackTimeMs = times.second,
                result = result.name,
                endReason = status.reason(),
                canSync = false
            )
        }
    }

    private fun updateUiState() {
        val currentGame = game ?: return
        val domainPieces = currentGame.pieces
        val uiBoard = domainPieces.associate { piece ->
            piece.square.toUiSquare() to
                    piece.toUiPiece()
        }

        val lastMove = currentGame.lastMove

        val uiMoves = currentGame.moveList
            .mapIndexed { index, move ->
                val moveText = formatMove(move)
                val moveNumber = index / 2 + 1

                if (index % 2 == 0) {
                    MoveUi(
                        moveNumber = moveNumber,
                        whiteMove = moveText,
                        blackMove = null
                    )

                } else {
                    MoveUi(
                        moveNumber = moveNumber,
                        whiteMove = null,
                        blackMove = moveText
                    )
                }
            }
            .groupMoves()

        val capturedWhite = currentGame.captured
            .filter {
                it.color == DomainPieceColor.White
            }
            .map {
                it.toUiPiece()
            }

        val capturedBlack = currentGame.captured
            .filter {
                it.color == DomainPieceColor.Black
            }
            .map {
                it.toUiPiece()
            }

        val checkSquare = currentGame.checkSquare
        val status = currentGame.status
        val result = status.toUiResult()
        _state.update {
            val undoAllowed =
                if (resumeGame) {
                    it.undoEnabled
                } else {
                    newUndoEnabled
                }

            it.copy(
                isLoading = false,
                errorMessage = null,
                board = uiBoard,
                whitePlayerName = it.whitePlayerName.ifBlank {
                    newWhiteName
                },
                blackPlayerName = it.blackPlayerName.ifBlank {
                    newBlackName
                },

                isWhiteTurn = currentGame.turn == DomainPieceColor.White,
                lastMoveFrom = lastMove?.from?.toUiSquare(),
                lastMoveTo = lastMove?.to?.toUiSquare(),
                isWhiteInCheck = checkSquare != null &&
                        currentGame.turn ==
                        DomainPieceColor.White,

                isBlackInCheck = checkSquare != null &&
                        currentGame.turn ==
                        DomainPieceColor.Black,

                capturedWhitePieces = capturedWhite,
                capturedBlackPieces = capturedBlack,
                moves = uiMoves,
                gameResult = result,
                isBoardFlipped =
                    if (autoFlip) {
                        currentGame.turn == DomainPieceColor.Black
                    } else {
                        it.isBoardFlipped
                    },

                undoEnabled = undoAllowed,
                canUndo = undoAllowed && currentGame.canUndo,
                canRedo = undoAllowed && currentGame.canRedo
            )
        }
    }

    private fun clearSelection() {

        _state.update {
            it.copy(
                selectedSquare = null,
                legalMoves = emptyList(),
                promotionPending = null
            )
        }
    }

    override fun onCleared() {
        clock?.stop()
        super.onCleared()
    }
}

private fun DomainSquare.toUiSquare(): UiSquare {
    return UiSquare(
        row = 7 - rank,
        column = file
    )
}

private fun UiSquare.toDomainSquare(): DomainSquare {
    return DomainSquare(
        file = column,
        rank = 7 - row
    )
}

private fun com.abhinav.caisarra.domain.chess.model.BoardPiece.toUiPiece(): Piece {
    return Piece(
        type = when (type) {
            DomainPieceType.King ->
                UiPieceType.KING

            DomainPieceType.Queen ->
                UiPieceType.QUEEN

            DomainPieceType.Rook ->
                UiPieceType.ROOK

            DomainPieceType.Bishop ->
                UiPieceType.BISHOP

            DomainPieceType.Knight ->
                UiPieceType.KNIGHT

            DomainPieceType.Pawn ->
                UiPieceType.PAWN
        },

        color = when (color) {
            DomainPieceColor.White ->
                UiPieceColor.WHITE

            DomainPieceColor.Black ->
                UiPieceColor.BLACK
        }
    )
}

private fun DomainPieceType.toUiPieceType(): UiPieceType {

    return when (this) {

        DomainPieceType.King ->
            UiPieceType.KING

        DomainPieceType.Queen ->
            UiPieceType.QUEEN

        DomainPieceType.Rook ->
            UiPieceType.ROOK

        DomainPieceType.Bishop ->
            UiPieceType.BISHOP

        DomainPieceType.Knight ->
            UiPieceType.KNIGHT

        DomainPieceType.Pawn ->
            UiPieceType.PAWN
    }
}

private fun UiPieceType.toDomainPieceType(): DomainPieceType {

    return when (this) {

        UiPieceType.KING ->
            DomainPieceType.King

        UiPieceType.QUEEN ->
            DomainPieceType.Queen

        UiPieceType.ROOK ->
            DomainPieceType.Rook

        UiPieceType.BISHOP ->
            DomainPieceType.Bishop

        UiPieceType.KNIGHT ->
            DomainPieceType.Knight

        UiPieceType.PAWN ->
            DomainPieceType.Pawn
    }
}

private fun formatMove(
    uci: String
): String {

    if (uci.length < 4) {
        return uci
    }
    return buildString {
        append(uci.substring(0, 2))
        append("-")
        append(uci.substring(2, 4))

        if (uci.length > 4) {
            append("=")
            append(
                when (uci[4].lowercaseChar()) {
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

private fun List<MoveUi>.groupMoves(): List<MoveUi> {

    if (isEmpty()) {
        return emptyList()
    }
    val grouped = mutableListOf<MoveUi>()
    for (move in this) {
        val existing = grouped.lastOrNull {
            it.moveNumber == move.moveNumber
        }
        if (existing == null) {
            grouped += move

        } else {
            grouped[grouped.lastIndex] =
                existing.copy(
                    whiteMove =
                        existing.whiteMove
                            ?: move.whiteMove,

                    blackMove =
                        existing.blackMove
                            ?: move.blackMove
                )
        }
    }
    return grouped
}

private fun GameStatus.toUiResult(): GameResult? {
    return when (this) {
        GameStatus.Ongoing,
        GameStatus.Check -> null

        is GameStatus.Checkmate ->
            if (winner == DomainPieceColor.White) {
                GameResult.WHITE_WINS
            } else {
                GameResult.BLACK_WINS
            }
        GameStatus.Stalemate,
        GameStatus.DrawFiftyMove,
        GameStatus.DrawRepetition,
        GameStatus.DrawInsufficientMaterial,
        GameStatus.DrawAgreement ->
            GameResult.DRAW
        is GameStatus.Resigned ->
            if (winner == DomainPieceColor.White) {
                GameResult.WHITE_WINS
            } else {
                GameResult.BLACK_WINS
            }

        is GameStatus.Timeout ->
            if (winner == DomainPieceColor.White) {
                GameResult.WHITE_WINS
            } else {
                GameResult.BLACK_WINS
            }
    }
}

private fun GameStatus.reason(): String {

    return when (this) {
        GameStatus.Ongoing -> "ongoing"
        GameStatus.Check -> "check"
        is GameStatus.Checkmate -> "checkmate"
        GameStatus.Stalemate -> "stalemate"
        GameStatus.DrawFiftyMove -> "fifty_move_rule"

        GameStatus.DrawRepetition -> "repetition"
        GameStatus.DrawInsufficientMaterial -> "insufficient_material"
        GameStatus.DrawAgreement -> "draw_agreement"

        is GameStatus.Resigned -> "resignation"
        is GameStatus.Timeout -> "timeout"
    }
}