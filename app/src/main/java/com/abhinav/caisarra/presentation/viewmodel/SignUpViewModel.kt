package com.abhinav.caisarra.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.local.RegistrationDataStore
import com.abhinav.caisarra.data.repository.AuthRepository
import com.abhinav.caisarra.data.repository.AuthResult
import com.abhinav.caisarra.domain.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
data class SignUpUiState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
) {

    val usernameError: String?
        get() = if (username.isBlank()) null else Validators.usernameError(username)

    val emailError: String?
        get() = if (email.isBlank()) null else Validators.emailError(email)

    val passwordError: String?
        get() = if (password.isBlank()) null else Validators.passwordError(password)
    val passwordRequirementsValid: Boolean
        get() =
            password.length in 8..16 && password.any { it.isDigit() } &&
                    password.any { it.isUpperCase() } && password.any { it.isLowerCase() } &&
                    password.any { !it.isLetterOrDigit() && !it.isWhitespace()} && !password.any { it.isWhitespace() }
    val confirmPasswordError: String?
        get() = if (confirmPassword.isBlank()) { null
        } else if (password != confirmPassword) {
            "Passwords do not match"
        } else {
            null
        }

    val canSubmit: Boolean
        get() =
            username.isNotBlank() &&
                    usernameError == null &&
                    email.isNotBlank() &&
                    emailError == null &&
                    password.isNotBlank() &&
                    passwordRequirementsValid &&
                    confirmPassword.isNotBlank() &&
                    confirmPasswordError == null &&
                    !isLoading
}


class SignUpViewModel(private val repository: AuthRepository,private val registrationDataStore: RegistrationDataStore
) : ViewModel() {

    private val _state = MutableStateFlow(SignUpUiState())
    val state: StateFlow<SignUpUiState> = _state.asStateFlow()

    init {
        loadSavedRegistrationData()
    }

    private fun loadSavedRegistrationData() {
        viewModelScope.launch {

            val username = registrationDataStore.username.first()
            val email = registrationDataStore.email.first()

            _state.update {
                it.copy(username = username,
                    email = email
                )
            }
        }
    }

    fun onUsernameChange(value: String) {
        _state.update {
            it.copy(
                username = value,
                error = null
            )
        }

        viewModelScope.launch {
            registrationDataStore.saveUsername(value)
        }
    }

    fun onEmailChange(value: String) {
        _state.update {
            it.copy(
                email = value,
                error = null
            )
        }

        viewModelScope.launch {
            registrationDataStore.saveEmail(value)
        }
    }

    fun onPasswordChange(value: String) =
        _state.update { it.copy(password = value, error = null) }

    fun onConfirmPasswordChange(value: String) =
        _state.update { it.copy(confirmPassword = value, error = null) }

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
                    registrationDataStore.clear()
                    _state.update {it.copy(
                            isLoading = false
                        )
                    }
                    onSuccess(current.email.trim())
                }
                is AuthResult.Error ->
                    _state.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }
}