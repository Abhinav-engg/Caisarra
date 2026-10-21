package com.abhinav.caisarra.presentation.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.local.TokenExpiryChecker
import com.abhinav.caisarra.data.local.TokenManager
import com.abhinav.caisarra.data.remote.GameSocketClient
import com.abhinav.caisarra.data.remote.RetrofitInstance
import com.abhinav.caisarra.data.remote.dto.ChatMessageDto
import com.abhinav.caisarra.data.remote.dto.SocketIncoming
import com.abhinav.caisarra.data.remote.dto.SocketOutgoing
import com.abhinav.caisarra.data.repository.ChatRepository
import com.abhinav.caisarra.data.repository.InviteRepository
import com.abhinav.caisarra.data.repository.ProfileRepository
import com.abhinav.caisarra.data.repository.RemoteGameRepository
import com.abhinav.caisarra.data.remote.dto.CreateInviteResponse
import com.abhinav.caisarra.domain.chess.engine.ChesslibEngine
import com.abhinav.caisarra.domain.chess.game.GameClock
import com.abhinav.caisarra.domain.chess.model.Move
import com.abhinav.caisarra.domain.chess.model.PieceColor as DomainColor
import com.abhinav.caisarra.domain.chess.game.mapper.toDomain
import com.abhinav.caisarra.domain.chess.game.mapper.toMoveUi
import com.abhinav.caisarra.domain.chess.game.mapper.toUi
import com.abhinav.caisarra.domain.chess.game.mapper.toUiBoard
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
import com.abhinav.caisarra.domain.chess.model.Move as DomainMove
import com.abhinav.caisarra.domain.chess.model.Square as DomainSquare
import com.abhinav.caisarra.domain.chess.model.PieceType as DomainPieceType
import com.abhinav.caisarra.domain.chess.model.PieceColor as DomainPieceColor
import com.abhinav.caisarra.domain.chess.game.mapper.toUiPiece

