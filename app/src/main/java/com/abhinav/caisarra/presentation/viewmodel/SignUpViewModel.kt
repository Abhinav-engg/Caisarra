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
data class SignUpUiState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val usernameError: String?
        get() = if (username.isBlank()) null else Validators.usernameError(username)

    val emailError: String?
        get() = if (email.isBlank()) null else Validators.emailError(email)

    val passwordError: String?
        get() = if (password.isBlank()) null else Validators.passwordError(password)

    val canSubmit: Boolean
        get() = username.isNotBlank() && usernameError == null &&
                email.isNotBlank() && emailError == null &&
                password.isNotBlank() && passwordError == null &&
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

    fun signUp(onSuccess: (String) -> Unit) {
        val current = _state.value
        if (!current.canSubmit) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = repository.register(
                current.username.trim(),
                current.email.trim(),
                current.password,
            )
            when (result) {
                is AuthResult.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    onSuccess(current.email.trim())
                }
                is AuthResult.Error ->
                    _state.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }
}