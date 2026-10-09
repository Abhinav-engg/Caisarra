package com.abhinav.caisarra.presentation.invite

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.remote.dto.InvitePreviewResponse
import com.abhinav.caisarra.data.repository.InviteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class JoinInviteUiState(
    val code: String = "",
    val preview: InvitePreviewResponse? = null,
    val gameId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class JoinInviteViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = InviteRepository.get(application)

    private val _uiState = MutableStateFlow(JoinInviteUiState())
    val uiState: StateFlow<JoinInviteUiState> = _uiState.asStateFlow()

    fun onCodeChange(value: String) {
        _uiState.update {
            it.copy(code = value.trim().lowercase().take(8), preview = null, error = null)
        }
    }

    fun previewInvite() {
        val state = _uiState.value
        if (state.isLoading) return
        if (!state.code.matches(Regex("[0-9a-f]{8}"))) {
            _uiState.update { it.copy(error = "Enter a valid 8-character code") }
            return
        }
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            repository.previewInvite(state.code)
                .onSuccess { preview ->
                    _uiState.update { it.copy(isLoading = false, preview = preview) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    fun joinGame() {
        val state = _uiState.value
        if (state.isLoading || state.preview == null) return
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            repository.joinInvite(state.code)
                .onSuccess { response ->
                    _uiState.update { it.copy(isLoading = false, gameId = response.gameId) }
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