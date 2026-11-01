package com.abhinav.caisarra.presentation.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.local.RatingPreferencesStore
import com.abhinav.caisarra.data.local.TokenExpiryChecker
import com.abhinav.caisarra.data.local.TokenManager
import com.abhinav.caisarra.data.remote.GameSocketClient
import com.abhinav.caisarra.data.remote.RetrofitInstance
import com.abhinav.caisarra.data.remote.dto.ChatMessageDto
import com.abhinav.caisarra.data.remote.dto.CreateInviteResponse
import com.abhinav.caisarra.data.remote.dto.SocketIncoming
import com.abhinav.caisarra.data.remote.dto.SocketOutgoing
import com.abhinav.caisarra.data.repository.ChallengeManager
import com.abhinav.caisarra.data.repository.ChatRepository
import com.abhinav.caisarra.data.repository.InviteRepository
import com.abhinav.caisarra.data.repository.ProfileRepository
import com.abhinav.caisarra.data.repository.RemoteGameRepository
import com.abhinav.caisarra.domain.chess.engine.ChesslibEngine
import com.abhinav.caisarra.domain.chess.game.GameClock
import com.abhinav.caisarra.domain.chess.game.mapper.calculateCapturedPieces
import com.abhinav.caisarra.domain.chess.game.mapper.toDomain
import com.abhinav.caisarra.domain.chess.game.mapper.toMoveUi
import com.abhinav.caisarra.domain.chess.game.mapper.toUi
import com.abhinav.caisarra.domain.chess.game.mapper.toUiBoard
import com.abhinav.caisarra.domain.chess.game.mapper.toUiPiece
import com.abhinav.caisarra.domain.chess.game.mapper.uciToSquare
import com.abhinav.caisarra.domain.chess.model.Move
import com.abhinav.caisarra.domain.chess.model.Move as DomainMove
import com.abhinav.caisarra.domain.chess.model.PieceColor as DomainColor
import com.abhinav.caisarra.domain.chess.model.PieceColor as DomainPieceColor
import com.abhinav.caisarra.domain.chess.model.PieceType as DomainPieceType
import com.abhinav.caisarra.domain.chess.model.Square as DomainSquare
import com.abhinav.caisarra.presentation.game.model.GameResult
import com.abhinav.caisarra.presentation.game.model.GameUiState
import com.abhinav.caisarra.presentation.game.model.PieceColor
import com.abhinav.caisarra.presentation.game.model.PieceMoveAnimationEvent
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
    private val _rematchDeclined = MutableStateFlow<String?>(null)
    val rematchDeclined: StateFlow<String?> = _rematchDeclined.asStateFlow()
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
    private val _opponentAvatarUrl = MutableStateFlow<String?>(null)
    val opponentAvatarUrl: StateFlow<String?> = _opponentAvatarUrl.asStateFlow()
    private val _myAvatarUrl = MutableStateFlow<String?>(null)
    val myAvatarUrl: StateFlow<String?> = _myAvatarUrl.asStateFlow()
    private var whitePlayerId: Long? = null
    private var blackPlayerId: Long? = null
    private var whitePlayerUsername: String? = null
    private var blackPlayerUsername: String? = null
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
    private var uciMoves: List<String> = emptyList()
    private var viewedPly: Int? = null

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
        val tokenManager = TokenManager.get(getApplication())
        viewModelScope.launch {
            val token = tokenManager.getAccessToken()
            _myUserId.value = token?.let { TokenExpiryChecker.getUserId(it) }
            _myUsername.value = tokenManager.getUsername()
            runCatching { profileRepository.getMyProfile() }.getOrNull()?.let { profile ->
                _myAvatarUrl.value = profile.avatarUrl
                if (_myUsername.value.isNullOrBlank() && profile.username.isNotBlank()) {
                    _myUsername.value = profile.username
                }
            }
            enrichChatMessages()
        }
        viewModelScope.launch {
            if (!socket.connect(gameId)) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Please log in to play online.") }
                return@launch
            }
            if (withTimeoutOrNull(8000) { socket.connected.first { it } } == null) reconnect()
        }
        viewModelScope.launch {
            chatRepository.getMessages(gameId).onSuccess { history ->
                _chat.update { current -> (history + current).distinctBy { it.id }.sortedBy { it.createdAt } }
                enrichChatMessages()
            }
        }
        viewModelScope.launch { loadGameDetailsAndRating() }
        viewModelScope.launch {
            gameRepository.createWatchInvite(gameId).onSuccess { watchResp ->
                _uiState.update {
                    it.copy(
                        watchCode = watchResp.watchCode,
                        watchLink = watchResp.link
                    )
                }
            }
        }
        val challengeManager = ChallengeManager.get(getApplication())
        viewModelScope.launch {
            val outgoing = challengeManager.outgoingChallenge.value
            if (outgoing != null && (outgoing.gameId == gameId || outgoing.code == gameId)) {
                if (outgoing.status == "declined") {
                    val name = outgoing.opponentUsername.ifBlank {
                        _opponentName.value.takeIf { !it.equals("Opponent", true) && !it.equals("Your opponent", true) } ?: "Opponent"
                    }
                    _rematchDeclined.value = "$name declined the rematch"
                    _waitingForOpponent.value = false
                }
            }

            challengeManager.declinedEvent.collect { event ->
                val currentOutgoing = challengeManager.outgoingChallenge.value
                val matchesGameId = (event.gameId != null && event.gameId == gameId) ||
                        (currentOutgoing != null && currentOutgoing.gameId == gameId && (
                                (event.code != null && event.code == currentOutgoing.code) ||
                                        (event.opponentId != null && event.opponentId == currentOutgoing.opponentId) ||
                                        (!event.opponentUsername.isNullOrBlank() && event.opponentUsername.equals(currentOutgoing.opponentUsername, true))
                                ))
                val matchesOpponent = _waitingForOpponent.value && (
                        matchesGameId ||
                                (!event.opponentUsername.isNullOrBlank() &&
                                        !_opponentName.value.equals("Opponent", true) &&
                                        !_opponentName.value.equals("Your opponent", true) &&
                                        event.opponentUsername.equals(_opponentName.value, true))
                        )

                if (matchesOpponent) {
                    val name = event.opponentUsername?.takeIf { it.isNotBlank() }
                        ?: currentOutgoing?.opponentUsername?.takeIf { it.isNotBlank() }
                        ?: _opponentName.value.takeIf { !it.equals("Opponent", true) && !it.equals("Your opponent", true) }
                        ?: "Opponent"
                    _rematchDeclined.value = "$name declined the rematch"
                    _waitingForOpponent.value = false
                }
            }
        }
        viewModelScope.launch {
            challengeManager.cancelledEvent.collect { event ->
                val currentOutgoing = challengeManager.outgoingChallenge.value
                if (_waitingForOpponent.value && currentOutgoing?.gameId == gameId) {
                    _rematchDeclined.value = "Rematch challenge was cancelled"
                    _waitingForOpponent.value = false
                }
            }
        }
        viewModelScope.launch {
            while (_waitingForOpponent.value && _rematchDeclined.value == null) {
                delay(2500L)
                if (!_waitingForOpponent.value || _rematchDeclined.value != null) break
                val game = runCatching { gameRepository.getGame(gameId) }.getOrNull()
                if (game != null) {
                    val status = game.status.lowercase()
                    if (status in setOf("declined", "cancelled", "aborted")) {
                        val name = _opponentName.value.takeIf { !it.equals("Opponent", true) && !it.equals("Your opponent", true) } ?: "Opponent"
                        _rematchDeclined.value = if (status == "declined") "$name declined the rematch" else "Rematch was cancelled"
                        _waitingForOpponent.value = false
                        break
                    }
                }
            }
        }
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
        if (game != null) {
            val status = game.status.lowercase()
            if (status in setOf("declined", "cancelled", "aborted")) {
                if (_waitingForOpponent.value) {
                    val name = _opponentName.value.takeIf { !it.equals("Opponent", true) && !it.equals("Your opponent", true) } ?: "Opponent"
                    _rematchDeclined.value = if (status == "declined") "$name declined the rematch" else "Rematch was cancelled"
                    _waitingForOpponent.value = false
                }
            }
            if (game.whitePlayerId != 0L) whitePlayerId = game.whitePlayerId
            if (game.blackPlayerId != 0L) blackPlayerId = game.blackPlayerId
            if (!game.whiteUsername.isNullOrBlank()) whitePlayerUsername = game.whiteUsername
            if (!game.blackUsername.isNullOrBlank()) blackPlayerUsername = game.blackUsername
        }
        val minutes = game?.timeControlMinutes ?: _uiState.value.timeControlMinutes
        mode = resolveMode(minutes, game?.timeControlMode ?: mode)
        val daily = minutes >= 1440
        _uiState.update { current ->
            current.copy(
                isRated = game?.rated ?: current.isRated,
                timeControlMinutes = game?.timeControlMinutes ?: current.timeControlMinutes,
                incrementSeconds = game?.incrementMs?.div(1000L)?.toInt() ?: current.incrementSeconds,
                isDailyMode = if (daily) true else current.isDailyMode
            )
        }
        if (game != null && game.position.isNotBlank()) {
            runCatching {
                if (_uiState.value.board.isEmpty()) {
                    engine.loadFen(game.position)
                    _uiState.update { current ->
                        if (current.board.isEmpty()) {
                            current.copy(
                                isLoading = false,
                                board = engine.pieces().toUiBoard(),
                                isDailyMode = daily
                            )
                        } else current
                    }
                }
            }
        }
        val myId = _myUserId.value
        val myName = _myUsername.value
        val resolvedOpponent = when {
            myId != null && myId == game?.whitePlayerId -> game?.blackUsername
            myId != null && myId == game?.blackPlayerId -> game?.whiteUsername
            myName != null && myName.equals(game?.whiteUsername, ignoreCase = true) -> game?.blackUsername
            myName != null && myName.equals(game?.blackUsername, ignoreCase = true) -> game?.whiteUsername
            else -> game?.blackUsername ?: game?.whiteUsername
        }?.takeIf { it.isNotBlank() && !it.equals("Opponent", true) && !it.equals("Your opponent", true) }

        if (!resolvedOpponent.isNullOrBlank()) {
            lastOpponentUsername = resolvedOpponent
            _opponentName.value = resolvedOpponent
            loadOpponentProfile(resolvedOpponent)
        }
        refreshRating(captureChange = false)
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
            if (captureChange && change != null) {
                RatingPreferencesStore.get(getApplication()).setRatingChange(currentMode, change)
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
            appendLine("[Event \"Mosaic Rated\"]")
            appendLine("[Site \"Mosaic\"]")
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

    fun getOpponentId(): Long? {
        val myId = _myUserId.value
        return when {
            myId != null && myId == whitePlayerId && blackPlayerId != 0L -> blackPlayerId
            myId != null && myId == blackPlayerId && whitePlayerId != 0L -> whitePlayerId
            myColor == PieceColor.WHITE && blackPlayerId != 0L -> blackPlayerId
            myColor == PieceColor.BLACK && whitePlayerId != 0L -> whitePlayerId
            whitePlayerId != 0L && blackPlayerId != 0L -> {
                if (myColor == PieceColor.WHITE) blackPlayerId else whitePlayerId
            }
            else -> null
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
        val opponentId = getOpponentId()
        val opponentName = _opponentName.value.takeIf { !it.equals("Opponent", true) && !it.equals("Your opponent", true) } ?: "Opponent"
        val challengeManager = ChallengeManager.get(getApplication())
        val res = if (opponentId != null && opponentId > 0) {
            challengeManager.sendChallenge(
                timeControlMinutes = state.timeControlMinutes.coerceAtLeast(1),
                incrementSeconds = state.incrementSeconds.coerceAtLeast(0),
                color = myColorChoice,
                rated = state.isRated,
                opponentId = opponentId,
                opponentUsername = opponentName
            )
        } else {
            inviteRepository.createInvite(
                timeControlMinutes = state.timeControlMinutes.coerceAtLeast(1),
                incrementSeconds = state.incrementSeconds.coerceAtLeast(0),
                color = myColorChoice,
                rated = state.isRated,
                opponentId = null
            )
        }
        res.onSuccess { invite ->
            if (opponentId != null && opponentId > 0) {
                challengeManager.registerOutgoingChallenge(
                    code = invite.inviteCode,
                    gameId = invite.gameId,
                    opponentId = opponentId,
                    opponentUsername = opponentName
                )
            }
        }
        rematchInFlight = false
        return res
    }

    fun cancelWaitingChallenge(onDone: () -> Unit = {}) {
        viewModelScope.launch {
            val challengeManager = ChallengeManager.get(getApplication())
            val outgoing = challengeManager.outgoingChallenge.value
            val code = if (outgoing?.gameId == gameId) outgoing.code else null
            if (!code.isNullOrBlank()) {
                challengeManager.cancelChallenge(code)
            }
            challengeManager.clearOutgoingChallenge()
            _waitingForOpponent.value = false
            _rematchDeclined.value = "Rematch challenge cancelled"
            onDone()
        }
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
            val whiteTurn = turn == DomainPieceColor.White
            val (capturedWhite, capturedBlack) = calculateCapturedPieces(uciMoves)
            val lastMove = uciMoves.lastOrNull()
            _uiState.update {
                it.copy(
                    board = engine.pieces().toUiBoard(),
                    selectedSquare = null,
                    legalMoves = emptyList(),
                    promotionPending = null,
                    lastMoveFrom = lastMove?.let { m -> uciToSquare(m.take(2)) },
                    lastMoveTo = lastMove?.let { m -> uciToSquare(m.drop(2).take(2)) },
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
            val move = DomainMove(
                from = DomainSquare(fromFile, fromRank),
                to = DomainSquare(toFile, toRank),
                promotion = promotion
            )
            replayEngine.play(move)
        }

        val lastMove = historyMoves.lastOrNull()
        if (lastMove != null && lastMove.length >= 4) {
            lastFrom = uciToSquare(lastMove.take(2))
            lastTo = uciToSquare(lastMove.drop(2).take(2))
        }

        val inCheck = replayEngine.checkSquare() != null
        val turn = replayEngine.turn()
        val whiteTurn = turn == DomainPieceColor.White
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
            is SocketIncoming.SpectatorCount -> {
                _uiState.update { it.copy(spectatorCount = message.count) }
            }
            is SocketIncoming.WatchEnded -> Unit
        }
    }

    private fun addChat(message: SocketIncoming.ChatMessage) {
        val myId = _myUserId.value
        val myName = _myUsername.value
        val isMe = (myId != null && message.senderId == myId) ||
                (myName != null && message.username.equals(myName, true))

        val senderOpponentName = when (message.senderId) {
            whitePlayerId -> whitePlayerUsername
            blackPlayerId -> blackPlayerUsername
            else -> null
        }
        val oppName = senderOpponentName
            ?: _opponentName.value.takeIf { !it.equals("Opponent", true) && !it.equals("Your opponent", true) }
            ?: lastOpponentUsername

        val oppAvatar = _opponentAvatarUrl.value

        val username = if (isMe) {
            myName ?: message.username ?: "You"
        } else {
            message.username?.takeIf { !it.equals("Opponent", true) && !it.equals("Your opponent", true) }
                ?: oppName
                ?: "Opponent"
        }

        val avatar = if (isMe) {
            message.avatarUrl ?: _myAvatarUrl.value
        } else {
            message.avatarUrl ?: oppAvatar
        }

        val dto = ChatMessageDto(
            id = message.id,
            gameId = message.gameId,
            senderId = message.senderId,
            username = username,
            message = message.message,
            createdAt = message.createdAt,
            avatarUrl = avatar
        )
        _chat.update { current -> (current + dto).distinctBy { it.id } }

        if (!isMe && oppName != null && _opponentAvatarUrl.value == null) {
            viewModelScope.launch { loadOpponentProfile(oppName) }
        }
    }

    private fun enrichChatMessages() {
        val myId = _myUserId.value
        val myName = _myUsername.value
        val myAvatar = _myAvatarUrl.value
        val oppName = _opponentName.value.takeIf { !it.equals("Opponent", true) && !it.equals("Your opponent", true) }
            ?: lastOpponentUsername
        val oppAvatar = _opponentAvatarUrl.value

        _chat.update { messages ->
            messages.map { msg ->
                val isMe = (myId != null && msg.senderId == myId) ||
                        (myName != null && msg.username.equals(myName, true))
                val targetName = if (isMe) {
                    myName ?: msg.username ?: "You"
                } else {
                    msg.username?.takeIf { !it.equals("Opponent", true) && !it.equals("Your opponent", true) }
                        ?: oppName
                        ?: "Opponent"
                }
                val targetAvatar = if (isMe) {
                    msg.avatarUrl ?: myAvatar
                } else {
                    msg.avatarUrl ?: oppAvatar
                }

                if (msg.username != targetName || msg.avatarUrl != targetAvatar) {
                    msg.copy(username = targetName, avatarUrl = targetAvatar)
                } else {
                    msg
                }
            }
        }
    }

    private suspend fun applyState(state: SocketIncoming.GameState) {
        val moveWasAwaitingServer = awaitingServer
        awaitingServer = false

        if (state.whitePlayerId != 0L) whitePlayerId = state.whitePlayerId
        if (state.blackPlayerId != 0L) blackPlayerId = state.blackPlayerId
        if (!state.whiteUsername.isNullOrBlank()) whitePlayerUsername = state.whiteUsername
        if (!state.blackUsername.isNullOrBlank()) blackPlayerUsername = state.blackUsername

        val colorWasUnknown = myColor == null
        if (colorWasUnknown) myColor = resolveColor(state)

        val myId = _myUserId.value
        val myName = _myUsername.value

        val candidateOpponent = when {
            myColor == PieceColor.WHITE -> state.blackUsername ?: blackPlayerUsername
            myColor == PieceColor.BLACK -> state.whiteUsername ?: whitePlayerUsername
            myId != null && myId == state.whitePlayerId -> state.blackUsername ?: blackPlayerUsername
            myId != null && myId == state.blackPlayerId -> state.whiteUsername ?: whitePlayerUsername
            myName != null && myName.equals(state.whiteUsername, ignoreCase = true) -> state.blackUsername ?: blackPlayerUsername
            myName != null && myName.equals(state.blackUsername, ignoreCase = true) -> state.whiteUsername ?: whitePlayerUsername
            else -> null
        }?.takeIf { it.isNotBlank() && !it.equals("Opponent", true) && !it.equals("Your opponent", true) }

        val resolvedOpponent = candidateOpponent
            ?: _opponentName.value.takeIf { !it.equals("Opponent", true) && !it.equals("Your opponent", true) }
            ?: lastOpponentUsername

        val resolvedMode = resolveMode(state.timeControlMinutes, mode)
        val modeChanged = mode != resolvedMode
        mode = resolvedMode

        if (!resolvedOpponent.isNullOrBlank()) {
            _opponentName.value = resolvedOpponent
            if (lastOpponentUsername != resolvedOpponent || _uiState.value.opponentRating == null || _opponentAvatarUrl.value == null || modeChanged) {
                lastOpponentUsername = resolvedOpponent
                viewModelScope.launch { loadOpponentProfile(resolvedOpponent) }
            }
        }
        if (modeChanged) {
            viewModelScope.launch { refreshRating(captureChange = false) }
        }

        val whiteName = state.whiteUsername?.takeIf { it.isNotBlank() }
            ?: whitePlayerUsername
            ?: when (myColor) {
                PieceColor.WHITE -> "You"
                PieceColor.BLACK -> resolvedOpponent ?: "Opponent"
                else -> null
            }
        val blackName = state.blackUsername?.takeIf { it.isNotBlank() }
            ?: blackPlayerUsername
            ?: when (myColor) {
                PieceColor.BLACK -> "You"
                PieceColor.WHITE -> resolvedOpponent ?: "Opponent"
                else -> null
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
        val newUciMoves = state.moves?.map { it.move }.orEmpty()
        val wasAtLatest = viewedPly == null || viewedPly == uciMoves.size
        uciMoves = newUciMoves

        val (capturedWhite, capturedBlack) =
            calculateCapturedPieces(uciMoves)

        if (wasAtLatest) {
            viewedPly = null
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

                    lastMoveFrom = state.lastMove?.takeIf { it.length >= 4 }?.let {
                        uciToSquare(it.take(2))
                    } ?: previous.lastMoveFrom,

                    lastMoveTo = state.lastMove?.takeIf { it.length >= 4 }?.let {
                        uciToSquare(it.drop(2).take(2))
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

                    undoEnabled = true,
                    redoEnabled = false,
                    canUndo = uciMoves.isNotEmpty(),
                    canRedo = false,
                    gameResult = if (finished) resultFrom(state.result) else null
                ).withSideRatings()
            }
        } else {
            _uiState.update { previous ->
                previous.copy(
                    isLoading = false,
                    errorMessage = null,
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

                    moves = state.moves
                        ?.map { it.move }
                        ?.toMoveUi()
                        ?: previous.moves,

                    isBoardFlipped = if (colorWasUnknown) {
                        myColor == PieceColor.BLACK
                    } else {
                        previous.isBoardFlipped
                    },

                    undoEnabled = true,
                    redoEnabled = true,
                    canUndo = (viewedPly ?: 0) > 0,
                    canRedo = (viewedPly ?: 0) < uciMoves.size,
                    gameResult = if (finished) resultFrom(state.result) else null
                ).withSideRatings()
            }
        }
        if (finished && _uiState.value.isRated && !ratingWasRefreshed) {
            ratingWasRefreshed = true
            viewModelScope.launch {
                refreshRating(captureChange = true)
                lastOpponentUsername?.let { loadOpponentProfile(it) }
            }
        }
    }

    private suspend fun loadOpponentProfile(username: String) {
        if (username.isBlank() || username.equals("You", true) || username.equals("Opponent", true) || username.equals("Your opponent", true)) return
        lastOpponentUsername = username
        _opponentName.value = username

        val profile = runCatching { ratingService.getPublicProfile(username).data }.getOrNull()
        if (profile != null) {
            val currentMode = mode ?: resolveMode(_uiState.value.timeControlMinutes)
            val rating = profile.ratings?.get(currentMode)?.rating ?: profile.rating
            _uiState.update { current ->
                current.copy(opponentRating = rating ?: current.opponentRating).withSideRatings()
            }
            if (!profile.avatarUrl.isNullOrBlank()) {
                _opponentAvatarUrl.value = profile.avatarUrl
            }
        }
        enrichChatMessages()
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

    override fun onCleared() {
        socket.close()
        clock.stop()
    }
}
