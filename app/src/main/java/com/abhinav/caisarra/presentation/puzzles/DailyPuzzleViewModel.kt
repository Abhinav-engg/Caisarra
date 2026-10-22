package com.abhinav.caisarra.presentation.puzzles

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.remote.dto.DailyPuzzleResponse
import com.abhinav.caisarra.data.repository.PuzzleRepository
import com.abhinav.caisarra.domain.chess.engine.ChesslibEngine
import com.abhinav.caisarra.domain.chess.model.Move
import com.abhinav.caisarra.domain.chess.model.PieceColor as DomainPieceColor
import com.abhinav.caisarra.domain.chess.model.PieceType as DomainPieceType
import com.abhinav.caisarra.domain.chess.model.Square as DomainSquare
import com.abhinav.caisarra.presentation.game.model.Piece
import com.abhinav.caisarra.presentation.game.model.PieceColor as UiPieceColor
import com.abhinav.caisarra.presentation.game.model.PieceType as UiPieceType
import com.abhinav.caisarra.presentation.game.model.PieceMoveAnimationEvent
import com.abhinav.caisarra.presentation.game.model.Square as UiSquare
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DailyPuzzleViewModel(
    private val puzzleRepository: PuzzleRepository
) : ViewModel() {

    private val _state = MutableStateFlow<DailyPuzzleUiState>(DailyPuzzleUiState.Loading)
    val state = _state.asStateFlow()

    private val engine = ChesslibEngine()
    private var moveStartTime = 0L
    private var animationId = 0L

    init {
        loadDailyPuzzle()
    }

    fun loadDailyPuzzle() {
        viewModelScope.launch {
            _state.value = DailyPuzzleUiState.Loading
            puzzleRepository.getDailyPuzzle()
                .onSuccess { response ->
                    setupPuzzle(response)
                }
                .onFailure { error ->
                    _state.value = DailyPuzzleUiState.Error(
                        message = error.message ?: "Failed to load daily puzzle"
                    )
                }
        }
    }

    fun onMoveAnimationStarted(eventId: Long) {
        val current = _state.value
        if (current is DailyPuzzleUiState.Playing && current.moveAnimationEvent?.id == eventId) {
            _state.value = current.copy(moveAnimationConsumedId = eventId)
        }
    }

    fun onSquareClicked(square: UiSquare) {
        val current = _state.value
        if (current !is DailyPuzzleUiState.Playing || current.isAwaitingServer) return

        val selected = current.selectedSquare
        if (selected == null) {
            val piece = current.board[square]
            if (piece != null && piece.color == current.playerColor) {
                val legalTargets = engine.legalMovesFrom(square.toDomainSquare())
                    .map { it.to.toUiSquare() }
                _state.value = current.copy(
                    selectedSquare = square,
                    legalMoves = legalTargets,
                    message = null
                )
            }
            return
        }

        if (selected == square) {
            _state.value = current.copy(
                selectedSquare = null,
                legalMoves = emptyList()
            )
            return
        }

        val clickedPiece = current.board[square]
        if (clickedPiece != null && clickedPiece.color == current.playerColor) {
            val legalTargets = engine.legalMovesFrom(square.toDomainSquare())
                .map { it.to.toUiSquare() }
            _state.value = current.copy(
                selectedSquare = square,
                legalMoves = legalTargets,
                message = null
            )
            return
        }

        if (square in current.legalMoves) {
            val fromDomain = selected.toDomainSquare()
            val toDomain = square.toDomainSquare()

            if (engine.isPromotion(fromDomain, toDomain)) {
                _state.value = current.copy(
                    promotionPending = Pair(selected, square)
                )
            } else {
                executePlayerMove(
                    from = selected,
                    to = square,
                    promotion = null
                )
            }
        } else {
            _state.value = current.copy(
                selectedSquare = null,
                legalMoves = emptyList()
            )
        }
    }

    fun onPromotionPieceSelected(pieceType: UiPieceType) {
        val current = _state.value
        if (current !is DailyPuzzleUiState.Playing) return
        val pending = current.promotionPending ?: return

        val domainPromotion = when (pieceType) {
            UiPieceType.QUEEN -> DomainPieceType.Queen
            UiPieceType.ROOK -> DomainPieceType.Rook
            UiPieceType.BISHOP -> DomainPieceType.Bishop
            UiPieceType.KNIGHT -> DomainPieceType.Knight
            else -> DomainPieceType.Queen
        }

        _state.value = current.copy(promotionPending = null)
        executePlayerMove(
            from = pending.first,
            to = pending.second,
            promotion = domainPromotion
        )
    }

    fun onDismissPromotion() {
        val current = _state.value
        if (current is DailyPuzzleUiState.Playing) {
            _state.value = current.copy(
                promotionPending = null,
                selectedSquare = null,
                legalMoves = emptyList()
            )
        }
    }

    fun requestHint() {
        val current = _state.value
        if (current !is DailyPuzzleUiState.Playing || !current.hintAvailable || current.isAwaitingServer) return

        viewModelScope.launch {
            puzzleRepository.getHint(current.puzzle.puzzleId)
                .onSuccess { response ->
                    val hintSquare = response.fromSquare?.let { parseSquareNameToUi(it) }
                    val stateNow = _state.value
                    if (stateNow is DailyPuzzleUiState.Playing) {
                        _state.value = stateNow.copy(
                            hintAvailable = false,
                            highlightedSquare = hintSquare,
                            message = response.message ?: "Hint applied"
                        )
                    }
                }
                .onFailure { error ->
                    val stateNow = _state.value
                    if (stateNow is DailyPuzzleUiState.Playing) {
                        _state.value = stateNow.copy(
                            message = error.message ?: "Could not get hint"
                        )
                    }
                }
        }
    }

    fun requestSolution() {
        val current = _state.value
        val puzzleId = when (current) {
            is DailyPuzzleUiState.Playing -> current.puzzle.puzzleId
            is DailyPuzzleUiState.Failed -> current.puzzle.puzzleId
            else -> return
        }

        viewModelScope.launch {
            puzzleRepository.getSolution(puzzleId)
                .onSuccess { response ->
                    val stateNow = _state.value
                    if (stateNow is DailyPuzzleUiState.Playing) {
                        _state.value = stateNow.copy(
                            solutionMoves = response.moves,
                            solutionRevealed = true,
                            canViewSolution = false,
                            message = response.message
                        )
                    } else if (stateNow is DailyPuzzleUiState.Failed) {
                        _state.value = DailyPuzzleUiState.Playing(
                            puzzle = stateNow.puzzle,
                            board = stateNow.board,
                            playerColor = if (stateNow.isBoardFlipped) UiPieceColor.BLACK else UiPieceColor.WHITE,
                            isBoardFlipped = stateNow.isBoardFlipped,
                            attemptsLeft = 0,
                            streak = stateNow.streak,
                            playerRating = stateNow.userRatingAfter,
                            solutionMoves = response.moves,
                            solutionRevealed = true,
                            canViewSolution = false,
                            message = response.message
                        )
                    }
                }
                .onFailure { error ->
                    val stateNow = _state.value
                    if (stateNow is DailyPuzzleUiState.Playing) {
                        _state.value = stateNow.copy(
                            message = error.message ?: "Could not load solution"
                        )
                    }
                }
        }
    }

    private fun setupPuzzle(puzzle: DailyPuzzleResponse) {
        val fenTokens = puzzle.fen.trim().split(" ")
        val fenSide = fenTokens.getOrNull(1) ?: "w"
        val playerColor = if (fenSide == "w") UiPieceColor.BLACK else UiPieceColor.WHITE
        val isFlipped = playerColor == UiPieceColor.BLACK

        engine.loadFen(puzzle.fen)

        if (puzzle.solved) {
            applyInitialMove(puzzle.initialMove)
            applyMoveSequence(puzzle.attemptedMoves)
            val board = currentBoardMap()
            _state.value = DailyPuzzleUiState.Solved(
                puzzle = puzzle,
                board = board,
                isBoardFlipped = isFlipped,
                ratingChange = 0,
                userRatingAfter = puzzle.playerRating ?: puzzle.rating,
                streak = puzzle.currentStreak,
                message = "Puzzle already completed!"
            )
            return
        }

        if (puzzle.attemptsLeft <= 0) {
            applyInitialMove(puzzle.initialMove)
            applyMoveSequence(puzzle.attemptedMoves)
            val board = currentBoardMap()
            _state.value = DailyPuzzleUiState.Failed(
                puzzle = puzzle,
                board = board,
                isBoardFlipped = isFlipped,
                ratingChange = 0,
                userRatingAfter = puzzle.playerRating ?: puzzle.rating,
                streak = puzzle.currentStreak,
                message = "No attempts remaining."
            )
            return
        }

        val initialMoveObj = parseUciMove(puzzle.initialMove)
        val initialMovingPiece = engine.pieces().find { it.square == initialMoveObj.from }?.toUiPiece()

        engine.play(initialMoveObj)

        val animationEvent = initialMovingPiece?.let {
            PieceMoveAnimationEvent(
                id = ++animationId,
                from = initialMoveObj.from.toUiSquare(),
                to = initialMoveObj.to.toUiSquare(),
                piece = it
            )
        }

        applyMoveSequence(puzzle.attemptedMoves)

        val board = currentBoardMap()
        val checkSquare = engine.checkSquare()

        _state.value = DailyPuzzleUiState.Playing(
            puzzle = puzzle,
            board = board,
            playerColor = playerColor,
            isBoardFlipped = isFlipped,
            attemptsLeft = puzzle.attemptsLeft,
            streak = puzzle.currentStreak,
            playerRating = puzzle.playerRating ?: puzzle.rating,
            lastMoveFrom = initialMoveObj.from.toUiSquare(),
            lastMoveTo = initialMoveObj.to.toUiSquare(),
            isWhiteInCheck = checkSquare != null && engine.turn() == DomainPieceColor.White,
            isBlackInCheck = checkSquare != null && engine.turn() == DomainPieceColor.Black,
            moveAnimationEvent = animationEvent,
            hintAvailable = puzzle.hintAvailable && !puzzle.solutionViewed,
            canViewSolution = puzzle.attemptsLeft <= 0 || puzzle.solutionViewed
        )

        moveStartTime = SystemClock.elapsedRealtime()
    }

    private fun executePlayerMove(
        from: UiSquare,
        to: UiSquare,
        promotion: DomainPieceType?
    ) {
        val current = _state.value
        if (current !is DailyPuzzleUiState.Playing) return

        val domainMove = Move(
            from = from.toDomainSquare(),
            to = to.toDomainSquare(),
            promotion = promotion
        )

        val piece = current.board[from] ?: return

        val played = engine.play(domainMove)
        if (!played) return

        val uciMove = domainMove.toUci()
        val timeTaken = SystemClock.elapsedRealtime() - moveStartTime

        val newBoard = currentBoardMap()
        val checkSquare = engine.checkSquare()

        _state.value = current.copy(
            board = newBoard,
            selectedSquare = null,
            legalMoves = emptyList(),
            lastMoveFrom = from,
            lastMoveTo = to,
            isWhiteInCheck = checkSquare != null && engine.turn() == DomainPieceColor.White,
            isBlackInCheck = checkSquare != null && engine.turn() == DomainPieceColor.Black,
            isAwaitingServer = true,
            moveAnimationEvent = PieceMoveAnimationEvent(
                id = ++animationId,
                from = from,
                to = to,
                piece = piece
            )
        )

        viewModelScope.launch {
            puzzleRepository.submitAttempt(
                puzzleId = current.puzzle.puzzleId,
                move = uciMove,
                timeTakenMs = timeTaken
            ).onSuccess { response ->
                handleAttemptSuccess(response, current)
            }.onFailure { error ->
                handleAttemptFailure(error, current)
            }
        }
    }

    private suspend fun handleAttemptSuccess(
        response: com.abhinav.caisarra.data.remote.dto.DailyPuzzleAttemptResponse,
        playingState: DailyPuzzleUiState.Playing
    ) {
        val isCorrect = response.correct == true
        val isSolved = response.solved == true

        if (isCorrect) {
            if (isSolved) {
                _state.value = DailyPuzzleUiState.Solved(
                    puzzle = playingState.puzzle,
                    board = currentBoardMap(),
                    isBoardFlipped = playingState.isBoardFlipped,
                    ratingChange = response.ratingChange ?: 0,
                    userRatingAfter = response.userRatingAfter ?: playingState.playerRating,
                    streak = response.currentStreak ?: playingState.streak,
                    message = response.message ?: "Puzzle solved successfully!"
                )
            } else {
                val nextMoveUci = response.nextMove
                if (!nextMoveUci.isNullOrBlank()) {
                    delay(250)
                    val replyMove = parseUciMove(nextMoveUci)
                    val replyPiece = engine.pieces().find { it.square == replyMove.from }?.toUiPiece()
                    engine.play(replyMove)

                    val replyAnimation = replyPiece?.let {
                        PieceMoveAnimationEvent(
                            id = ++animationId,
                            from = replyMove.from.toUiSquare(),
                            to = replyMove.to.toUiSquare(),
                            piece = it
                        )
                    }

                    val updatedBoard = currentBoardMap()
                    val checkSquare = engine.checkSquare()

                    _state.value = playingState.copy(
                        board = updatedBoard,
                        lastMoveFrom = replyMove.from.toUiSquare(),
                        lastMoveTo = replyMove.to.toUiSquare(),
                        isWhiteInCheck = checkSquare != null && engine.turn() == DomainPieceColor.White,
                        isBlackInCheck = checkSquare != null && engine.turn() == DomainPieceColor.Black,
                        moveAnimationEvent = replyAnimation,
                        isAwaitingServer = false,
                        message = response.message
                    )
                    moveStartTime = SystemClock.elapsedRealtime()
                } else {
                    _state.value = playingState.copy(
                        isAwaitingServer = false,
                        message = response.message
                    )
                    moveStartTime = SystemClock.elapsedRealtime()
                }
            }
        } else {
            engine.undo()
            val restoredBoard = currentBoardMap()
            val checkSquare = engine.checkSquare()
            val remainingAttempts = response.attemptsLeft ?: (playingState.attemptsLeft - 1)

            if (remainingAttempts <= 0) {
                _state.value = DailyPuzzleUiState.Failed(
                    puzzle = playingState.puzzle,
                    board = restoredBoard,
                    isBoardFlipped = playingState.isBoardFlipped,
                    ratingChange = response.ratingChange ?: 0,
                    userRatingAfter = response.userRatingAfter ?: playingState.playerRating,
                    streak = response.currentStreak ?: playingState.streak,
                    message = response.message ?: "No attempts remaining."
                )
            } else {
                _state.value = playingState.copy(
                    board = restoredBoard,
                    selectedSquare = null,
                    legalMoves = emptyList(),
                    isWhiteInCheck = checkSquare != null && engine.turn() == DomainPieceColor.White,
                    isBlackInCheck = checkSquare != null && engine.turn() == DomainPieceColor.Black,
                    attemptsLeft = remainingAttempts,
                    isAwaitingServer = false,
                    canViewSolution = remainingAttempts <= 0,
                    message = response.message ?: "Incorrect move. Try again."
                )
                moveStartTime = SystemClock.elapsedRealtime()
            }
        }
    }

    private fun handleAttemptFailure(
        error: Throwable,
        playingState: DailyPuzzleUiState.Playing
    ) {
        engine.undo()
        val restoredBoard = currentBoardMap()
        val checkSquare = engine.checkSquare()

        _state.value = playingState.copy(
            board = restoredBoard,
            selectedSquare = null,
            legalMoves = emptyList(),
            isWhiteInCheck = checkSquare != null && engine.turn() == DomainPieceColor.White,
            isBlackInCheck = checkSquare != null && engine.turn() == DomainPieceColor.Black,
            isAwaitingServer = false,
            message = error.message ?: "Network error. Please try again."
        )
        moveStartTime = SystemClock.elapsedRealtime()
    }

    private fun applyInitialMove(uci: String) {
        if (uci.isNotBlank()) {
            val move = parseUciMove(uci)
            engine.play(move)
        }
    }

    private fun applyMoveSequence(moves: List<String>) {
        moves.forEach { uci ->
            if (uci.isNotBlank()) {
                val move = parseUciMove(uci)
                engine.play(move)
            }
        }
    }

    private fun currentBoardMap(): Map<UiSquare, Piece> {
        return engine.pieces().associate { domainPiece ->
            domainPiece.square.toUiSquare() to domainPiece.toUiPiece()
        }
    }
}

