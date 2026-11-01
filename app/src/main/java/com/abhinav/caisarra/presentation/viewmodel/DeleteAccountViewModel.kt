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

data class DeleteAccountUiState(
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
) {
    val canSubmit: Boolean
        get() = password.isNotBlank() && password.length <= 72 && !isLoading
}

class DeleteAccountViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DeleteAccountUiState())
    val state: StateFlow<DeleteAccountUiState> = _state.asStateFlow()

    fun onPasswordChange(newPassword: String) {
        _state.update { currentState ->
            currentState.copy(
                password = newPassword,
                error = null
            )
        }
    }

    fun clearError() {
        _state.update { currentState ->
            currentState.copy(error = null)
        }
    }

    fun confirmDeleteAccount(onSuccess: () -> Unit) {
        val current = _state.value
        val trimmedPassword = current.password.trim()

        if (trimmedPassword.isEmpty()) {
            _state.update { it.copy(error = "Please enter your password to confirm deletion.") }
            return
        }

        if (trimmedPassword.length > 72) {
            _state.update { it.copy(error = "Password cannot be longer than 72 characters.") }
            return
        }

        _state.update {
            it.copy(
                isLoading = true,
                error = null
            )
        }

        viewModelScope.launch {
            val result = repository.deleteAccount(trimmedPassword)

            when (result) {
                is AuthResult.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true,
                            error = null
                        )
                    }
                    onSuccess()
                }

                is AuthResult.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }
}
