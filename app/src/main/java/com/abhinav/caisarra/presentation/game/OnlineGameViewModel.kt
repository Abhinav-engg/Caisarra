package com.abhinav.caisarra.presentation.game



import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.remote.GameSocketClient
import com.abhinav.caisarra.data.remote.dto.SocketIncoming
import com.abhinav.caisarra.data.remote.dto.SocketOutgoing
import com.abhinav.caisarra.data.repository.AuthRepository
import com.abhinav.caisarra.domain.chess.engine.ChesslibEngine
import com.abhinav.caisarra.domain.chess.game.GameClock
import com.abhinav.caisarra.domain.chess.model.Move
import com.abhinav.caisarra.domain.chess.model.PieceColor as DomainColor
import com.abhinav.caisarra.presentation.game.mapper.toDomain
import com.abhinav.caisarra.presentation.game.mapper.toMoveUi
import com.abhinav.caisarra.presentation.game.mapper.toUi
import com.abhinav.caisarra.presentation.game.mapper.toUiBoard
import com.abhinav.caisarra.presentation.game.model.GameResult
import com.abhinav.caisarra.presentation.game.model.GameUiState
import com.abhinav.caisarra.presentation.game.model.PieceColor
import com.abhinav.caisarra.presentation.game.model.PieceType
import com.abhinav.caisarra.presentation.game.model.Square
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnlineGameViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val gameId: String = checkNotNull(savedStateHandle["gameId"])
    private var awaitingServer = false
    private val engine = ChesslibEngine()
    private val socket = GameSocketClient(application)
    private val clock = GameClock(viewModelScope, 0L, 0L) { }

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _waitingForOpponent = MutableStateFlow(true)
    val waitingForOpponent: StateFlow<Boolean> = _waitingForOpponent.asStateFlow()

    private var myUsername: String? = null
    private var myColor: PieceColor? = null
    private var pendingPromotionFrom: Square? = null
    private var finished = false

    init {
        viewModelScope.launch {
            socket.incoming.collect { handle(it) }
        }
        viewModelScope.launch {
            clock.times.collect { (white, black) ->
                _uiState.update { it.copy(whiteTimeMillis = white, blackTimeMillis = black) }
            }
        }
        viewModelScope.launch {
            var wasConnected = false
            socket.connected.collect { connected ->
                if (connected) {
                    wasConnected = true
                } else if (wasConnected && !finished) {
                    _uiState.update { it.copy(errorMessage = "Connection lost. Reconnecting...") }
                    delay(2000)
                    socket.connect(gameId)
                }
            }
        }
        viewModelScope.launch {
            socket.connect(gameId)
        }
    }

    fun onSquareClick(square: Square) {
        val state = _uiState.value
        val color = myColor ?: return
        if (finished || awaitingServer || state.promotionPending != null) return
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
        socket.send(SocketOutgoing.Resign)
    }

    fun offerDraw() {
        if (finished) return
        socket.send(SocketOutgoing.OfferDraw)
    }

    fun acceptDraw() {
        socket.send(SocketOutgoing.AcceptDraw)
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
        val sent = socket.send(SocketOutgoing.Move(move.uci))
        awaitingServer = sent
        if (sent) {
            viewModelScope.launch {
                delay(3000)
                awaitingServer = false
            }
        }
        _uiState.update {
            it.copy(
                selectedSquare = null,
                legalMoves = emptyList(),
                promotionPending = null,
                errorMessage = if (sent) it.errorMessage else "Connection lost. Move not sent."
            )
        }
    }

    private suspend fun handle(message: SocketIncoming) {
        when (message) {
            is SocketIncoming.GameStart -> _waitingForOpponent.value = false
            is SocketIncoming.GameState -> applyState(message)
            is SocketIncoming.ChatMessage -> Unit
        }
    }

    private suspend fun applyState(state: SocketIncoming.GameState) {
        awaitingServer = false
        val me = myUsername ?: AuthRepository.get(getApplication()).getUsername().also { myUsername = it }
        val colorWasUnknown = myColor == null
        if (colorWasUnknown) {
            myColor = when {
                me == null -> null
                me == state.whiteUsername -> PieceColor.WHITE
                me == state.blackUsername -> PieceColor.BLACK
                else -> null
            }
        }

        engine.loadFen(state.position)
        finished = state.status == "finished"
        if (state.status == "active" || finished) _waitingForOpponent.value = false

        val turn = engine.turn()
        val inCheck = engine.checkSquare() != null
        val whiteTurn = turn == DomainColor.White

        clock.restore(state.whiteTimeMs, state.blackTimeMs)
        if (finished) clock.stop() else clock.start(turn)

        _uiState.update {
            it.copy(
                isLoading = false,
                errorMessage = null,
                board = engine.pieces().toUiBoard(),
                whitePlayerName = state.whiteUsername ?: it.whitePlayerName,
                blackPlayerName = state.blackUsername ?: it.blackPlayerName,
                isWhiteTurn = whiteTurn,
                selectedSquare = null,
                legalMoves = emptyList(),
                promotionPending = null,
                lastMoveFrom = state.lastMove?.let { m -> squareFrom(m.take(2)) } ?: it.lastMoveFrom,
                lastMoveTo = state.lastMove?.let { m -> squareFrom(m.drop(2).take(2)) } ?: it.lastMoveTo,
                isWhiteInCheck = inCheck && whiteTurn,
                isBlackInCheck = inCheck && !whiteTurn,
                moves = state.moves?.toMoveUi() ?: it.moves,
                isBoardFlipped = if (colorWasUnknown) myColor == PieceColor.BLACK else it.isBoardFlipped,
                undoEnabled = false,
                gameResult = if (finished) resultFrom(state.result) else null
            )
        }
    }

    private fun resultFrom(result: String?) = when (result) {
        "white" -> GameResult.WHITE_WINS
        "black" -> GameResult.BLACK_WINS
        "draw" -> GameResult.DRAW
        else -> null
    }

    private fun squareFrom(name: String) =
        Square(row = 8 - name[1].digitToInt(), column = name[0] - 'a')

    override fun onCleared() {
        socket.close()
        clock.stop()
    }

}