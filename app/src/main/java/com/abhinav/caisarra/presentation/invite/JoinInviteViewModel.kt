package com.abhinav.caisarra.presentation.invite

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.local.TokenExpiryChecker
import com.abhinav.caisarra.data.local.TokenManager
import com.abhinav.caisarra.data.repository.AuthRepository
import com.abhinav.caisarra.data.repository.InviteRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

sealed interface JoinInviteState {
    data object Loading : JoinInviteState
    data class Error(val message: String) : JoinInviteState
    data class Preview(
        val inviterName: String,
        val timeControlMinutes: Int,
        val incrementSeconds: Int,
        val yourColor: String,
        val needsLogin: Boolean,
        val joining: Boolean = false
    ) : JoinInviteState
}

class JoinInviteViewModel(
    application: Application,
    private val code: String
) : AndroidViewModel(application) {

    private val inviteRepository = InviteRepository.get(application)
    private val authRepository = AuthRepository.get(application)
    private val tokenManager = TokenManager(application)

    private val _state = MutableStateFlow<JoinInviteState>(JoinInviteState.Loading)
    val state: StateFlow<JoinInviteState> = _state.asStateFlow()

    private val _gameReady = Channel<String>(Channel.BUFFERED)
    val gameReady: Flow<String> = _gameReady.receiveAsFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = JoinInviteState.Loading
            inviteRepository.previewInvite(code)
                .onSuccess { preview ->
                    val loggedIn = authRepository.isLoggedIn()
                    val myId = if (loggedIn) {
                        tokenManager.getAccessToken()?.let { TokenExpiryChecker.getUserId(it) }
                    } else null

                    if (myId != null && myId == preview.inviter.id) {
                        _gameReady.send(preview.gameId)
                        return@onSuccess
                    }

                    _state.value = JoinInviteState.Preview(
                        inviterName = preview.inviter.username,
                        timeControlMinutes = preview.timeControlMinutes,
                        incrementSeconds = preview.incrementSeconds,
                        yourColor = when (preview.color) {
                            "white" -> "Black"
                            "black" -> "White"
                            else -> "Random"
                        },
                        needsLogin = !loggedIn
                    )
                }
                .onFailure { _state.value = JoinInviteState.Error(it.message.orEmpty()) }
        }
    }

    fun join() {
        val current = _state.value as? JoinInviteState.Preview ?: return
        if (current.joining || current.needsLogin) return
        viewModelScope.launch {
            _state.value = current.copy(joining = true)
            inviteRepository.joinInvite(code)
                .onSuccess { _gameReady.send(it.gameId) }
                .onFailure { _state.value = JoinInviteState.Error(it.message.orEmpty()) }
        }
    }
}