package com.abhinav.caisarra.presentation.tournament

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.remote.dto.CreateTournamentRequest
import com.abhinav.caisarra.data.repository.TournamentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateTournamentUiState(
    val name: String = "",
    val description: String = "",
    val format: String = "swiss",
    val timeControl: String = "5+0",
    val minPlayers: String = "",
    val maxPlayers: String = "",
    val visibility: String = "public",
    val submitting: Boolean = false,
    val error: String? = null,
    val createdId: String? = null
)

class CreateTournamentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TournamentRepository.get(application)

    private val _state = MutableStateFlow(CreateTournamentUiState())
    val state = _state.asStateFlow()

    fun onNameChange(value: String) = _state.update { it.copy(name = value.take(60), error = null) }
    fun onDescriptionChange(value: String) =
        _state.update { it.copy(description = value.take(200), error = null) }

    fun onFormatChange(value: String) = _state.update { it.copy(format = value, error = null) }
    fun onTimeControlChange(value: String) =
        _state.update { it.copy(timeControl = value, error = null) }

    fun onMinPlayersChange(value: String) =
        _state.update { it.copy(minPlayers = value.filter(Char::isDigit).take(3), error = null) }

    fun onMaxPlayersChange(value: String) =
        _state.update { it.copy(maxPlayers = value.filter(Char::isDigit).take(3), error = null) }

    fun submit() {
        val s = _state.value
        if (s.submitting) return

        val name = s.name.trim()
        val min = s.minPlayers.toIntOrNull()
        val max = s.maxPlayers.toIntOrNull()
        val error = when {
            name.length < 3 -> "Name must be at least 3 characters"
            min == null || min < 2 -> "Minimum players must be at least 2"
            max == null || max < min -> "Maximum players can't be less than the minimum"
            !TIME_CONTROL.matches(s.timeControl) -> "Pick a time control"
            else -> null
        }
        if (error != null || min == null || max == null) {
            _state.update { it.copy(error = error) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(submitting = true, error = null) }
            repository.createTournament(
                CreateTournamentRequest(
                    name = name,
                    description = s.description.trim(),
                    format = s.format,
                    timeControl = s.timeControl,
                    minPlayers = min,
                    maxPlayers = max,
                    visibility = "public",
                    totalRounds = 0,
                    startAt = null
                )
            ).onSuccess { tournament ->
                _state.update { it.copy(submitting = false, createdId = tournament.id) }
            }.onFailure { e ->
                _state.update {
                    it.copy(submitting = false, error = e.message ?: "Could not create tournament")
                }
            }
        }
    }

    private companion object {
        val TIME_CONTROL = Regex("""\d+\+\d+""")
    }
}
