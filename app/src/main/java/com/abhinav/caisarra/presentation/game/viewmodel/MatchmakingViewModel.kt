package com.abhinav.caisarra.presentation.game.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.remote.api.MatchmakingJoinRequest
import com.abhinav.caisarra.data.remote.api.MatchmakingService
import com.abhinav.caisarra.data.remote.api.TiltWarning
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class MatchmakingUiState(
    val isSearching: Boolean = false,
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

    fun startMatch(
        minutes: Int,
        incrementSeconds: Int,
        rated: Boolean = true,
        ignoreTilt: Boolean = false
    ) {
        if (_state.value.isSearching) return
        searchJob?.cancel()
        statusJob?.cancel()
        searchJob = viewModelScope.launch {
            _state.value = MatchmakingUiState(isSearching = true,
                message = "Joining matchmaking queue..."
            )
            try {
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

                if (e.code() == 409 &&errorBody?.contains("tilt_warning") == true
                ) {
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
                fail(e.message ?: "Could not start matchmaking.")
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
                                fail("Matched response did not contain a game ID.")
                            } else {
                                matched(gameId)
                            }
                            return@launch
                        }
                        "idle" -> {
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
        if (!_state.value.isSearching) return
        searchJob?.cancel()
        searchJob = null
        statusJob?.cancel()
        statusJob = null

        viewModelScope.launch {
            _state.update {
                it.copy(message = "Cancelling search...")
            }

            try {
                service.cancelQueue()
                _state.value = MatchmakingUiState()
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    try {
                        val response = service.getStatus()

                        if (
                            response.state == "matched" &&
                            !response.gameId.isNullOrBlank()
                        ) {
                            matched(response.gameId)
                            return@launch
                        }
                    } catch (_: Exception) {

                    }
                }

                fail("Could not cancel search. Check your connection and retry.")
            } catch (e: Exception) {
                fail("Could not cancel search. Check your connection and retry.")
            }
        }
    }

    private fun matched(gameId: String) {
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
        super.onCleared()
    }
}