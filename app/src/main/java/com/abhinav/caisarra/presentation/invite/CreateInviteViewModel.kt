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
    val color: String = "random",
    val code: String? = null,
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

    fun onColorChange(color: String) {
        _uiState.update { it.copy(color = color) }
    }

    fun createInvite() {
        val state = _uiState.value
        if (state.isLoading) return
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            repository.createInvite(state.timeControlMinutes, state.color)
                .onSuccess { response ->
                    _uiState.update { it.copy(isLoading = false, code = response.code) }
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