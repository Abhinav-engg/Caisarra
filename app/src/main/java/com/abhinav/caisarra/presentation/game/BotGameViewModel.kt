package com.abhinav.caisarra.presentation.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.local.SessionExpiredEvent
import com.abhinav.caisarra.data.remote.BotSocketClient
import com.abhinav.caisarra.data.remote.dto.BotErrorMessage
import com.abhinav.caisarra.data.remote.dto.BotGame
import com.abhinav.caisarra.data.remote.dto.BotGameStateMessage
import com.abhinav.caisarra.data.remote.dto.BotSocketIncoming
import com.abhinav.caisarra.data.remote.dto.BotThinkingMessage
import com.abhinav.caisarra.data.remote.dto.NextBot
import com.abhinav.caisarra.data.repository.BotGameRepository
import com.abhinav.caisarra.domain.chess.engine.ChesslibEngine
import com.abhinav.caisarra.domain.chess.game.mapper.toDomain
import com.abhinav.caisarra.domain.chess.game.mapper.toMoveUi
import com.abhinav.caisarra.domain.chess.game.mapper.toUi
import com.abhinav.caisarra.domain.chess.game.mapper.toUiBoard
import com.abhinav.caisarra.domain.chess.game.mapper.toUiPiece
import com.abhinav.caisarra.domain.chess.model.Move
import com.abhinav.caisarra.domain.chess.model.PieceColor as DomainColor
import com.abhinav.caisarra.domain.chess.model.PieceType as DomainPieceType
import com.abhinav.caisarra.domain.chess.model.Square as DomainSquare
import com.abhinav.caisarra.presentation.game.model.GameResult
import com.abhinav.caisarra.presentation.game.model.GameUiState
import com.abhinav.caisarra.presentation.game.model.PieceColor
import com.abhinav.caisarra.presentation.game.model.PieceMoveAnimationEvent
import com.abhinav.caisarra.presentation.game.model.PieceType
import com.abhinav.caisarra.presentation.game.model.Square
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class BotGameViewModel(
    application: Application,
    private val gameId: String
) : AndroidViewModel(application) {

    private val engine = ChesslibEngine()
    private val socket = BotSocketClient(application)
    private val repository = BotGameRepository.get(application)

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _botThinking = MutableStateFlow(false)
    val botThinking: StateFlow<Boolean> = _botThinking.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    private val _nextBot = MutableStateFlow<NextBot?>(null)
    val nextBot: StateFlow<NextBot?> = _nextBot.asStateFlow()

    private val _endReason = MutableStateFlow<String?>(null)
    val endReason: StateFlow<String?> = _endReason.asStateFlow()

    private val _isStartingNextBot = MutableStateFlow(false)
    val isStartingNextBot: StateFlow<Boolean> = _isStartingNextBot.asStateFlow()

    private var myColor: PieceColor? = null
    private var myColorString: String = "white"
    private var pendingPromotionFrom: Square? = null
    private var awaitingServer = false
    private var finished = false
    private var reconnecting = false
    private var animationSequence = 0L
    private var lastAnimatedMoveKey: String? = null
    private var uciMoves: List<String> = emptyList()
    private var viewedPly: Int? = null

    init {
        viewModelScope.launch {
            socket.incoming.collect { handle(it) }
        }
        viewModelScope.launch {
            var wasConnected = false
            socket.connected.collect { connected ->
                if (connected) {
                    wasConnected = true
                } else if (wasConnected && !finished) {
                    reconnect()
                }
            }
        }
        viewModelScope.launch {
            if (!socket.connect(gameId)) {
                loadGameFallback()
                return@launch
            }
            if (withTimeoutOrNull(6000) { socket.connected.first { it } } == null) {
                loadGameFallback()
            }
        }
    }

    fun onMoveAnimationStarted(eventId: Long) {
        _uiState.update { current ->
            if (current.moveAnimationEvent?.id == eventId) {
                current.copy(moveAnimationConsumedId = eventId)
            } else {
                current
            }
        }
    }

    private fun loadGameFallback() {
        viewModelScope.launch {
            repository.getGame(gameId)
                .onSuccess { game ->
                    applyState(game)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
        }
    }

    private fun reconnect() {
        if (reconnecting || finished) return
        reconnecting = true
        viewModelScope.launch {
            var attempt = 0
            while (!finished && !socket.connected.value && attempt < 5) {
                delay(minOf(1500L shl attempt.coerceAtMost(2), 6000L))
                if (!socket.connect(gameId)) break
                if (withTimeoutOrNull(4000) { socket.connected.first { it } } != null) break
                attempt++
            }
            if (!finished && !socket.connected.value) {
                loadGameFallback()
            }
            reconnecting = false
        }
    }

    fun undo() {
        val totalPlies = uciMoves.size
        if (totalPlies == 0) return
        val currentPly = viewedPly ?: totalPlies
        if (currentPly <= 0) return
        viewPly(currentPly - 1)
    }

    fun redo() {
        val totalPlies = uciMoves.size
        val currentPly = viewedPly ?: totalPlies
        if (currentPly >= totalPlies) return
        viewPly(currentPly + 1)
    }

    private fun viewPly(ply: Int) {
        val totalPlies = uciMoves.size
        if (ply < 0 || ply > totalPlies) return

        if (ply == totalPlies) {
            viewedPly = null
            val inCheck = engine.checkSquare() != null
            val turn = engine.turn()
            val whiteTurn = turn == DomainColor.White
            val (capturedWhite, capturedBlack) = calculateCapturedPieces(uciMoves)
            val lastMove = uciMoves.lastOrNull()
            _uiState.update {
                it.copy(
                    board = engine.pieces().toUiBoard(),
                    selectedSquare = null,
                    legalMoves = emptyList(),
                    promotionPending = null,
                    lastMoveFrom = lastMove?.let { m -> squareFrom(m.take(2)) },
                    lastMoveTo = lastMove?.let { m -> squareFrom(m.drop(2).take(2)) },
                    isWhiteInCheck = inCheck && whiteTurn,
                    isBlackInCheck = inCheck && !whiteTurn,
                    isWhiteTurn = whiteTurn,
                    capturedWhitePieces = capturedWhite,
                    capturedBlackPieces = capturedBlack,
                    canUndo = totalPlies > 0,
                    canRedo = false,
                    undoEnabled = true,
                    redoEnabled = false
                )
            }
            return
        }

        viewedPly = ply
        val replayEngine = ChesslibEngine()
        var lastFrom: Square? = null
        var lastTo: Square? = null
        val historyMoves = uciMoves.take(ply)

        for (uci in historyMoves) {
            if (uci.length < 4) continue
            val fromFile = uci[0] - 'a'
            val fromRank = uci[1].digitToIntOrNull()?.minus(1) ?: continue
            val toFile = uci[2] - 'a'
            val toRank = uci[3].digitToIntOrNull()?.minus(1) ?: continue
            if (fromFile !in 0..7 || toFile !in 0..7 || fromRank !in 0..7 || toRank !in 0..7) continue

            val promotion = when (uci.getOrNull(4)?.lowercaseChar()) {
                'q' -> DomainPieceType.Queen
                'r' -> DomainPieceType.Rook
                'b' -> DomainPieceType.Bishop
                'n' -> DomainPieceType.Knight
                else -> null
            }
            val move = Move(
                from = DomainSquare(fromFile, fromRank),
                to = DomainSquare(toFile, toRank),
                promotion = promotion
            )
            replayEngine.play(move)
        }

        val lastMove = historyMoves.lastOrNull()
        if (lastMove != null && lastMove.length >= 4) {
            lastFrom = squareFrom(lastMove.take(2))
            lastTo = squareFrom(lastMove.drop(2).take(2))
        }

        val inCheck = replayEngine.checkSquare() != null
        val turn = replayEngine.turn()
        val whiteTurn = turn == DomainColor.White
        val (capturedWhite, capturedBlack) = calculateCapturedPieces(historyMoves)

        _uiState.update {
            it.copy(
                board = replayEngine.pieces().toUiBoard(),
                selectedSquare = null,
                legalMoves = emptyList(),
                promotionPending = null,
                lastMoveFrom = lastFrom,
                lastMoveTo = lastTo,
                isWhiteInCheck = inCheck && whiteTurn,
                isBlackInCheck = inCheck && !whiteTurn,
                isWhiteTurn = whiteTurn,
                capturedWhitePieces = capturedWhite,
                capturedBlackPieces = capturedBlack,
                canUndo = ply > 0,
                canRedo = true,
                undoEnabled = true,
                redoEnabled = true
            )
        }
    }

    fun onSquareClick(square: Square) {
        val state = _uiState.value
        val color = myColor ?: return
        if (viewedPly != null && viewedPly != uciMoves.size) return
        if (finished || awaitingServer || _botThinking.value || state.promotionPending != null) return
        if (state.isWhiteTurn != (color == PieceColor.WHITE)) return

        val selected = state.selectedSquare
        when {
            selected != null && square in state.legalMoves -> move(selected, square)
            state.board[square]?.color == color -> _uiState.update {
                it.copy(
                    selectedSquare = square,
                    legalMoves = engine.legalMovesFrom(square.toDomain()).map { m -> m.to.toUi() }.distinct()
                )
            }
            else -> _uiState.update { it.copy(selectedSquare = null, legalMoves = emptyList()) }
        }
    }

    fun onPromote(type: PieceType) {
        val from = pendingPromotionFrom ?: return
        val to = _uiState.value.promotionPending ?: return
        pendingPromotionFrom = null
        send(from, to, type)
    }

    fun resign() {
        if (finished) return
        socket.sendResign()
    }

    fun flipBoard() {
        _uiState.update { it.copy(isBoardFlipped = !it.isBoardFlipped) }
    }

    fun dismissResult() {
        _uiState.update { it.copy(gameResult = null) }
    }

    fun playNextBot(onGameCreated: (String) -> Unit) {
        val bot = _nextBot.value ?: return
        if (_isStartingNextBot.value) return
        _isStartingNextBot.value = true
        viewedPly = null
        uciMoves = emptyList()
        viewModelScope.launch {
            repository.createGame(botId = bot.id, rating = null, color = myColorString)
                .onSuccess { game ->
                    _isStartingNextBot.value = false
                    onGameCreated(game.id)
                }
                .onFailure { error ->
                    _isStartingNextBot.value = false
                    _snackbarEvent.tryEmit(error.message ?: "Failed to start next game")
                }
        }
    }

    private fun move(from: Square, to: Square) {
        if (engine.isPromotion(from.toDomain(), to.toDomain())) {
            pendingPromotionFrom = from
            _uiState.update { it.copy(promotionPending = to) }
        } else {
            send(from, to, null)
        }
    }

    private fun send(from: Square, to: Square, promotion: PieceType?) {
        val move = Move(from.toDomain(), to.toDomain(), promotion?.toDomain())
        val legal = engine.legalMovesFrom(move.from).any { it.to == move.to && it.promotion == move.promotion }
        if (!legal) {
            _uiState.update { it.copy(selectedSquare = null, legalMoves = emptyList(), promotionPending = null) }
            return
        }
        val sent = socket.sendMove(move.uci)
        awaitingServer = sent
        _uiState.update {
            it.copy(
                selectedSquare = null,
                legalMoves = emptyList(),
                promotionPending = null
            )
        }
        if (!sent) {
            _snackbarEvent.tryEmit("Connection lost. Move not sent.")
        }
    }

    private fun handle(message: BotSocketIncoming) {
        when (message) {
            is BotGameStateMessage -> applyState(message.game)
            is BotThinkingMessage -> _botThinking.value = true
            is BotErrorMessage -> {
                awaitingServer = false
                _botThinking.value = false
                _uiState.update {
                    it.copy(
                        selectedSquare = null,
                        legalMoves = emptyList(),
                        promotionPending = null
                    )
                }
                _snackbarEvent.tryEmit(message.message)
                if (message.message == "invalid or revoked token") {
                    SessionExpiredEvent.emit()
                } else if (message.message.startsWith("bot could not move")) {
                    reconnect()
                } else if (message.message.contains("reload state", ignoreCase = true)) {
                    loadGameFallback()
                }
            }
        }
    }

    private fun calculateCapturedPieces(
        uciMoves: List<String>
    ): Pair<List<com.abhinav.caisarra.presentation.game.model.Piece>, List<com.abhinav.caisarra.presentation.game.model.Piece>> {
        val replayEngine = ChesslibEngine()
        val captured = mutableListOf<com.abhinav.caisarra.domain.chess.model.BoardPiece>()

        for (uci in uciMoves) {
            if (uci.length < 4) continue
            val fromFile = uci[0] - 'a'
            val fromRank = uci[1].digitToIntOrNull()?.minus(1) ?: continue
            val toFile = uci[2] - 'a'
            val toRank = uci[3].digitToIntOrNull()?.minus(1) ?: continue

            if (fromFile !in 0..7 || toFile !in 0..7 || fromRank !in 0..7 || toRank !in 0..7) continue

            val promotion = when (uci.getOrNull(4)?.lowercaseChar()) {
                'q' -> DomainPieceType.Queen
                'r' -> DomainPieceType.Rook
                'b' -> DomainPieceType.Bishop
                'n' -> DomainPieceType.Knight
                else -> null
            }

            val move = Move(
                from = DomainSquare(fromFile, fromRank),
                to = DomainSquare(toFile, toRank),
                promotion = promotion
            )
            val capturedPiece = replayEngine.capturedBy(move)
            if (replayEngine.play(move) && capturedPiece != null) {
                captured.add(capturedPiece)
            }
        }

        val whiteCaptured = captured.filter { it.color == DomainColor.White }.map { it.toUiPiece() }
        val blackCaptured = captured.filter { it.color == DomainColor.Black }.map { it.toUiPiece() }
        return whiteCaptured to blackCaptured
    }

    private fun applyState(game: BotGame) {
        awaitingServer = false
        myColorString = game.playerColor
        val colorWasUnknown = myColor == null
        if (colorWasUnknown) {
            myColor = if (game.playerColor == "white") PieceColor.WHITE else PieceColor.BLACK
        }

        val previousBoard = _uiState.value.board
        val lastMove = game.moves.lastOrNull()
        val moveKey = lastMove?.let { "$it:${game.moves.size}" }

        val animationEvent = if (
            previousBoard.isNotEmpty() &&
            lastMove != null &&
            moveKey != lastAnimatedMoveKey &&
            lastMove.length >= 4
        ) {
            val from = squareFrom(lastMove.take(2))
            val to = squareFrom(lastMove.drop(2).take(2))
            val movingPiece = previousBoard[from]
            if (movingPiece != null) {
                PieceMoveAnimationEvent(
                    id = ++animationSequence,
                    piece = movingPiece,
                    from = from,
                    to = to
                )
            } else null
        } else null

        lastAnimatedMoveKey = moveKey

        engine.loadFen(game.position)
        finished = game.status == "finished"
        _nextBot.value = game.nextBot
        _endReason.value = game.endReason

        val newMoves = game.moves
        val wasAtLatest = viewedPly == null || viewedPly == uciMoves.size
        uciMoves = newMoves

        val (capturedWhite, capturedBlack) = calculateCapturedPieces(game.moves)
        val turn = engine.turn()
        val inCheck = engine.checkSquare() != null
        val whiteTurn = turn == DomainColor.White
        val myTurn = whiteTurn == (myColor == PieceColor.WHITE)
        _botThinking.value = !finished && !myTurn

        val botName = if (game.botId != null) {
            "Bot (${game.botRating})"
        } else {
            "Custom Bot (${game.botRating})"
        }

        if (wasAtLatest) {
            viewedPly = null
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = null,
                    board = engine.pieces().toUiBoard(),
                    moveAnimationEvent = animationEvent ?: it.moveAnimationEvent,
                    whitePlayerName = if (myColor == PieceColor.WHITE) "You" else botName,
                    blackPlayerName = if (myColor == PieceColor.BLACK) "You" else botName,
                    isWhiteTurn = whiteTurn,
                    selectedSquare = null,
                    legalMoves = emptyList(),
                    promotionPending = null,
                    lastMoveFrom = lastMove?.let { m -> squareFrom(m.take(2)) },
                    lastMoveTo = lastMove?.let { m -> squareFrom(m.drop(2).take(2)) },
                    isWhiteInCheck = inCheck && whiteTurn,
                    isBlackInCheck = inCheck && !whiteTurn,
                    moves = game.moves.toMoveUi(),
                    capturedWhitePieces = capturedWhite,
                    capturedBlackPieces = capturedBlack,
                    isBoardFlipped = if (colorWasUnknown) myColor == PieceColor.BLACK else it.isBoardFlipped,
                    undoEnabled = true,
                    redoEnabled = false,
                    canUndo = uciMoves.isNotEmpty(),
                    canRedo = false,
                    gameResult = if (finished) resultFrom(game.result) else null
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = null,
                    whitePlayerName = if (myColor == PieceColor.WHITE) "You" else botName,
                    blackPlayerName = if (myColor == PieceColor.BLACK) "You" else botName,
                    moves = game.moves.toMoveUi(),
                    isBoardFlipped = if (colorWasUnknown) myColor == PieceColor.BLACK else it.isBoardFlipped,
                    undoEnabled = true,
                    redoEnabled = true,
                    canUndo = (viewedPly ?: 0) > 0,
                    canRedo = (viewedPly ?: 0) < uciMoves.size,
                    gameResult = if (finished) resultFrom(game.result) else null
                )
            }
        }
    }

    private fun resultFrom(result: String?) = when (result) {
        "white_win" -> GameResult.WHITE_WINS
        "black_win" -> GameResult.BLACK_WINS
        "draw" -> GameResult.DRAW
        else -> null
    }

    private fun squareFrom(name: String) =
        Square(row = 8 - name[1].digitToInt(), column = name[0] - 'a')

    override fun onCleared() {
        socket.close()
    }
}
