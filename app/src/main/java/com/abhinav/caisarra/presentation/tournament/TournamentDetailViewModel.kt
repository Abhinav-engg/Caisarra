package com.abhinav.caisarra.presentation.tournament

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.local.TokenExpiryChecker
import com.abhinav.caisarra.data.local.TokenManager
import com.abhinav.caisarra.data.remote.TournamentSocketClient
import com.abhinav.caisarra.data.remote.dto.RoundDto
import com.abhinav.caisarra.data.remote.dto.RoundsResponse
import com.abhinav.caisarra.data.remote.dto.StandingDto
import com.abhinav.caisarra.data.remote.dto.StandingsResponse
import com.abhinav.caisarra.data.remote.dto.TournamentDto
import com.abhinav.caisarra.data.remote.dto.TournamentGamesResponse
import com.abhinav.caisarra.data.remote.dto.TournamentSocketEvent
import com.abhinav.caisarra.data.repository.TournamentRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

data class TournamentDetailUiState(
    val loading: Boolean = true,
    val tournament: TournamentDto? = null,
    val standings: List<StandingDto> = emptyList(),
    val rounds: List<RoundDto> = emptyList(),
    val myUserId: Long? = null,
    val hasJoined: Boolean = false,
    val actionInProgress: Boolean = false,
    val winnerId: Long? = null,
    val message: String? = null,
    val error: String? = null,
    val isTournamentCancelled: Boolean = false
)

class TournamentDetailViewModel(
    application: Application,
    private val tournamentId: String,
    private val inviteCode: String?
) : AndroidViewModel(application) {

    private val repository = TournamentRepository.get(application)
    private val socket = TournamentSocketClient(application)

    private val _state = MutableStateFlow(TournamentDetailUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val token = TokenManager.get(application).getAccessToken()
            val userId = token?.let { TokenExpiryChecker.getUserId(it) }
            _state.update { it.copy(myUserId = userId) }
            refreshAll(showLoading = true)
        }
        observeSocketEvents()
        keepSocketAlive()
    }

    fun refresh() {
        viewModelScope.launch { refreshAll(showLoading = true) }
    }

    fun clearMessage() = _state.update { it.copy(message = null) }

    fun join() {
        val code = inviteCode
        perform("You joined the tournament", joined = true) {
            if (code != null) repository.joinByInvite(code)
            else repository.joinTournament(tournamentId)
        }
    }

    fun leave() = perform("You left the tournament", joined = false) {
        repository.leaveTournament(tournamentId)
    }

    fun start() = perform("Tournament started") {
        repository.startTournament(tournamentId)
    }

    fun end() = perform("Tournament cancelled") {
        repository.endTournament(tournamentId)
    }

    private fun perform(
        successMessage: String,
        joined: Boolean? = null,
        block: suspend () -> Result<*>
    ) {
        if (_state.value.actionInProgress) return
        viewModelScope.launch {
            _state.update { it.copy(actionInProgress = true) }
            block()
                .onSuccess {
                    _state.update {
                        it.copy(message = successMessage, hasJoined = joined ?: it.hasJoined)
                    }
                    refreshAll(showLoading = false)
                }
                .onFailure { e ->
                    _state.update { it.copy(message = e.message ?: "Something went wrong") }
                }
            _state.update { it.copy(actionInProgress = false) }
        }
    }

    private suspend fun refreshAll(showLoading: Boolean) {
        if (showLoading) _state.update { it.copy(loading = true, error = null) }
        coroutineScope {
            val tournament = async { repository.getTournament(tournamentId) }
            val standings = async { repository.getStandings(tournamentId) }
            val rounds = async { repository.getRounds(tournamentId) }
            val myGames = async { repository.getMyGames(tournamentId) }
            applyRefresh(tournament.await(), standings.await(), rounds.await(), myGames.await())
        }
    }

    private fun applyRefresh(
        tournamentResult: Result<TournamentDto>,
        standingsResult: Result<StandingsResponse>,
        roundsResult: Result<RoundsResponse>,
        myGamesResult: Result<TournamentGamesResponse>
    ) {
        _state.update { cur ->
            val standings = standingsResult.getOrNull()?.standings ?: cur.standings
            val rounds = roundsResult.getOrNull()?.rounds ?: cur.rounds
            val tournament = tournamentResult.getOrNull() ?: cur.tournament
            val joined = cur.hasJoined ||
                    (cur.myUserId != null && standings.any { it.playerId == cur.myUserId }) ||
                    (myGamesResult.getOrNull()?.games?.isNotEmpty() == true)
            cur.copy(
                loading = false,
                tournament = tournament,
                standings = standings,
                rounds = rounds,
                hasJoined = joined,
                error = if (tournament == null) {
                    tournamentResult.exceptionOrNull()?.message ?: "Could not load tournament"
                } else null
            )
        }
    }

    private fun observeSocketEvents() {
        viewModelScope.launch {
            socket.incoming.collect { event ->
                when (event) {
                    is TournamentSocketEvent.RoundStarted -> {
                        _state.update { it.copy(message = "Round ${event.round} has started") }
                        refreshAll(showLoading = false)
                    }

                    TournamentSocketEvent.StandingsUpdated -> refreshAll(showLoading = false)

                    is TournamentSocketEvent.Completed -> {
                        _state.update { it.copy(winnerId = event.winnerId) }
                        refreshAll(showLoading = false)
                    }

                    is TournamentSocketEvent.Cancelled -> {
                        _state.update {
                            it.copy(
                                winnerId = event.winnerId,
                                isTournamentCancelled = true,
                                message = "Tournament cancelled"
                            )
                        }
                    }
                }
            }
        }
    }

    private fun keepSocketAlive() {
        viewModelScope.launch {
            while (isActive) {
                val status = _state.value.tournament?.status
                if (status == "completed" || status == "cancelled") break
                if (!socket.connect(tournamentId)) {
                    delay(5_000)
                    continue
                }
                withTimeoutOrNull(10_000) { socket.connected.first { it } }
                socket.connected.first { !it }
                delay(3_000)
            }
        }
    }

    override fun onCleared() {
        socket.close()
        super.onCleared()
    }
}