package com.abhinav.caisarra.presentation.puzzles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.remote.dto.RushMoveResponse
import com.abhinav.caisarra.data.remote.dto.RushPuzzleData
import com.abhinav.caisarra.data.remote.dto.RushSummaryDto
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RushViewModel(
    private val puzzleRepository: PuzzleRepository,
    private val mode: String = "3min"
) : ViewModel() {

    private val _state = MutableStateFlow<RushUiState>(RushUiState.Loading)
    val state = _state.asStateFlow()

    private val engine = ChesslibEngine()
    private var animationId = 0L
    private var timerJob: Job? = null
    private var startJob: Job? = null
    private var timeRemainingSeconds = 0

    init {
        startRush()
    }

    fun startRush() {
        if (startJob?.isActive == true) return

        timerJob?.cancel()
        _state.value = RushUiState.Loading

        startJob = viewModelScope.launch {
            try {
                puzzleRepository.startRush(mode)
                    .onSuccess { response ->
                        setupFirstPuzzle(
                            response.sessionId,
                            response.mode,
                            response.timeLimitSec,
                            response.puzzle
                        )

                        if (mode != "survival") {
                            startTimer(response.timeLimitSec)
                        }
                    }
                    .onFailure { error ->
                        _state.value = RushUiState.Error(
                            message = error.message
                                ?: "Failed to start Puzzle Rush"
                        )
                    }
            } finally {
                startJob = null
            }
        }
    }

    fun onMoveAnimationStarted(eventId: Long) {
        val current = _state.value
        if (current is RushUiState.Playing && current.moveAnimationEvent?.id == eventId) {
            _state.value = current.copy(moveAnimationConsumedId = eventId)
        }
    }

    fun onSquareClicked(square: UiSquare) {
        val current = _state.value
        if (current !is RushUiState.Playing || current.isAwaitingServer || current.isSkipping) return

        val selected = current.selectedSquare
        if (selected == null) {
            val piece = current.board[square]
            if (piece != null && piece.color == current.playerColor) {
                val legalTargets = engine.legalMovesFrom(square.toDomainSquare())
                    .map { it.to.toUiSquare() }
                _state.value = current.copy(
                    selectedSquare = square,
                    legalMoves = legalTargets
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
                legalMoves = legalTargets
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
                executePlayerMove(selected, square, null)
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
        if (current !is RushUiState.Playing) return
        val pending = current.promotionPending ?: return

        val domainPromotion = when (pieceType) {
            UiPieceType.QUEEN -> DomainPieceType.Queen
            UiPieceType.ROOK -> DomainPieceType.Rook
            UiPieceType.BISHOP -> DomainPieceType.Bishop
            UiPieceType.KNIGHT -> DomainPieceType.Knight
            else -> DomainPieceType.Queen
        }

        _state.value = current.copy(promotionPending = null)
        executePlayerMove(pending.first, pending.second, domainPromotion)
    }

    fun requestHint() {
        val current = _state.value
        if (current !is RushUiState.Playing || current.isAwaitingServer || current.isSkipping) return

        viewModelScope.launch {
            puzzleRepository.getHint(current.currentPuzzle.puzzleId)
                .onSuccess { response ->
                    val hintSquare = response.fromSquare?.let { parseSquareNameToUi(it) }
                    val stateNow = _state.value
                    if (stateNow is RushUiState.Playing && hintSquare != null) {
                        val legalTargets = engine.legalMovesFrom(hintSquare.toDomainSquare())
                            .map { it.to.toUiSquare() }
                        _state.value = stateNow.copy(
                            selectedSquare = hintSquare,
                            legalMoves = legalTargets,
                            lastMoveFrom = hintSquare,
                            highlightedSquare = hintSquare
                        )
                    }
                }
                .onFailure {
                    val stateNow = _state.value
                    if (stateNow is RushUiState.Playing) {
                        val targetDomainColor = if (current.playerColor == UiPieceColor.WHITE) DomainPieceColor.White else DomainPieceColor.Black
                        val candidatePiece = engine.pieces().firstOrNull { piece ->
                            piece.color == targetDomainColor &&
                            engine.legalMovesFrom(piece.square).isNotEmpty()
                        }
                        val hintSquare = candidatePiece?.square?.toUiSquare()
                        val legalTargets = candidatePiece?.let { piece ->
                            engine.legalMovesFrom(piece.square).map { it.to.toUiSquare() }
                        } ?: emptyList()

                        _state.value = stateNow.copy(
                            selectedSquare = hintSquare,
                            legalMoves = legalTargets,
                            lastMoveFrom = hintSquare,
                            highlightedSquare = hintSquare
                        )
                    }
                }
        }
    }

    fun skipPuzzle() {
        val current = _state.value
        if (current !is RushUiState.Playing || current.isAwaitingServer || current.isSkipping) return

        if (current.secondsRemaining != null) {
            timeRemainingSeconds = maxOf(0, timeRemainingSeconds - 45)
            if (timeRemainingSeconds <= 0) {
                _state.value = current.copy(secondsRemaining = 0)
                quitRush()
                return
            }
            _state.value = current.copy(secondsRemaining = timeRemainingSeconds, isSkipping = true, isAwaitingServer = true)
        } else {
            _state.value = current.copy(isSkipping = true, isAwaitingServer = true)
        }

        val currentTurnColor = if (current.playerColor == UiPieceColor.WHITE) DomainPieceColor.White else DomainPieceColor.Black
        val candidateMove = engine.pieces()
            .filter { it.color == currentTurnColor }
            .flatMap { engine.legalMovesFrom(it.square) }
            .firstOrNull()
            ?.toUci() ?: "a2a3"

        viewModelScope.launch {
            puzzleRepository.skipRushPuzzle(current.sessionId, current.currentPuzzle.puzzleId)
                .onSuccess { response ->
                    handleMoveResponse(response, current)
                }
                .onFailure {
                    puzzleRepository.submitRushMove(current.sessionId, current.currentPuzzle.puzzleId, candidateMove)
                        .onSuccess { response ->
                            handleMoveResponse(response, current)
                        }
                        .onFailure {
                            val stateNow = _state.value
                            if (stateNow is RushUiState.Playing) {
                                _state.value = stateNow.copy(
                                    isSkipping = false,
                                    isAwaitingServer = false
                                )
                            }
                        }
                }
        }
    }

    fun quitRush() {
        val current = _state.value
        if (current !is RushUiState.Playing) return
        timerJob?.cancel()

        viewModelScope.launch {
            try {
                puzzleRepository.endRush(current.sessionId)
            } catch (_: Exception) {
            }
        }
    }

    private fun setupFirstPuzzle(
        sessionId: String,
        rushMode: String,
        timeLimitSec: Int,
        puzzle: RushPuzzleData
    ) {
        val fenTokens = puzzle.fen.trim().split(" ")
        val fenSide = fenTokens.getOrNull(1) ?: "w"
        val playerColor = if (fenSide == "w") UiPieceColor.BLACK else UiPieceColor.WHITE
        val isFlipped = playerColor == UiPieceColor.BLACK

        engine.loadFen(puzzle.fen)

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

        val board = currentBoardMap()
        val checkSquare = engine.checkSquare()

        _state.value = RushUiState.Playing(
            sessionId = sessionId,
            mode = rushMode,
            currentPuzzle = puzzle,
            board = board,
            playerColor = playerColor,
            isBoardFlipped = isFlipped,
            score = 0,
            strikes = 0,
            strikesLeft = 3,
            secondsRemaining = if (rushMode != "survival") timeLimitSec else null,
            lastMoveFrom = initialMoveObj.from.toUiSquare(),
            lastMoveTo = initialMoveObj.to.toUiSquare(),
            isWhiteInCheck = checkSquare != null && engine.turn() == DomainPieceColor.White,
            isBlackInCheck = checkSquare != null && engine.turn() == DomainPieceColor.Black,
            moveAnimationEvent = animationEvent
        )
    }

    private fun executePlayerMove(
        from: UiSquare,
        to: UiSquare,
        promotion: DomainPieceType?
    ) {
        val current = _state.value
        if (current !is RushUiState.Playing) return

        val domainMove = Move(
            from = from.toDomainSquare(),
            to = to.toDomainSquare(),
            promotion = promotion
        )

        val piece = current.board[from] ?: return
        val played = engine.play(domainMove)
        if (!played) return

        val uciMove = domainMove.toUci()
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
            puzzleRepository.submitRushMove(
                sessionId = current.sessionId,
                puzzleId = current.currentPuzzle.puzzleId,
                move = uciMove
            ).onSuccess { response ->
                handleMoveResponse(response, current)
            }.onFailure {
                engine.undo()
                val restoredBoard = currentBoardMap()
                val stateNow = _state.value
                if (stateNow is RushUiState.Playing) {
                    _state.value = stateNow.copy(
                        board = restoredBoard,
                        isAwaitingServer = false
                    )
                }
            }
        }
    }

    private suspend fun handleMoveResponse(
        response: RushMoveResponse,
        playingState: RushUiState.Playing
    ) {
        if (response.gameOver) {
            timerJob?.cancel()
            _state.value = RushUiState.GameOver(
                summary = response.summary ?: RushSummaryDto(
                    sessionId = playingState.sessionId,
                    mode = playingState.mode,
                    finalScore = response.score,
                    totalStrikes = response.strikes ?: playingState.strikes,
                    isNewHighScore = false,
                    bestScore = response.score,
                    durationSec = 0
                ),
                mode = playingState.mode
            )
            return
        }

        if (playingState.isSkipping) {
            val nextPuzzle = response.nextPuzzle
            val preservedStrikes = if (playingState.mode == "survival") {
                playingState.strikes + 1
            } else {
                playingState.strikes
            }
            val preservedStrikesLeft = maxOf(0, 3 - preservedStrikes)

            if (playingState.mode == "survival" && (preservedStrikes >= 3 || preservedStrikesLeft <= 0)) {
                timerJob?.cancel()
                _state.value = RushUiState.GameOver(
                    summary = response.summary ?: RushSummaryDto(
                        sessionId = playingState.sessionId,
                        mode = playingState.mode,
                        finalScore = playingState.score,
                        totalStrikes = preservedStrikes,
                        isNewHighScore = false,
                        bestScore = playingState.score,
                        durationSec = 0
                    ),
                    mode = playingState.mode
                )
                viewModelScope.launch {
                    try {
                        puzzleRepository.endRush(playingState.sessionId)
                    } catch (_: Exception) {
                    }
                }
                return
            }

            if (nextPuzzle != null) {
                loadNextPuzzle(nextPuzzle, playingState.score, preservedStrikes, preservedStrikesLeft, playingState)
            } else {
                _state.value = playingState.copy(
                    board = currentBoardMap(),
                    strikes = preservedStrikes,
                    strikesLeft = preservedStrikesLeft,
                    isAwaitingServer = false,
                    isSkipping = false
                )
            }
            return
        }

        if (response.correct) {
            val preservedStrikes = maxOf(playingState.strikes, response.strikes ?: 0)
            val preservedStrikesLeft = minOf(playingState.strikesLeft, response.strikesLeft ?: (3 - preservedStrikes)).coerceAtLeast(0)

            val nextPuzzle = response.nextPuzzle
            if (nextPuzzle != null) {
                _state.value = playingState.copy(
                    score = response.score,
                    strikes = preservedStrikes,
                    strikesLeft = preservedStrikesLeft,
                    isCorrectFlash = true,
                    isAwaitingServer = true,
                    isSkipping = false
                )
                delay(200)
                loadNextPuzzle(nextPuzzle, response.score, preservedStrikes, preservedStrikesLeft, playingState)
            } else if (!response.nextMove.isNullOrBlank()) {
                delay(200)
                val replyMove = parseUciMove(response.nextMove)
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

                _state.value = playingState.copy(
                    board = currentBoardMap(),
                    strikes = preservedStrikes,
                    strikesLeft = preservedStrikesLeft,
                    lastMoveFrom = replyMove.from.toUiSquare(),
                    lastMoveTo = replyMove.to.toUiSquare(),
                    moveAnimationEvent = replyAnimation,
                    isAwaitingServer = false,
                    isSkipping = false
                )
            } else {
                _state.value = playingState.copy(
                    strikes = preservedStrikes,
                    strikesLeft = preservedStrikesLeft,
                    isAwaitingServer = false,
                    isSkipping = false
                )
            }
        } else {
            engine.undo()
            val serverStrikes = response.strikes ?: 0
            val calculatedStrikes = maxOf(playingState.strikes + 1, serverStrikes)
            val calculatedStrikesLeft = maxOf(0, 3 - calculatedStrikes)

            if (calculatedStrikes >= 3 || calculatedStrikesLeft <= 0) {
                timerJob?.cancel()
                _state.value = RushUiState.GameOver(
                    summary = response.summary ?: RushSummaryDto(
                        sessionId = playingState.sessionId,
                        mode = playingState.mode,
                        finalScore = if (response.score > 0) response.score else playingState.score,
                        totalStrikes = calculatedStrikes,
                        isNewHighScore = false,
                        bestScore = playingState.score,
                        durationSec = 0
                    ),
                    mode = playingState.mode
                )
                viewModelScope.launch {
                    try {
                        puzzleRepository.endRush(playingState.sessionId)
                    } catch (_: Exception) {
                    }
                }
                return
            }

            val nextPuzzle = response.nextPuzzle
            if (nextPuzzle != null) {
                _state.value = playingState.copy(
                    strikes = calculatedStrikes,
                    strikesLeft = calculatedStrikesLeft,
                    isStrikeFlash = true,
                    isAwaitingServer = true,
                    isSkipping = false
                )
                delay(250)
                loadNextPuzzle(nextPuzzle, playingState.score, calculatedStrikes, calculatedStrikesLeft, playingState)
            } else {
                _state.value = playingState.copy(
                    board = currentBoardMap(),
                    strikes = calculatedStrikes,
                    strikesLeft = calculatedStrikesLeft,
                    isAwaitingServer = false,
                    isSkipping = false,
                    isStrikeFlash = true
                )
            }
        }
    }

    private fun loadNextPuzzle(
        nextPuzzle: RushPuzzleData,
        newScore: Int,
        newStrikes: Int,
        newStrikesLeft: Int,
        previousState: RushUiState.Playing
    ) {
        val fenTokens = nextPuzzle.fen.trim().split(" ")
        val fenSide = fenTokens.getOrNull(1) ?: "w"
        val playerColor = if (fenSide == "w") UiPieceColor.BLACK else UiPieceColor.WHITE
        val isFlipped = playerColor == UiPieceColor.BLACK

        engine.loadFen(nextPuzzle.fen)

        val initialMoveObj = parseUciMove(nextPuzzle.initialMove)
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

        val board = currentBoardMap()
        val checkSquare = engine.checkSquare()

        _state.value = previousState.copy(
            currentPuzzle = nextPuzzle,
            board = board,
            playerColor = playerColor,
            isBoardFlipped = isFlipped,
            score = newScore,
            strikes = newStrikes,
            strikesLeft = newStrikesLeft,
            selectedSquare = null,
            legalMoves = emptyList(),
            lastMoveFrom = initialMoveObj.from.toUiSquare(),
            lastMoveTo = initialMoveObj.to.toUiSquare(),
            isWhiteInCheck = checkSquare != null && engine.turn() == DomainPieceColor.White,
            isBlackInCheck = checkSquare != null && engine.turn() == DomainPieceColor.Black,
            moveAnimationEvent = animationEvent,
            isAwaitingServer = false,
            isSkipping = false,
            isCorrectFlash = false,
            isStrikeFlash = false,
            highlightedSquare = null
        )
    }

    private fun startTimer(totalSeconds: Int) {
        timerJob?.cancel()
        timeRemainingSeconds = totalSeconds
        timerJob = viewModelScope.launch {
            while (timeRemainingSeconds > 0) {
                delay(1000)
                timeRemainingSeconds--
                val current = _state.value
                if (current is RushUiState.Playing) {
                    _state.value = current.copy(secondsRemaining = timeRemainingSeconds)
                }
            }
            quitRush()
        }
    }

    private fun currentBoardMap(): Map<UiSquare, Piece> {
        return engine.pieces().associate { domainPiece ->
            domainPiece.square.toUiSquare() to domainPiece.toUiPiece()
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
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
