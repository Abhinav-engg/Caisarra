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

data class SignUpUiState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val canSubmit: Boolean
        get() = username.isNotBlank() &&
                email.contains("@") &&
                password.length >= 8 &&
                !isLoading
}

class SignUpViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(SignUpUiState())
    val state: StateFlow<SignUpUiState> = _state.asStateFlow()

    fun onUsernameChange(value: String) =
        _state.update { it.copy(username = value, error = null) }

    fun onEmailChange(value: String) =
        _state.update { it.copy(email = value, error = null) }

    fun onPasswordChange(value: String) =
        _state.update { it.copy(password = value, error = null) }

    fun signUp(onSuccess: () -> Unit) {
        val current = _state.value
        if (!current.canSubmit) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = repository.register(
                current.username.trim(),
                current.email.trim(),
                current.password
            )
            when (result) {
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