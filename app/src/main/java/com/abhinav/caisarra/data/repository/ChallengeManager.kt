package com.abhinav.caisarra.data.repository

import android.content.Context
import android.util.Log
import com.abhinav.caisarra.data.local.TokenManager
import com.abhinav.caisarra.data.remote.ChallengeNotificationEvent
import com.abhinav.caisarra.data.remote.NotificationSocketClient
import com.abhinav.caisarra.data.remote.RetrofitInstance
import com.abhinav.caisarra.data.remote.dto.AcceptChallengeResponse
import com.abhinav.caisarra.data.remote.dto.ChallengeDto
import com.abhinav.caisarra.data.remote.dto.CreateInviteResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OutgoingChallengeState(
    val code: String,
    val gameId: String,
    val opponentId: Long,
    val opponentUsername: String,
    val status: String // "pending", "accepted", "declined"
)

class ChallengeManager private constructor(private val context: Context) {

    private val inviteRepository = InviteRepository.get(context)
    private val socketClient = NotificationSocketClient.get(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _incomingChallenge = MutableStateFlow<ChallengeNotificationEvent.Received?>(null)
    val incomingChallenge: StateFlow<ChallengeNotificationEvent.Received?> = _incomingChallenge.asStateFlow()

    private val _outgoingChallenge = MutableStateFlow<OutgoingChallengeState?>(null)
    val outgoingChallenge: StateFlow<OutgoingChallengeState?> = _outgoingChallenge.asStateFlow()

    private val _acceptedEvent = MutableSharedFlow<ChallengeNotificationEvent.Accepted>(extraBufferCapacity = 16)
    val acceptedEvent: SharedFlow<ChallengeNotificationEvent.Accepted> = _acceptedEvent.asSharedFlow()

    private val _declinedEvent = MutableSharedFlow<ChallengeNotificationEvent.Declined>(extraBufferCapacity = 16)
    val declinedEvent: SharedFlow<ChallengeNotificationEvent.Declined> = _declinedEvent.asSharedFlow()

    private val _cancelledEvent = MutableSharedFlow<ChallengeNotificationEvent.Cancelled>(extraBufferCapacity = 16)
    val cancelledEvent: SharedFlow<ChallengeNotificationEvent.Cancelled> = _cancelledEvent.asSharedFlow()

    private val _receivedChallenges = MutableStateFlow<List<ChallengeDto>>(emptyList())
    val receivedChallenges: StateFlow<List<ChallengeDto>> = _receivedChallenges.asStateFlow()

    private val _sentChallenges = MutableStateFlow<List<ChallengeDto>>(emptyList())
    val sentChallenges: StateFlow<List<ChallengeDto>> = _sentChallenges.asStateFlow()

    init {
        scope.launch {
            socketClient.challengeEvents.collectLatest { event ->
                handleChallengeEvent(event)
            }
        }
    }

    fun startListening() {
        socketClient.connect()
        refreshChallenges()
    }

    fun stopListening() {
        socketClient.disconnect()
    }

    fun refreshChallenges() {
        scope.launch(Dispatchers.IO) {
            refreshReceivedChallenges()
            refreshSentChallenges()
        }
    }

    suspend fun refreshReceivedChallenges() {
        inviteRepository.getReceivedChallenges()
            .onSuccess { list ->
                _receivedChallenges.value = list
                if (_incomingChallenge.value == null) {
                    val pending = list.firstOrNull { it.status.equals("pending", ignoreCase = true) }
                    if (pending != null) {
                        _incomingChallenge.value = ChallengeNotificationEvent.Received(
                            code = pending.code,
                            gameId = pending.gameId,
                            link = pending.link,
                            challengerId = pending.challengerId,
                            challengerUsername = pending.challengerUsername,
                            title = "Game Challenge",
                            body = "${pending.challengerUsername} challenged you to a game"
                        )
                    }
                }
            }
            .onFailure {
                Log.w(TAG, "Failed to load received challenges: ${it.message}")
            }
    }

    fun postChallengeReceived(
        code: String,
        gameId: String = "",
        link: String? = null,
        challengerId: Long = 0L,
        challengerUsername: String = "",
        title: String = "New challenge",
        body: String = ""
    ) {
        val event = ChallengeNotificationEvent.Received(
            code = code,
            gameId = gameId,
            link = link,
            challengerId = challengerId,
            challengerUsername = challengerUsername,
            title = title.ifBlank { "New challenge" },
            body = body.ifBlank { "${challengerUsername.ifBlank { "Opponent" }} challenged you to a game" }
        )
        _incomingChallenge.value = event
        scope.launch(Dispatchers.IO) { refreshReceivedChallenges() }
    }

    fun postChallengeAccepted(code: String?, gameId: String, opponentId: Long? = null, opponentUsername: String? = null) {
        val event = ChallengeNotificationEvent.Accepted(
            code = code,
            gameId = gameId,
            link = null,
            opponentId = opponentId,
            opponentUsername = opponentUsername,
            title = "Challenge accepted",
            body = "${opponentUsername ?: "Opponent"} accepted your challenge!"
        )
        handleChallengeEvent(event)
    }

    fun postChallengeDeclined(code: String?, gameId: String?, opponentId: Long? = null, opponentUsername: String? = null) {
        val event = ChallengeNotificationEvent.Declined(
            code = code,
            gameId = gameId,
            opponentId = opponentId,
            opponentUsername = opponentUsername,
            title = "Challenge declined",
            body = "${opponentUsername ?: "Opponent"} declined your challenge"
        )
        handleChallengeEvent(event)
    }

    fun postChallengeCancelled(code: String?, gameId: String?, challengerId: Long? = null, challengerUsername: String? = null) {
        val event = ChallengeNotificationEvent.Cancelled(
            code = code,
            gameId = gameId,
            challengerId = challengerId,
            challengerUsername = challengerUsername,
            title = "Challenge cancelled",
            body = "${challengerUsername ?: "Challenger"} cancelled the challenge"
        )
        handleChallengeEvent(event)
    }

    suspend fun refreshSentChallenges() {
        inviteRepository.getSentChallenges()
            .onSuccess { list ->
                _sentChallenges.value = list
            }
            .onFailure {
                Log.w(TAG, "Failed to load sent challenges: ${it.message}")
            }
    }

    private var outgoingPollJob: Job? = null

    suspend fun sendChallenge(
        timeControlMinutes: Int,
        incrementSeconds: Int,
        color: String,
        rated: Boolean,
        opponentId: Long,
        opponentUsername: String
    ): Result<CreateInviteResponse> {
        val result = inviteRepository.createInvite(
            timeControlMinutes = timeControlMinutes,
            incrementSeconds = incrementSeconds,
            color = color,
            rated = rated,
            opponentId = opponentId
        )

        result.onSuccess { res ->
            val out = OutgoingChallengeState(
                code = res.inviteCode,
                gameId = res.gameId,
                opponentId = opponentId,
                opponentUsername = opponentUsername,
                status = "pending"
            )
            _outgoingChallenge.value = out
            startOutgoingWatcher(out)
            refreshSentChallenges()
        }

        return result
    }

    private fun startOutgoingWatcher(state: OutgoingChallengeState) {
        outgoingPollJob?.cancel()
        outgoingPollJob = scope.launch(Dispatchers.IO) {
            val gamesService = RetrofitInstance.createGamesService(TokenManager.get(context))
            val gameRepo = RemoteGameRepository(gamesService)
            while (_outgoingChallenge.value?.code == state.code && _outgoingChallenge.value?.status == "pending") {
                delay(2000L)
                val game = runCatching { gameRepo.getGame(state.gameId) }.getOrNull()
                if (game != null && (game.status.equals("active", true) || (game.whitePlayerId != 0L && game.blackPlayerId != 0L))) {
                    _outgoingChallenge.value = state.copy(status = "accepted")
                    _acceptedEvent.tryEmit(
                        ChallengeNotificationEvent.Accepted(
                            code = state.code,
                            gameId = state.gameId,
                            link = null,
                            opponentId = state.opponentId,
                            opponentUsername = state.opponentUsername,
                            title = "Challenge accepted",
                            body = "${state.opponentUsername} accepted your challenge!"
                        )
                    )
                    refreshSentChallenges()
                    break
                }

                val sentResult = runCatching { inviteRepository.getSentChallenges() }.getOrNull()
                val sentList = sentResult?.getOrNull()
                if (sentList != null) {
                    _sentChallenges.value = sentList
                    val stillInSent = sentList.any { it.code == state.code }
                    if (!stillInSent) {
                        val finalGame = runCatching { gameRepo.getGame(state.gameId) }.getOrNull()
                        if (finalGame != null && (finalGame.status.equals("active", true) || (finalGame.whitePlayerId != 0L && finalGame.blackPlayerId != 0L))) {
                            _outgoingChallenge.value = state.copy(status = "accepted")
                            _acceptedEvent.tryEmit(
                                ChallengeNotificationEvent.Accepted(
                                    code = state.code,
                                    gameId = state.gameId,
                                    link = null,
                                    opponentId = state.opponentId,
                                    opponentUsername = state.opponentUsername,
                                    title = "Challenge accepted",
                                    body = "${state.opponentUsername} accepted your challenge!"
                                )
                            )
                        } else {
                            _outgoingChallenge.value = state.copy(status = "declined")
                            _declinedEvent.tryEmit(
                                ChallengeNotificationEvent.Declined(
                                    code = state.code,
                                    gameId = state.gameId,
                                    opponentId = state.opponentId,
                                    opponentUsername = state.opponentUsername,
                                    title = "Challenge declined",
                                    body = "${state.opponentUsername} declined your challenge"
                                )
                            )
                        }
                        break
                    }
                }
            }
        }
    }

    suspend fun acceptChallenge(code: String): Result<AcceptChallengeResponse> {
        val result = inviteRepository.acceptChallenge(code)
        result.onSuccess {
            if (_incomingChallenge.value?.code == code) {
                _incomingChallenge.value = null
            }
            refreshReceivedChallenges()
        }
        return result
    }

    suspend fun declineChallenge(code: String): Result<Unit> {
        val result = inviteRepository.declineChallenge(code)
        return if (result.isSuccess) {
            if (_incomingChallenge.value?.code == code) {
                _incomingChallenge.value = null
            }
            refreshReceivedChallenges()
            Result.success(Unit)
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Failed to decline challenge"))
        }
    }

    suspend fun cancelChallenge(code: String): Result<Unit> {
        val result = inviteRepository.cancelChallenge(code)
        return if (result.isSuccess) {
            outgoingPollJob?.cancel()
            if (_outgoingChallenge.value?.code == code) {
                _outgoingChallenge.value = null
            }
            refreshSentChallenges()
            Result.success(Unit)
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Failed to cancel challenge"))
        }
    }

    fun dismissIncomingChallenge() {
        _incomingChallenge.value = null
    }

    fun clearOutgoingChallenge() {
        outgoingPollJob?.cancel()
        _outgoingChallenge.value = null
    }

    fun registerOutgoingChallenge(
        code: String,
        gameId: String,
        opponentId: Long,
        opponentUsername: String
    ) {
        val out = OutgoingChallengeState(
            code = code,
            gameId = gameId,
            opponentId = opponentId,
            opponentUsername = opponentUsername,
            status = "pending"
        )
        _outgoingChallenge.value = out
        startOutgoingWatcher(out)
        scope.launch(Dispatchers.IO) { refreshSentChallenges() }
    }

    private fun handleChallengeEvent(event: ChallengeNotificationEvent) {
        when (event) {
            is ChallengeNotificationEvent.Received -> {
                _incomingChallenge.value = event
                scope.launch(Dispatchers.IO) { refreshReceivedChallenges() }
            }

            is ChallengeNotificationEvent.Accepted -> {
                val outgoing = _outgoingChallenge.value
                if (outgoing != null && (event.code == outgoing.code || event.gameId == outgoing.gameId)) {
                    _outgoingChallenge.value = outgoing.copy(status = "accepted")
                }
                _acceptedEvent.tryEmit(event)
                scope.launch(Dispatchers.IO) { refreshSentChallenges() }
            }

            is ChallengeNotificationEvent.Declined -> {
                val outgoing = _outgoingChallenge.value
                val matches = outgoing != null && (
                        (event.code != null && event.code == outgoing.code) ||
                                (event.gameId != null && event.gameId == outgoing.gameId) ||
                                (event.opponentId != null && event.opponentId == outgoing.opponentId) ||
                                (event.opponentUsername != null && event.opponentUsername.equals(outgoing.opponentUsername, true)) ||
                                (event.opponentId == null && event.code == null && event.gameId == null)
                        )
                if (outgoing != null && matches) {
                    _outgoingChallenge.value = outgoing.copy(status = "declined")
                }
                _declinedEvent.tryEmit(event)
                scope.launch(Dispatchers.IO) { refreshSentChallenges() }
            }

            is ChallengeNotificationEvent.Cancelled -> {
                val incoming = _incomingChallenge.value
                if (incoming != null && (event.challengerId == null || event.challengerId == incoming.challengerId ||
                            event.challengerUsername == incoming.challengerUsername)) {
                    _incomingChallenge.value = null
                }
                _cancelledEvent.tryEmit(event)
                scope.launch(Dispatchers.IO) { refreshReceivedChallenges() }
            }
        }
    }

    companion object {
        private const val TAG = "ChallengeManager"

        @Volatile
        private var instance: ChallengeManager? = null

        fun get(context: Context): ChallengeManager =
            instance ?: synchronized(this) {
                instance ?: ChallengeManager(context.applicationContext).also { instance = it }
            }
    }
}
