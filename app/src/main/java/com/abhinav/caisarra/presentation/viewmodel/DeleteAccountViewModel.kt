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

/**
 * UI State representation for the Delete Account screen.
 *
 * @property password Current password input value typed by the user to confirm deletion.
 * @property isLoading Indicates whether the deletion API call is currently in flight.
 * @property error Error message string displayed to the user if deletion fails or validation fails.
 * @property isSuccess Indicates whether deletion succeeded.
 */
data class DeleteAccountUiState(
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
) {
    /**
     * Determines whether the "DELETE MY ACCOUNT" button can be tapped.
     * Enforces non-empty password and maximum 72 characters as required by the backend.
     */
    val canSubmit: Boolean
        get() = password.isNotBlank() && password.length <= 72 && !isLoading
}

/**
 * ViewModel managing the business logic and API interaction for deleting a user account.
 *
 * Implements client requirements from the Delete Account API specification:
 * - Validates password input (required, max 72 characters).
 * - Calls DELETE /api/account via [AuthRepository].
 * - On 403 (wrong password): updates state error and keeps user on the screen.
 * - On 429: displays rate limit notice.
 * - On 200: invokes the onSuccess callback to navigate to the login screen.
 */
class DeleteAccountViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DeleteAccountUiState())
    val state: StateFlow<DeleteAccountUiState> = _state.asStateFlow()

    /**
     * Updates the password state as user types, clearing any existing validation or API error.
     */
    fun onPasswordChange(newPassword: String) {
        _state.update { currentState ->
            currentState.copy(
                password = newPassword,
                error = null
            )
        }
    }

    /**
     * Clears any active error message from the state.
     */
    fun clearError() {
        _state.update { currentState ->
            currentState.copy(error = null)
        }
    }

    /**
     * Submits the account deletion request with the user's password.
     *
     * @param onSuccess Callback executed once deletion is successfully confirmed (HTTP 200).
     */
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
