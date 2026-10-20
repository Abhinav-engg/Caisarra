package com.abhinav.caisarra.presentation.tournament

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.remote.dto.TournamentDto
import com.abhinav.caisarra.data.repository.TournamentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TournamentListUiState(
    val loading: Boolean = true,
    val tournaments: List<TournamentDto> = emptyList(),
    val error: String? = null,
    val code: String = "",
    val lookingUp: Boolean = false
)

class TournamentListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TournamentRepository.get(application)

    private val _state = MutableStateFlow(TournamentListUiState())
    val state = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            repository.listTournaments()
                .onSuccess { list ->
                    _state.update { it.copy(loading = false, tournaments = list) }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(loading = false, error = e.message ?: "Could not load tournaments")
                    }
                }
        }
    }

    fun onCodeChange(value: String) {
        _state.update { it.copy(code = value.trim().uppercase().take(6), error = null) }
    }

    fun lookupCode(onFound: (TournamentDto, String) -> Unit) {
        val code = _state.value.code
        if (code.length != 6) {
            _state.update { it.copy(error = "Enter the 6-character invite code") }
            return
        }
        if (_state.value.lookingUp) return
        viewModelScope.launch {
            _state.update { it.copy(lookingUp = true, error = null) }
            repository.getTournamentByInvite(code)
                .onSuccess { tournament ->
                    _state.update { it.copy(lookingUp = false) }
                    onFound(tournament, code)
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(lookingUp = false, error = e.message ?: "Invalid invite code")
                    }
                }
        }
    }
}