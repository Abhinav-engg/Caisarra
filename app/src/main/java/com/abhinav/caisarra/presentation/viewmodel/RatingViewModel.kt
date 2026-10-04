package com.abhinav.caisarra.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.repository.AuthRepository
import com.abhinav.caisarra.data.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ExperienceLevel(val label: String) {
    BEGINNER("Beginner"),
    COMPETENT("Competent"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced");

    val key: String get() = name.lowercase()
}

data class RatingUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

class RatingViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(RatingUiState())
    val state: StateFlow<RatingUiState> = _state.asStateFlow()

    fun submit(level: ExperienceLevel, onSuccess: () -> Unit) {
        if (_state.value.isLoading) return
        _state.update { RatingUiState(isLoading = true) }
        viewModelScope.launch {
            when (val result = repository.submitRating(level.key)) {
                is AuthResult.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                is AuthResult.Error ->
                    _state.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }
}