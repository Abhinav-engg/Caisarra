package com.abhinav.caisarra.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.repository.AuthRepository
import com.abhinav.caisarra.data.repository.AuthResult
import com.abhinav.caisarra.domain.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ResetPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val codeSent: Boolean = false,
    val error: String? = null
) {
    val emailError: String?
        get() = if (email.isBlank()) null else Validators.emailError(email)

    val canSubmit: Boolean
        get() = email.isNotBlank() && emailError == null && !isLoading
}

class ResetPasswordViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(ResetPasswordUiState())
    val state: StateFlow<ResetPasswordUiState> = _state.asStateFlow()

    fun onEmailChange(value: String) =
        _state.update { it.copy(email = value, error = null, codeSent = false) }

    fun sendCode(onSuccess: (String) -> Unit) {
        val current = _state.value
        if (!current.canSubmit) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = repository.sendResetCode(current.email.trim())) {
                is AuthResult.Success -> {
                    _state.update { it.copy(isLoading = false, codeSent = true) }
                    onSuccess(current.email.trim())
                }
                is AuthResult.Error ->
                    _state.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }
}