package com.abhinav.caisarra.presentation.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.remote.BotSocketClient
import com.abhinav.caisarra.data.remote.dto.BotGameDto
import com.abhinav.caisarra.data.remote.dto.BotSocketIncoming
import com.abhinav.caisarra.data.remote.dto.BotSocketOutgoing
import com.abhinav.caisarra.domain.chess.engine.ChesslibEngine
import com.abhinav.caisarra.domain.chess.game.mapper.toDomain
import com.abhinav.caisarra.domain.chess.game.mapper.toMoveUi
import com.abhinav.caisarra.domain.chess.game.mapper.toUi
import com.abhinav.caisarra.domain.chess.game.mapper.toUiBoard
import com.abhinav.caisarra.domain.chess.model.Move
import com.abhinav.caisarra.domain.chess.model.PieceColor as DomainColor
import com.abhinav.caisarra.presentation.game.model.GameResult
import com.abhinav.caisarra.presentation.game.model.GameUiState
import com.abhinav.caisarra.presentation.game.model.PieceColor
import com.abhinav.caisarra.presentation.game.model.PieceType
import com.abhinav.caisarra.presentation.game.model.Square
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import com.abhinav.caisarra.presentation.game.model.PieceMoveAnimationEvent
import com.abhinav.caisarra.domain.chess.model.PieceType as DomainPieceType
import com.abhinav.caisarra.domain.chess.model.Square as DomainSquare
import com.abhinav.caisarra.domain.chess.game.mapper.toUiPiece
class BotGameViewModel(
    application: Application,
    private val gameId: String
) : AndroidViewModel(application) {

    private val engine = ChesslibEngine()
    private val socket = BotSocketClient(application)

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
    fun onMoveAnimationStarted(eventId: Long) {
        _uiState.update { current ->
            if (current.moveAnimationEvent?.id == eventId) {
                current.copy(moveAnimationConsumedId = eventId)
            } else {
                current
            }
        }
    }

    private val _botThinking = MutableStateFlow(false)
    val botThinking: StateFlow<Boolean> = _botThinking.asStateFlow()

    private var myColor: PieceColor? = null
    private var pendingPromotionFrom: Square? = null
    private var awaitingServer = false
    private var finished = false
    private var reconnecting = false
    private var moveCount = 0
    private var animationSequence = 0L
    private var lastAnimatedMoveKey: String? = null

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
                _uiState.update { it.copy(isLoading = false, errorMessage = "Please log in to play against the bot.") }
                return@launch
            }
            if (withTimeoutOrNull(8000) { socket.connected.first { it } } == null) reconnect()
        }
    }

    private fun reconnect() {
        if (reconnecting) return
        reconnecting = true
        viewModelScope.launch {
            var attempt = 0
            while (!finished && !socket.connected.value && attempt < 5) {
                _uiState.update { it.copy(errorMessage = "Connection lost. Reconnecting...") }
                delay(minOf(2000L shl attempt.coerceAtMost(2), 8000L))
                if (!socket.connect(gameId)) break
                withTimeoutOrNull(5000) { socket.connected.first { it } }
                attempt++
            }
            if (!finished && !socket.connected.value) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Couldn't connect to the game.") }
            }
            reconnecting = false
        }
    }

    fun onSquareClick(square: Square) {
        val state = _uiState.value
        val color = myColor ?: return
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
        socket.send(BotSocketOutgoing.Resign)
    }

    private fun resignIfUntouched() {
        val color = myColor ?: return
        val botOpening = if (color == PieceColor.BLACK) 1 else 0
        if (finished || moveCount > botOpening) return
        socket.send(BotSocketOutgoing.Resign)
    }

    fun flipBoard() {
        _uiState.update { it.copy(isBoardFlipped = !it.isBoardFlipped) }
    }

    fun dismissResult() {
        _uiState.update { it.copy(gameResult = null) }
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
        val sent = socket.send(BotSocketOutgoing.Move(move.uci))
        awaitingServer = sent
        _uiState.update {
            it.copy(
                selectedSquare = null,
                legalMoves = emptyList(),
                promotionPending = null,
                errorMessage = if (sent) null else "Connection lost. Move not sent."
            )
        }
    }

    private fun handle(message: BotSocketIncoming) {
        when (message) {
            is BotSocketIncoming.GameState -> applyState(message.game)
            is BotSocketIncoming.BotThinking -> _botThinking.value = true
            is BotSocketIncoming.Error -> {
                awaitingServer = false
                _botThinking.value = false
                _uiState.update {
                    it.copy(
                        selectedSquare = null,
                        legalMoves = emptyList(),
                        promotionPending = null,
                        errorMessage = message.message
                    )
                }
                if (message.message.startsWith("bot could not move")) reconnect()
            }
        }
    }
    private fun calculateCapturedPieces(
        uciMoves: List<String>
    ): Pair<
            List<com.abhinav.caisarra.presentation.game.model.Piece>,
            List<com.abhinav.caisarra.presentation.game.model.Piece>
            > {
        val replayEngine = ChesslibEngine()

        val captured = mutableListOf<
                com.abhinav.caisarra.domain.chess.model.BoardPiece
                >()

        for (uci in uciMoves) {
            if (uci.length < 4) continue

            val fromFile = uci[0] - 'a'
            val fromRank = uci[1].digitToIntOrNull()?.minus(1)
            val toFile = uci[2] - 'a'
            val toRank = uci[3].digitToIntOrNull()?.minus(1)

            if (
                fromFile !in 0..7 ||
                toFile !in 0..7 ||
                fromRank !in 0..7 ||
                toRank !in 0..7
            ) {
                continue
            }

            val promotion = when (uci.getOrNull(4)?.lowercaseChar()) {
                'q' -> DomainPieceType.Queen
                'r' -> DomainPieceType.Rook
                'b' -> DomainPieceType.Bishop
                'n' -> DomainPieceType.Knight
                else -> null
            }

            val move = Move(
                from = DomainSquare(fromFile, fromRank!!),
                to = DomainSquare(toFile, toRank!!),
                promotion = promotion
            )
            val capturedPiece = replayEngine.capturedBy(move)

            if (replayEngine.play(move) && capturedPiece != null) {
                captured.add(capturedPiece)
            }
        }

        val whiteCaptured = captured
            .filter { it.color == DomainColor.White }
            .map { it.toUiPiece() }

        val blackCaptured = captured
            .filter { it.color == DomainColor.Black }
            .map { it.toUiPiece() }

        return whiteCaptured to blackCaptured
    }


    private fun applyState(game: BotGameDto) {
        awaitingServer = false
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
            } else {
                null
            }
        } else {
            null
        }

        lastAnimatedMoveKey = moveKey

        engine.loadFen(game.position)
        finished = game.status == "finished"
        moveCount = game.moves.size
        val (capturedWhite, capturedBlack) =
            calculateCapturedPieces(game.moves)


        val turn = engine.turn()
        val inCheck = engine.checkSquare() != null
        val whiteTurn = turn == DomainColor.White
        val myTurn = whiteTurn == (myColor == PieceColor.WHITE)
        _botThinking.value = !finished && !myTurn

        val botName = "Bot (${game.botRating})"
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
                undoEnabled = false,
                gameResult = if (finished) resultFrom(game.result) else null
            )
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
        resignIfUntouched()
        socket.close()
    }
}