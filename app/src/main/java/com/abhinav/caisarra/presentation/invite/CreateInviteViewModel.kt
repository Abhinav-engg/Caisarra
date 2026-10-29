package com.abhinav.caisarra.presentation.invite

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.repository.InviteRepository
import com.abhinav.caisarra.presentation.game.viewmodel.GameMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateInviteUiState(
    val timeControlMinutes: Int = 10,
    val incrementSeconds: Int = 0,
    val selectedControl: String = "10 min",
    val mode: GameMode = GameMode.RAPID,
    val color: String = "random",
    val allowTakebacks: Boolean = false,
    val inviteCode: String? = null,
    val gameId: String? = null,
    val link: String? = null,
    val opponentId: Long? = null,
    val opponentUsername: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class CreateInviteViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = InviteRepository.get(application)

    private val _uiState = MutableStateFlow(CreateInviteUiState())
    val uiState: StateFlow<CreateInviteUiState> = _uiState.asStateFlow()

    fun onTimeControlChange(minutes: Int) {
        _uiState.update { current ->
            current.copy(
                timeControlMinutes = minutes,
                selectedControl = timeLabel(minutes.toFloat(), current.incrementSeconds.toFloat()),
                mode = modeFor(minutes.toFloat())
            )
        }
    }

    fun onIncrementChange(seconds: Int) {
        _uiState.update { current ->
            current.copy(
                incrementSeconds = seconds,
                selectedControl = timeLabel(current.timeControlMinutes.toFloat(), seconds.toFloat())
            )
        }
    }

    fun onControlSelected(label: String, minutes: Float, incrementSeconds: Float, mode: GameMode) {
        _uiState.update {
            it.copy(
                timeControlMinutes = minutes.toInt(),
                incrementSeconds = incrementSeconds.toInt(),
                selectedControl = label,
                mode = mode
            )
        }
    }

    fun onTimeChanged(minutes: Float) {
        val totalMinutes = minutes.toInt()
        _uiState.update { current ->
            current.copy(
                timeControlMinutes = totalMinutes,
                selectedControl = timeLabel(minutes, current.incrementSeconds.toFloat()),
                mode = modeFor(minutes)
            )
        }
    }

    fun onBonusChanged(seconds: Float) {
        val increment = seconds.toInt()
        _uiState.update { current ->
            current.copy(
                incrementSeconds = increment,
                selectedControl = timeLabel(current.timeControlMinutes.toFloat(), seconds)
            )
        }
    }

    private fun modeFor(minutes: Float): GameMode = when {
        minutes >= 1440f -> GameMode.DAILY
        minutes < 3f -> GameMode.BULLET
        minutes < 10f -> GameMode.BLITZ
        else -> GameMode.RAPID
    }

    private fun timeLabel(
        minutes: Float,
        incrementSeconds: Float
    ): String {
        val totalMinutes = minutes.toInt()
        val increment = incrementSeconds.toInt()

        val duration = when (totalMinutes) {
            1440 -> "1 day"
            2880 -> "2 days"
            4320 -> "3 days"
            7200 -> "5 days"
            10080 -> "7 days"
            20160 -> "14 days"
            else -> "$totalMinutes min"
        }

        return if (increment == 0) {
            duration
        } else {
            "$duration + $increment"
        }
    }

    fun onColorChange(color: String) {
        _uiState.update { it.copy(color = color) }
    }

    fun onTakebacksChanged(allowed: Boolean) {
        _uiState.update { it.copy(allowTakebacks = allowed) }
    }

    fun setOpponent(id: Long?, username: String?) {
        _uiState.update { it.copy(opponentId = id, opponentUsername = username) }
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
            repository.createInvite(
                timeControlMinutes = state.timeControlMinutes,
                incrementSeconds = state.incrementSeconds,
                color = state.color,
                rated = !state.allowTakebacks,
                opponentId = state.opponentId
            )
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
