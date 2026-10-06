package com.abhinav.caisarra.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(

    val username: String = "",
    val isLoggingOut: Boolean = false,
    val error: String? = null
)


class HomeViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _state =
        MutableStateFlow(
            HomeUiState()
        )

    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        loadUsername()
    }

    private fun loadUsername() {

        viewModelScope.launch {

            val username = repository.getUsername()

            _state.update {
                it.copy(username = username.orEmpty())
            }
        }
    }
    fun logout(
        onSuccess: () -> Unit
    ) {

        if (_state.value.isLoggingOut) {
            return
        }

        _state.update {
            it.copy(
                isLoggingOut = true,
                error = null
            )
        }

        viewModelScope.launch {
            try {
                repository.logout()
                _state.update {
                    it.copy(isLoggingOut = false)
                }
                onSuccess()
            } catch (e: Exception) {
                _state.update {
                    it.copy(isLoggingOut = false,
                        error = e.message ?: "Unable to log out"
                    )
                }
            }
        }
    }


    fun clearError() {
        _state.update {
            it.copy(
                error = null
            )
        }
    }
}