package com.abhinav.caisarra.presentation.game.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.core.network.userMessage
import com.abhinav.caisarra.data.remote.api.MatchmakingJoinRequest
import com.abhinav.caisarra.data.remote.api.MatchmakingService
import com.abhinav.caisarra.data.remote.api.TiltWarning
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

data class MatchmakingUiState(
    val isSearching: Boolean = false,
    val isCancelling: Boolean = false,
    val message: String = "",
    val error: String? = null,
    val matchedGameId: String? = null,
    val tiltWarning: TiltWarning? = null
)

class MatchmakingViewModel(
    private val service: MatchmakingService
) : ViewModel() {

    private val _state = MutableStateFlow(MatchmakingUiState())
    val state = _state.asStateFlow()

    private var searchJob: Job? = null
    private var statusJob: Job? = null
    private var cancelJob: Job? = null

    @Volatile
    private var possiblyQueued = false
    private var autoStarted = false

    fun startMatchOnce(
        minutes: Int,
        incrementSeconds: Int,
        rated: Boolean = true
    ) {
        if (autoStarted) return
        autoStarted = true
        startMatch(minutes, incrementSeconds, rated)
    }

    fun startMatch(
        minutes: Int,
        incrementSeconds: Int,
        rated: Boolean = true,
        ignoreTilt: Boolean = false
    ) {
        if (_state.value.isSearching || cancelJob?.isActive == true) return
        searchJob?.cancel()
        statusJob?.cancel()
        _state.value = MatchmakingUiState(
            isSearching = true,
            message = "Joining matchmaking queue..."
        )
        searchJob = viewModelScope.launch {
            try {
                if (possiblyQueued) {
                    runCatching { service.cancelQueue() }
                        .onFailure { if (it is CancellationException) throw it }
                }
                possiblyQueued = true
                val response = service.joinQueue(
                    MatchmakingJoinRequest(
                        timeControlMinutes = minutes,
                        incrementSeconds = incrementSeconds,
                        rated = rated,
                        ignoreTilt = ignoreTilt
                    )
                )
                when (response.state) {
                    "searching" -> startPolling()

                    "matched" -> {
                        val gameId = response.gameId
                        if (gameId.isNullOrBlank()) {
                            fail("Matched response did not contain a game ID.")
                        } else {
                            matched(gameId)
                        }
                    }
                    else -> fail(
                        response.error ?: "Could not join matchmaking."
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: HttpException) {
                val errorBody = e.response()
                    ?.errorBody()
                    ?.string()
                val isTiltWarning = e.code() == 409 && errorBody?.contains("tilt_warning") == true
                possiblyQueued = e.code() == 409 && !isTiltWarning

                if (isTiltWarning) {
                    _state.update {
                        it.copy(
                            isSearching = false,
                            message = "",
                            error = null,
                            tiltWarning = TiltWarning(
                                tilted = true
                            )
                        )
                    }
                } else {
                    fail(
                        when (e.code()) {
                            401 -> "Please log in again."
                            403 -> "Online matchmaking requires a registered account."
                            409 -> "You are already in the queue."
                            else -> "Could not start matchmaking (${e.code()})."
                        }
                    )
                }
            } catch (e: Exception) {
                fail(e.userMessage("Could not start matchmaking."))
            }
        }
    }

    private fun startPolling() {
        statusJob?.cancel()
        statusJob = viewModelScope.launch {
            while (true) {
                delay(1500L)

                try {
                    val response = service.getStatus()
                    when (response.state) {
                        "searching" -> {
                            _state.update {
                                it.copy(
                                    isSearching = true,
                                    message = "Finding opponent...",
                                    error = null
                                )
                            }
                        }
                        "matched" -> {
                            val gameId = response.gameId
                            if (gameId.isNullOrBlank()) {
                                possiblyQueued = false
                                fail("Matched response did not contain a game ID.")
                            } else {
                                matched(gameId)
                            }
                            return@launch
                        }
                        "idle" -> {
                            possiblyQueued = false
                            fail(
                                "Matchmaking expired. Please try again."
                            )
                            return@launch
                        }

                        else -> {
                            fail("Unexpected matchmaking status.")
                            return@launch
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    _state.update {
                        it.copy(
                            message = "Reconnecting to matchmaking..."
                        )
                    }
                }
            }
        }
    }

    fun retryAfterTiltWarning(
        minutes: Int,
        incrementSeconds: Int
    ) {
        _state.update { it.copy(tiltWarning = null) }

        startMatch(
            minutes = minutes,
            incrementSeconds = incrementSeconds,
            rated = true,
            ignoreTilt = true
        )
    }

    fun dismissTiltWarning() {
        _state.update { it.copy(tiltWarning = null) }
    }

    fun cancelMatch() {
        if (!_state.value.isSearching || cancelJob?.isActive == true) return
        val pendingSearch = searchJob
        val pendingStatus = statusJob
        searchJob = null
        statusJob = null

        _state.update {
            it.copy(isCancelling = true, message = "Cancelling search...")
        }

        cancelJob = viewModelScope.launch {
            pendingSearch?.cancelAndJoin()
            pendingStatus?.cancelAndJoin()

            try {
                val response = service.cancelQueue()
                if (response.state == "matched" && !response.gameId.isNullOrBlank()) {
                    matched(response.gameId)
                } else {
                    possiblyQueued = false
                    _state.value = MatchmakingUiState()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    resolveAfterMissingQueueEntry()
                } else {
                    fail("Could not cancel search. Check your connection and retry.")
                }
            } catch (e: Exception) {
                fail("Could not cancel search. Check your connection and retry.")
            }
        }
    }

    private suspend fun resolveAfterMissingQueueEntry() {
        try {
            val response = service.getStatus()
            if (response.state == "matched" && !response.gameId.isNullOrBlank()) {
                matched(response.gameId)
            } else {
                possiblyQueued = false
                _state.value = MatchmakingUiState()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            fail("Could not confirm cancellation. Check your connection and retry.")
        }
    }

    private fun matched(gameId: String) {
        possiblyQueued = false
        searchJob?.cancel()
        statusJob?.cancel()

        _state.value = MatchmakingUiState(
            isSearching = false,
            message = "Opponent found!",
            matchedGameId = gameId
        )
    }
    private fun fail(message: String) {
        searchJob?.cancel()
        statusJob?.cancel()

        _state.value = MatchmakingUiState(
            error = message
        )
    }

    fun clearMatchedGame() {
        _state.update { it.copy(matchedGameId = null) }
    }
    override fun onCleared() {
        searchJob?.cancel()
        statusJob?.cancel()
        cancelJob?.cancel()
        if (possiblyQueued) {
            possiblyQueued = false
            cleanupScope.launch {
                withContext(NonCancellable) {
                    runCatching { service.cancelQueue() }
                }
            }
        }
        super.onCleared()
    }

    private companion object {
        val cleanupScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}