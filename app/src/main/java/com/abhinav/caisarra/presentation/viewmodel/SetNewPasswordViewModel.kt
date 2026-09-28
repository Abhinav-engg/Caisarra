package com.abhinav.caisarra.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.repository.AuthRepository
import com.abhinav.caisarra.data.repository.AuthResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SetNewPasswordUiState(
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val updated: Boolean = false,
    val error: String? = null
) {
    val passwordsMismatch: Boolean
        get() = confirmPassword.isNotEmpty() && newPassword != confirmPassword

    val canSubmit: Boolean
        get() = newPassword.length >= 8 &&
                newPassword == confirmPassword &&
                !isLoading &&
                !updated
}

class SetNewPasswordViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(SetNewPasswordUiState())
    val state: StateFlow<SetNewPasswordUiState> = _state.asStateFlow()

    fun onNewPasswordChange(value: String) =
        _state.update { it.copy(newPassword = value, error = null) }

    fun onConfirmPasswordChange(value: String) =
        _state.update { it.copy(confirmPassword = value, error = null) }

    fun resetPassword(onSuccess: () -> Unit) {
        val current = _state.value
        if (!current.canSubmit) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = repository.resetPassword(current.newPassword, current.confirmPassword)) {
                is AuthResult.Success -> {
                    _state.update { it.copy(isLoading = false, updated = true) }
                    delay(1200)
                    onSuccess()
                }
                is AuthResult.Error ->
                    _state.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }
}