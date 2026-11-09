
package com.abhinav.caisarra.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.repository.AuthRepository
import com.abhinav.caisarra.data.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminLoginUiState(
    val email: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val error: String? = null
) {
    val canSubmit: Boolean
        get() = email.isNotBlank() &&
                password.isNotBlank() &&
                !loading
}

class AdminLoginViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AdminLoginUiState())
    val state = _state.asStateFlow()

    fun onEmailChange(value: String) {
        _state.update {
            it.copy(email = value, error = null)
        }
    }

    fun onPasswordChange(value: String) {
        _state.update {
            it.copy(password = value, error = null)
        }
    }

    fun login(onSuccess: () -> Unit) {
        val current = _state.value

        if (!current.canSubmit) return

        viewModelScope.launch {
            _state.update {
                it.copy(loading = true, error = null)
            }

            when (
                val result = repository.login(
                    current.email.trim(),
                    current.password
                )
            ) {
                is AuthResult.Success -> {

                    if (repository.isAdmin()) {
                        _state.update {
                            it.copy(loading = false)
                        }
                        onSuccess()
                    } else {
                        repository.logout()

                        _state.update {
                            it.copy(
                                loading = false,
                                error = "Admin access denied. Please use an authorized admin account."
                            )
                        }
                    }
                }

                is AuthResult.Error -> {
                    _state.update {
                        it.copy(
                            loading = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }
}