private fun DomainSquare.toUiSquare(): UiSquare = UiSquare(
    row = 7 - rank,
    column = file
)

private fun UiSquare.toDomainSquare(): DomainSquare = DomainSquare(
    file = column,
    rank = 7 - row
)

private fun com.abhinav.caisarra.domain.chess.model.BoardPiece.toUiPiece(): Piece {
    return Piece(
        type = when (type) {
            DomainPieceType.King -> UiPieceType.KING
            DomainPieceType.Queen -> UiPieceType.QUEEN
            DomainPieceType.Rook -> UiPieceType.ROOK
            DomainPieceType.Bishop -> UiPieceType.BISHOP
            DomainPieceType.Knight -> UiPieceType.KNIGHT
            DomainPieceType.Pawn -> UiPieceType.PAWN
        },
        color = when (color) {
            DomainPieceColor.White -> UiPieceColor.WHITE
            DomainPieceColor.Black -> UiPieceColor.BLACK
        }
    )
}

private fun parseUciMove(uci: String): Move {
    val fromFile = uci[0] - 'a'
    val fromRank = uci[1] - '1'
    val toFile = uci[2] - 'a'
    val toRank = uci[3] - '1'
    val promotion = if (uci.length >= 5) {
        when (uci[4].lowercaseChar()) {
            'q' -> DomainPieceType.Queen
            'r' -> DomainPieceType.Rook
            'b' -> DomainPieceType.Bishop
            'n' -> DomainPieceType.Knight
            else -> null
        }
    } else null
    return Move(DomainSquare(fromFile, fromRank), DomainSquare(toFile, toRank), promotion)
}

private fun Move.toUci(): String {
    val promo = when (promotion) {
        DomainPieceType.Queen -> "q"
        DomainPieceType.Rook -> "r"
        DomainPieceType.Bishop -> "b"
        DomainPieceType.Knight -> "n"
        else -> ""
    }
    return "${'a' + from.file}${from.rank + 1}${'a' + to.file}${to.rank + 1}$promo"
}

private fun parseSquareNameToUi(name: String): UiSquare? {
    if (name.length < 2) return null
    val file = name[0].lowercaseChar() - 'a'
    val rank = name[1] - '1'
    if (file !in 0..7 || rank !in 0..7) return null
    return UiSquare(row = 7 - rank, column = file)
}
