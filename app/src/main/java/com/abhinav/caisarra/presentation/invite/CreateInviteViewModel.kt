package com.abhinav.caisarra.presentation.invite

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.repository.InviteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateInviteUiState(
    val timeControlMinutes: Int = 10,
    val incrementSeconds: Int = 0,
    val color: String = "random",
    val inviteCode: String? = null,
    val gameId: String? = null,
    val link: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class CreateInviteViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = InviteRepository.get(application)

    private val _uiState = MutableStateFlow(CreateInviteUiState())
    val uiState: StateFlow<CreateInviteUiState> = _uiState.asStateFlow()

    fun onTimeControlChange(minutes: Int) {
        _uiState.update { it.copy(timeControlMinutes = minutes) }
    }

    fun onIncrementChange(seconds: Int) {
        _uiState.update { it.copy(incrementSeconds = seconds) }
    }

    fun onColorChange(color: String) {
        _uiState.update { it.copy(color = color) }
    }

    fun createInvite() {
        val state = _uiState.value
        if (state.isLoading) return
        if (state.timeControlMinutes < 1) {
            _uiState.update { it.copy(error = "Time must be at least 1 minute") }
            return
        }
        if (state.incrementSeconds < 0) {
            _uiState.update { it.copy(error = "Increment can't be negative") }
            return
        }
        if (state.color !in listOf("white", "black", "random")) {
            _uiState.update { it.copy(error = "Choose a color") }
            return
        }
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            repository.createInvite(state.timeControlMinutes, state.incrementSeconds, state.color)
                .onSuccess { response ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            inviteCode = response.inviteCode,
                            gameId = response.gameId,
                            link = response.link
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}