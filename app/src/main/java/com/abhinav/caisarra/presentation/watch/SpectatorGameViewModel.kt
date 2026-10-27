package com.abhinav.caisarra.presentation.watch

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.remote.GameSocketClient
import com.abhinav.caisarra.data.remote.dto.SocketIncoming
import com.abhinav.caisarra.domain.chess.engine.ChesslibEngine
import com.abhinav.caisarra.domain.chess.game.mapper.calculateCapturedPieces
import com.abhinav.caisarra.domain.chess.game.mapper.toMoveUi
import com.abhinav.caisarra.domain.chess.game.mapper.toUiBoard
import com.abhinav.caisarra.domain.chess.game.mapper.uciToSquare
import com.abhinav.caisarra.domain.chess.model.PieceColor as DomainPieceColor
import com.abhinav.caisarra.presentation.game.model.PieceMoveAnimationEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SpectatorGameViewModel(
    application: Application,
    private val watchCode: String
) : AndroidViewModel(application) {

    private val socketClient = GameSocketClient(application)
    private val engine = ChesslibEngine()

    private val _uiState = MutableStateFlow(SpectatorUiState(watchCode = watchCode))
    val uiState: StateFlow<SpectatorUiState> = _uiState.asStateFlow()

    private var clockJob: Job? = null
    private var animationSequence = 0L
    private var lastAnimatedMoveKey: String? = null

    init {
        connect()
        observeSocket()
    }

    private fun connect() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val success = socketClient.connectWatch(watchCode)
            if (!success) {
                val error = socketClient.errorMessage.value ?: "Could not connect to spectator service."
                _uiState.update { it.copy(isLoading = false, errorMessage = error) }
            }
        }
    }

    private fun observeSocket() {
        viewModelScope.launch {
            socketClient.connected.collect { connected ->
                _uiState.update { it.copy(isConnected = connected) }
            }
        }

        viewModelScope.launch {
            socketClient.errorMessage.collect { error ->
                if (!error.isNullOrBlank()) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = error) }
                }
            }
        }

        viewModelScope.launch {
            socketClient.incoming.collect { incoming ->
                handleIncoming(incoming)
            }
        }
    }

    private fun handleIncoming(incoming: SocketIncoming) {
        when (incoming) {
            is SocketIncoming.GameState -> applyGameState(incoming)
            is SocketIncoming.SpectatorCount -> {
                _uiState.update { it.copy(spectatorCount = incoming.count) }
            }
            is SocketIncoming.WatchEnded -> {
                stopClock()
                _uiState.update { it.copy(isWatchEnded = true, status = "finished") }
            }
            is SocketIncoming.Error -> {
                _uiState.update { it.copy(errorMessage = incoming.message) }
            }
            else -> Unit
        }
    }

    private fun applyGameState(state: SocketIncoming.GameState) {
        val previousBoard = _uiState.value.board
        val moveCount = state.moves?.size ?: 0
        val moveKey = state.lastMove?.let { "$it:$moveCount" }

        val animationEvent = if (
            previousBoard.isNotEmpty() &&
            !state.lastMove.isNullOrBlank() &&
            moveKey != lastAnimatedMoveKey &&
            state.lastMove.length >= 4
        ) {
            val from = uciToSquare(state.lastMove.take(2))
            val to = uciToSquare(state.lastMove.drop(2).take(2))
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

        engine.loadFen(state.position)
        val inCheck = engine.checkSquare() != null
        val turn = engine.turn()
        val isWhiteTurn = turn == DomainPieceColor.White

        val rawMoves = state.moves?.map { it.move }.orEmpty()
        val moveUiList = rawMoves.toMoveUi()
        val (capturedWhite, capturedBlack) = calculateCapturedPieces(rawMoves)

        val lastFrom = state.lastMove?.takeIf { it.length >= 4 }?.let { uciToSquare(it.take(2)) }
        val lastTo = state.lastMove?.takeIf { it.length >= 4 }?.let { uciToSquare(it.drop(2).take(2)) }

        val isFinished = state.status.equals("finished", ignoreCase = true)

        _uiState.update { current ->
            current.copy(
                isLoading = false,
                errorMessage = null,
                gameId = state.gameId,
                whitePlayerName = state.whiteUsername?.takeIf { it.isNotBlank() } ?: "White",
                blackPlayerName = state.blackUsername?.takeIf { it.isNotBlank() } ?: "Black",
                whiteTimeMs = state.whiteTimeMs,
                blackTimeMs = state.blackTimeMs,
                isWhiteTurn = isWhiteTurn,
                isWhiteInCheck = inCheck && isWhiteTurn,
                isBlackInCheck = inCheck && !isWhiteTurn,
                board = engine.pieces().toUiBoard(),
                moves = moveUiList,
                rawMoves = rawMoves,
                lastMoveFrom = lastFrom,
                lastMoveTo = lastTo,
                moveAnimationEvent = animationEvent ?: current.moveAnimationEvent,
                capturedWhitePieces = capturedWhite,
                capturedBlackPieces = capturedBlack,
                status = state.status,
                result = state.result,
                endReason = state.endReason,
                rated = state.rated ?: true
            )
        }

        if (isFinished) {
            stopClock()
        } else {
            startClock(isWhiteTurn, state.whiteTimeMs, state.blackTimeMs)
        }
    }

    private fun startClock(isWhiteTurn: Boolean, whiteMs: Long, blackMs: Long) {
        clockJob?.cancel()
        clockJob = viewModelScope.launch {
            var currentWhite = whiteMs
            var currentBlack = blackMs
            var lastTime = System.currentTimeMillis()

            while (isActive) {
                delay(100)
                val now = System.currentTimeMillis()
                val delta = now - lastTime
                lastTime = now

                if (isWhiteTurn) {
                    currentWhite = (currentWhite - delta).coerceAtLeast(0L)
                } else {
                    currentBlack = (currentBlack - delta).coerceAtLeast(0L)
                }

                _uiState.update {
                    it.copy(
                        whiteTimeMs = currentWhite,
                        blackTimeMs = currentBlack
                    )
                }
            }
        }
    }

    private fun stopClock() {
        clockJob?.cancel()
        clockJob = null
    }

    fun flipBoard() {
        _uiState.update { it.copy(isFlipped = !it.isFlipped) }
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

    fun leave() {
        socketClient.sendRaw("""{"type":"leave"}""")
        socketClient.close()
    }

    override fun onCleared() {
        super.onCleared()
        stopClock()
        socketClient.close()
    }
}