class OnlineGameViewModel(
    application: Application,
    private val gameId: String
) : AndroidViewModel(application) {

    private var awaitingServer = false

    private val engine = ChesslibEngine()
    private val socket = GameSocketClient(application)
    private val chatRepository = ChatRepository.get(application)
    private val gameRepository = RemoteGameRepository(
        RetrofitInstance.createGamesService(TokenManager.get(application))
    )
    private val profileRepository = ProfileRepository(
        RetrofitInstance.createProfileService(TokenManager.get(application))
    )
    private val ratingService = RetrofitInstance.createRatingService(TokenManager.get(application))
    private val inviteRepository = InviteRepository.get(application)
    private val clock = GameClock(viewModelScope, 0L, 0L) { }

    private val _uiState = MutableStateFlow(GameUiState(isRated = true))
    
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
    private val _waitingForOpponent = MutableStateFlow(true)
    val waitingForOpponent: StateFlow<Boolean> = _waitingForOpponent.asStateFlow()
    private val _chat = MutableStateFlow<List<ChatMessageDto>>(emptyList())
    val chat: StateFlow<List<ChatMessageDto>> = _chat.asStateFlow()

    private val _myUserId = MutableStateFlow<Long?>(null)
    val myUserId: StateFlow<Long?> = _myUserId.asStateFlow()
    private val _myUsername = MutableStateFlow<String?>(null)
    private val _drawOfferedBy = MutableStateFlow<Long?>(null)
    val drawOfferedBy: StateFlow<Long?> = _drawOfferedBy.asStateFlow()
    private val _chatMuted = MutableStateFlow(false)
    val chatMuted: StateFlow<Boolean> = _chatMuted.asStateFlow()
    private val _opponentName = MutableStateFlow("Your opponent")
    val opponentName: StateFlow<String> = _opponentName.asStateFlow()
    private var myColor: PieceColor? = null
    private var pendingPromotionFrom: Square? = null
    private var finished = false
    private var reconnecting = false
    private var mode: String? = null
    private var ratingWasRefreshed = false
    private var rematchInFlight = false
    private var lastOpponentUsername: String? = null
    private var animationSequence = 0L
    private var lastAnimatedMoveKey: String? = null

    init {
        viewModelScope.launch {
            socket.incoming.collect { handle(it) }
        }
        viewModelScope.launch {
            clock.times.collect { (white, black) ->
                _uiState.update { state ->
                    if (state.isDailyMode) {
                        state
                    } else {
                        state.copy(
                            whiteTimeMillis = white,
                            blackTimeMillis = black
                        )
                    }
                }
            }
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
                _uiState.update { it.copy(isLoading = false, errorMessage = "Please log in to play online.") }
                return@launch
            }
            _myUserId.value = TokenManager.get(getApplication()).getAccessToken()
                ?.let { TokenExpiryChecker.getUserId(it) }
            _myUsername.value = TokenManager.get(getApplication()).getUsername()
            if (withTimeoutOrNull(8000) { socket.connected.first { it } } == null) reconnect()
        }
        viewModelScope.launch {
            chatRepository.getMessages(gameId).onSuccess { history ->
                _chat.update { current -> (history + current).distinctBy { it.id }.sortedBy { it.createdAt } }
            }
        }
        viewModelScope.launch { loadGameDetailsAndRating() }
    }

    private fun resolveMode(timeControlMinutes: Int, explicitMode: String? = null): String {
        val normalized = explicitMode?.trim()?.lowercase()
        if (normalized in setOf("bullet", "blitz", "rapid", "daily")) {
            return normalized!!
        }
        return when {
            timeControlMinutes >= 1440 -> "daily"
            timeControlMinutes < 3 -> "bullet"
            timeControlMinutes < 10 -> "blitz"
            else -> "rapid"
        }
    }

    private suspend fun loadGameDetailsAndRating() {
        val game = runCatching { gameRepository.getGame(gameId) }.getOrNull()
        val minutes = game?.timeControlMinutes ?: _uiState.value.timeControlMinutes
        mode = resolveMode(minutes, game?.timeControlMode ?: mode)
        _uiState.update { current ->
            current.copy(
                isRated = game?.rated ?: current.isRated,
                timeControlMinutes = game?.timeControlMinutes ?: current.timeControlMinutes,
                incrementSeconds = game?.incrementMs?.div(1000L)?.toInt() ?: current.incrementSeconds
            )
        }
        refreshRating(captureChange = false)
        lastOpponentUsername?.let { loadOpponentRating(it) }
    }

    private suspend fun refreshRating(captureChange: Boolean) {
        val profile = runCatching { profileRepository.getMyProfile() }.getOrNull() ?: return
        val currentMode = mode ?: resolveMode(_uiState.value.timeControlMinutes)
        val currentRating = profile.ratings?.get(currentMode)?.rating ?: profile.rating
        _uiState.update { current ->
            val change = if (captureChange && current.playerRating != null && currentRating != null) {
                currentRating - current.playerRating
            } else {
                current.ratingChange
            }
            current.copy(
                ratingChange = change,
                playerRating = currentRating
            ).withSideRatings()
        }
    }

    fun pgn(): String {
        val state = _uiState.value
        val result = when (state.gameResult) {
            GameResult.WHITE_WINS -> "1-0"
            GameResult.BLACK_WINS -> "0-1"
            GameResult.DRAW -> "1/2-1/2"
            null -> "*"
        }
        val moveText = state.moves.joinToString(" ") { move ->
            buildString {
                append(move.moveNumber)
                append('.')
                move.whiteMove?.let { append(' ').append(it) }
                move.blackMove?.let { append(' ').append(it) }
            }
        }
        return buildString {
            appendLine("[Event \"Caisaara Rated\"]")
            appendLine("[Site \"Caisaara\"]")
            appendLine("[White \"${state.whitePlayerName}\"]")
            appendLine("[Black \"${state.blackPlayerName}\"]")
            appendLine("[Result \"$result\"]")
            appendLine("[TimeControl \"${state.timeControlMinutes}+${state.incrementSeconds}\"]")
            appendLine()
            append(moveText)
            if (moveText.isNotBlank()) append(' ')
            append(result)
        }
    }

    suspend fun createRematch(): Result<CreateInviteResponse> {
        if (rematchInFlight) return Result.failure(IllegalStateException("Rematch already started"))
        rematchInFlight = true
        val state = _uiState.value
        val myColorChoice = when (state.myColor) {
            PieceColor.WHITE -> "black"
            PieceColor.BLACK -> "white"
            null -> "random"
        }
        return inviteRepository.createInvite(
            timeControlMinutes = state.timeControlMinutes.coerceAtLeast(1),
            incrementSeconds = state.incrementSeconds.coerceAtLeast(0),
            color = myColorChoice,
            rated = true
        ).also { rematchInFlight = false }
    }

    private fun reconnect() {
        if (reconnecting) return
        reconnecting = true
        viewModelScope.launch {
            var attempt = 0
            while (!finished && !socket.connected.value) {
                _uiState.update { it.copy(errorMessage = "Connection lost. Reconnecting...") }
                delay(
                    if (_waitingForOpponent.value) 2000L
                    else minOf(2000L shl attempt.coerceAtMost(2), 8000L)
                )
                if (!socket.connect(gameId)) break
                withTimeoutOrNull(5000) { socket.connected.first { it } }
                attempt++
            }
            reconnecting = false
        }
    }

    fun onSquareClick(square: Square) {
        val state = _uiState.value
        val color = myColor ?: return

        if (finished || awaitingServer || state.promotionPending != null) return
        if (state.isWhiteTurn != (color == PieceColor.WHITE)) return

        val selected = state.selectedSquare

        when {
            selected != null && square in state.legalMoves -> {
                move(selected, square)
            }

            state.board[square]?.color == color -> {
                _uiState.update {
                    it.copy(
                        selectedSquare = square,
                        legalMoves = engine.legalMovesFrom(square.toDomain())
                            .map { legalMove -> legalMove.to.toUi() }
                            .distinct()
                    )
                }
            }

            else -> {
                _uiState.update {
                    it.copy(
                        selectedSquare = null,
                        legalMoves = emptyList(),
                        lastMoveAccepted = if (it.isDailyMode) false else null
                    )
                }
            }
        }
    }

    fun onPromote(type: PieceType) {
        val from = pendingPromotionFrom ?: return
        val to = _uiState.value.promotionPending ?: return
        pendingPromotionFrom = null
        send(from, to, type)
    }

    fun sendChat(text: String) {
        val message = text.trim()
        if (message.isEmpty()) return
        if (!socket.send(SocketOutgoing.Chat(message))) {
            _uiState.update { it.copy(errorMessage = "Connection lost. Message not sent.") }
        }
    }

    fun resign() {
        if (finished) return
        socket.send(SocketOutgoing.Resign)
    }

    fun offerDraw() {
        val me = _myUserId.value
        if (finished || (me != null && _drawOfferedBy.value == me)) return
        socket.send(SocketOutgoing.OfferDraw)
    }

    fun acceptDraw() {
        if (finished) return
        socket.send(SocketOutgoing.AcceptDraw)
    }

    fun declineDraw() {
        if (finished) return
        socket.send(SocketOutgoing.DeclineDraw)
    }

    fun toggleChatMute() {
        _chatMuted.update { !it }
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

    private fun send(
        from: Square,
        to: Square,
        promotion: PieceType?
    ) {
        val move = Move(
            from.toDomain(),
            to.toDomain(),
            promotion?.toDomain()
        )

        val legal = engine.legalMovesFrom(move.from).any {
            it.to == move.to && it.promotion == move.promotion
        }

        if (!legal) {
            _uiState.update {
                it.copy(
                    selectedSquare = null,
                    legalMoves = emptyList(),
                    promotionPending = null,
                    lastMoveAccepted = if (it.isDailyMode) false else null
                )
            }
            return
        }

        val sent = socket.send(SocketOutgoing.Move(move.uci))
        awaitingServer = sent

        if (sent) {
            viewModelScope.launch {
                delay(3000)
                if (awaitingServer) {
                    awaitingServer = false
                    _uiState.update {
                        it.copy(
                            lastMoveAccepted = if (it.isDailyMode) {
                                false
                            } else {
                                null
                            },
                            errorMessage = if (it.isDailyMode) {
                                "Move confirmation timed out. Check connection."
                            } else {
                                it.errorMessage
                            }
                        )
                    }
                }
            }
        }

        _uiState.update {
            it.copy(
                selectedSquare = null,
                legalMoves = emptyList(),
                promotionPending = null,
                lastMoveAccepted = if (it.isDailyMode) {
                    if (sent) null else false
                } else {
                    null
                },
                errorMessage = if (sent) {
                    it.errorMessage
                } else {
                    "Connection lost. Move not sent."
                }
            )
        }
    }




    private suspend fun handle(message: SocketIncoming) {
        when (message) {
            is SocketIncoming.GameStart -> _waitingForOpponent.value = false
            is SocketIncoming.GameState -> applyState(message)
            is SocketIncoming.ChatMessage -> addChat(message)
            is SocketIncoming.Waiting -> Unit
            is SocketIncoming.DrawOffer -> _drawOfferedBy.value = message.offeredBy
            is SocketIncoming.DrawDeclined -> {
                _drawOfferedBy.value = null
                if (message.declinedBy != _myUserId.value) {
                    _uiState.update { it.copy(errorMessage = "Draw offer declined") }
                }
            }
            is SocketIncoming.Error -> {
                awaitingServer = false
                _uiState.update { it.copy(errorMessage = message.message) }
            }
        }
    }

    private fun addChat(message: SocketIncoming.ChatMessage) {
        val dto = ChatMessageDto(
            id = message.id,
            gameId = message.gameId,
            senderId = message.senderId,
            message = message.message,
            createdAt = message.createdAt
        )
        _chat.update { current -> (current + dto).distinctBy { it.id } }
    }

    private suspend fun applyState(state: SocketIncoming.GameState) {
        val moveWasAwaitingServer = awaitingServer
        awaitingServer = false

        val colorWasUnknown = myColor == null
        if (colorWasUnknown) myColor = resolveColor(state)

        val whiteName = state.whiteUsername?.takeIf { it.isNotBlank() } ?: when (myColor) {
            PieceColor.WHITE -> "You"
            PieceColor.BLACK -> "Opponent"
            else -> null
        }
        val blackName = state.blackUsername?.takeIf { it.isNotBlank() } ?: when (myColor) {
            PieceColor.BLACK -> "You"
            PieceColor.WHITE -> "Opponent"
            else -> null
        }
        val resolvedOpponent = when (myColor) {
            PieceColor.WHITE -> blackName
            PieceColor.BLACK -> whiteName
            else -> null
        }
        val resolvedMode = resolveMode(state.timeControlMinutes, mode)
        val modeChanged = mode != resolvedMode
        mode = resolvedMode

        if (!resolvedOpponent.isNullOrBlank()) {
            _opponentName.value = resolvedOpponent
            if (lastOpponentUsername != resolvedOpponent || _uiState.value.opponentRating == null || modeChanged) {
                lastOpponentUsername = resolvedOpponent
                viewModelScope.launch { loadOpponentRating(resolvedOpponent) }
            }
        }
        if (modeChanged) {
            viewModelScope.launch { refreshRating(captureChange = false) }
        }

        val previousBoard = _uiState.value.board
        val moveKey = state.lastMove?.let {
            "$it:${state.moves?.size ?: 0}"
        }

        val animationEvent = if (
            previousBoard.isNotEmpty() &&
            !state.lastMove.isNullOrBlank() &&
            moveKey != lastAnimatedMoveKey &&
            state.lastMove.length >= 4
        ) {
            val from = squareFrom(state.lastMove.take(2))
            val to = squareFrom(state.lastMove.drop(2).take(2))
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

        engine.loadFen(state.position)
        finished = state.status == "finished"

        _drawOfferedBy.value = if (finished) {
            null
        } else {
            state.drawOfferedBy?.takeIf { it != 0L }
        }

        if (
            state.status == "active" ||
            finished ||
            (state.whiteUsername != null && state.blackUsername != null)
        ) {
            _waitingForOpponent.value = false
        }

        val turn = engine.turn()
        val inCheck = engine.checkSquare() != null
        val whiteTurn = turn == DomainColor.White

        val dailyMode = state.timeControlMinutes >= 1440

        val daysPerMove = if (dailyMode) {
            (state.timeControlMinutes + 1439) / 1440
        } else {
            0
        }

        val initialDailyTimeMs = state.timeControlMinutes.toLong() * 60L * 1000L
        val whiteTimeMs = if (dailyMode && state.whiteTimeMs <= 0L && !finished) initialDailyTimeMs else state.whiteTimeMs
        val blackTimeMs = if (dailyMode && state.blackTimeMs <= 0L && !finished) initialDailyTimeMs else state.blackTimeMs

        if (dailyMode || finished) {
            clock.stop()
        } else {
            clock.restore(
                state.whiteTimeMs,
                state.blackTimeMs
            )
            clock.start(turn)
        }
        val (capturedWhite, capturedBlack) =
            calculateCapturedPieces(
                state.moves?.map { it.move }.orEmpty()
            )

        _uiState.update { previous ->
            previous.copy(
                isLoading = false,
                errorMessage = null,
                board = engine.pieces().toUiBoard(),
                moveAnimationEvent = animationEvent ?: previous.moveAnimationEvent,
                capturedWhitePieces = capturedWhite,
                capturedBlackPieces = capturedBlack,

                whitePlayerName = whiteName
                    ?: previous.whitePlayerName,
                blackPlayerName = blackName
                    ?: previous.blackPlayerName,

                whiteTimeMillis = whiteTimeMs,
                blackTimeMillis = blackTimeMs,

                isDailyMode = dailyMode,
                dailyDaysPerMove = daysPerMove,

                lastMoveAccepted = if (
                    dailyMode && moveWasAwaitingServer
                ) {
                    true
                } else {
                    null
                },

                myColor = myColor,
                isWhiteTurn = whiteTurn,
                selectedSquare = null,
                legalMoves = emptyList(),
                promotionPending = null,

                lastMoveFrom = state.lastMove?.let {
                    squareFrom(it.take(2))
                } ?: previous.lastMoveFrom,

                lastMoveTo = state.lastMove?.let {
                    squareFrom(it.drop(2).take(2))
                } ?: previous.lastMoveTo,

                isWhiteInCheck = inCheck && whiteTurn,
                isBlackInCheck = inCheck && !whiteTurn,

                moves = state.moves
                    ?.map { it.move }
                    ?.toMoveUi()
                    ?: previous.moves,

                isBoardFlipped = if (colorWasUnknown) {
                    myColor == PieceColor.BLACK
                } else {
                    previous.isBoardFlipped
                },

                undoEnabled = false,
                gameResult = if (finished) resultFrom(state.result) else null
            ).withSideRatings()
        }
        if (finished && _uiState.value.isRated && !ratingWasRefreshed) {
            ratingWasRefreshed = true
            viewModelScope.launch {
                refreshRating(captureChange = true)
                lastOpponentUsername?.let { loadOpponentRating(it) }
            }
        }
    }
    private fun calculateCapturedPieces(
        uciMoves: List<String>
    ): Pair<List<com.abhinav.caisarra.presentation.game.model.Piece>,
            List<com.abhinav.caisarra.presentation.game.model.Piece>> {

        val replayEngine = ChesslibEngine()

        val capturedPieces = mutableListOf<
                com.abhinav.caisarra.domain.chess.model.BoardPiece
                >()

        uciMoves.forEach { uci ->
            if (uci.length < 4) return@forEach

            val fromFile = uci[0] - 'a'
            val fromRank = uci[1].digitToIntOrNull()?.minus(1)
                ?: return@forEach
            val toFile = uci[2] - 'a'
            val toRank = uci[3].digitToIntOrNull()?.minus(1)
                ?: return@forEach

            if (fromFile !in 0..7 || toFile !in 0..7 ||
                fromRank !in 0..7 || toRank !in 0..7
            ) {
                return@forEach
            }

            val promotion = when (uci.getOrNull(4)?.lowercaseChar()) {
                'q' -> DomainPieceType.Queen
                'r' -> DomainPieceType.Rook
                'b' -> DomainPieceType.Bishop
                'n' -> DomainPieceType.Knight
                else -> null
            }

            val move = DomainMove(
                from = DomainSquare(fromFile, fromRank),
                to = DomainSquare(toFile, toRank),
                promotion = promotion
            )

            val captured = replayEngine.capturedBy(move)

            if (replayEngine.play(move) && captured != null) {
                capturedPieces.add(captured)
            }
        }

        val whitePiecesCaptured = capturedPieces
            .filter { it.color == DomainPieceColor.White }
            .map { it.toUiPiece() }

        val blackPiecesCaptured = capturedPieces
            .filter { it.color == DomainPieceColor.Black }
            .map { it.toUiPiece() }

        return whitePiecesCaptured to blackPiecesCaptured
    }


    private suspend fun loadOpponentRating(username: String) {
        if (username.isBlank() || username.equals("You", true) || username.equals("Opponent", true)) return
        val profile = runCatching { ratingService.getPublicProfile(username).data }.getOrNull() ?: return
        val currentMode = mode ?: resolveMode(_uiState.value.timeControlMinutes)
        val rating = profile.ratings?.get(currentMode)?.rating ?: profile.rating
        _uiState.update { current ->
            current.copy(opponentRating = rating ?: current.opponentRating).withSideRatings()
        }
    }

    private fun GameUiState.withSideRatings(): GameUiState {
        val mine = playerRating
        val theirs = opponentRating
        return copy(
            whiteRating = when (myColor) {
                PieceColor.WHITE -> mine
                PieceColor.BLACK -> theirs
                null -> whiteRating ?: (if (whitePlayerName == "You") mine else theirs)
            },
            blackRating = when (myColor) {
                PieceColor.BLACK -> mine
                PieceColor.WHITE -> theirs
                null -> blackRating ?: (if (blackPlayerName == "You") mine else theirs)
            }
        )
    }

    private suspend fun resolveColor(state: SocketIncoming.GameState): PieceColor? {
        val myId = _myUserId.value
        val myUsername = _myUsername.value
        var white = state.whitePlayerId
        var black = state.blackPlayerId
        if (white == 0L && black == 0L) {
            val game = runCatching { gameRepository.getGame(gameId) }.getOrNull()
            white = game?.whitePlayerId ?: 0L
            black = game?.blackPlayerId ?: 0L
        }
        if (myId != null && (white != 0L || black != 0L)) {
            if (myId == white) return PieceColor.WHITE
            if (myId == black) return PieceColor.BLACK
        }
        if (!myUsername.isNullOrBlank()) {
            if (state.whiteUsername.equals(myUsername, ignoreCase = true)) return PieceColor.WHITE
            if (state.blackUsername.equals(myUsername, ignoreCase = true)) return PieceColor.BLACK
        }
        return null
    }

    private fun resultFrom(result: String?) = when (result) {
        "white", "white_win" -> GameResult.WHITE_WINS
        "black", "black_win" -> GameResult.BLACK_WINS
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
