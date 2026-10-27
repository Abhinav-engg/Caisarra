package com.abhinav.caisarra.presentation.watch

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.local.TokenManager
import com.abhinav.caisarra.data.remote.RetrofitInstance
import com.abhinav.caisarra.data.remote.dto.ActiveGameDto
import com.abhinav.caisarra.data.repository.RemoteGameRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class WatchViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val gameRepository = RemoteGameRepository(
        RetrofitInstance.createGamesService(TokenManager.get(application))
    )

    private val _rawGames = MutableStateFlow<List<ActiveGameDto>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _codeQuery = MutableStateFlow("")
    val codeQuery: StateFlow<String> = _codeQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _navigateToWatchCode = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val navigateToWatchCode: SharedFlow<String> = _navigateToWatchCode

    private var pollJob: Job? = null

    val filteredGames: StateFlow<List<ActiveGameDto>> =
        combine(_rawGames, _searchQuery) { games, query ->
            val trimmed = query.trim()
            if (trimmed.isEmpty()) {
                games
            } else {
                games.filter { game ->
                    (game.whiteUsername?.contains(trimmed, ignoreCase = true) == true) ||
                    (game.blackUsername?.contains(trimmed, ignoreCase = true) == true) ||
                    (game.effectiveWatchCode?.contains(trimmed, ignoreCase = true) == true)
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadActiveGames(showLoader = true)
        startPeriodicRefresh()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCodeQueryChange(code: String) {
        _codeQuery.value = code
    }

    fun refresh() {
        loadActiveGames(showLoader = true)
    }

    private fun loadActiveGames(showLoader: Boolean) {
        viewModelScope.launch {
            if (showLoader) _isLoading.value = true
            _errorMessage.value = null

            val result = gameRepository.getActiveGames()
            result.onSuccess { games ->
                _rawGames.value = games.filter { it.status.equals("active", ignoreCase = true) }
                _isLoading.value = false
            }.onFailure { error ->
                _isLoading.value = false
                if (_rawGames.value.isEmpty()) {
                    _errorMessage.value = error.localizedMessage ?: "Could not fetch active games."
                }
            }
        }
    }

    private fun startPeriodicRefresh() {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (isActive) {
                delay(12000)
                loadActiveGames(showLoader = false)
            }
        }
    }

    fun submitCodeOrLink() {
        val input = _codeQuery.value.trim()
        if (input.isEmpty()) return

        val extractedCode = extractWatchCode(input)
        if (extractedCode.isNotEmpty()) {
            _navigateToWatchCode.tryEmit(extractedCode)
        } else {
            _errorMessage.value = "Invalid watch code or link format."
        }
    }

    fun selectGame(game: ActiveGameDto) {
        val code = game.effectiveWatchCode
        if (!code.isNullOrBlank()) {
            _navigateToWatchCode.tryEmit(code)
            return
        }

        val gameId = game.effectiveGameId
        if (gameId.isNotBlank()) {
            viewModelScope.launch {
                _isLoading.value = true
                val inviteResult = gameRepository.createWatchInvite(gameId)
                _isLoading.value = false
                inviteResult.onSuccess { resp ->
                    if (resp.watchCode.isNotBlank()) {
                        _navigateToWatchCode.tryEmit(resp.watchCode)
                    } else {
                        _errorMessage.value = "Could not obtain watch code for this game."
                    }
                }.onFailure {
                    _errorMessage.value = "This game does not have an active public watch code."
                }
            }
        }
    }

    private fun extractWatchCode(input: String): String {
        val trimmed = input.trim()
        if (trimmed.contains("/")) {
            return trimmed.substringAfterLast("/").trim()
        }
        return trimmed
    }

    override fun onCleared() {
        super.onCleared()
        pollJob?.cancel()
    }
}
