package com.abhinav.caisarra.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.repository.AuthRepository
import com.abhinav.caisarra.data.repository.AuthResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.abhinav.caisarra.data.local.PendingOtpStore

enum class VerifyPurpose { RESET_PASSWORD, REGISTRATION }

data class VerifyUiState(
    val otp: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val resendSeconds: Int = 59
) {
    val canSubmit: Boolean
        get() = otp.length == 6 && !isLoading
}

class VerifyViewModel(
    private val repository: AuthRepository,
    private val email: String,
    private val purpose: VerifyPurpose,
    private val pendingOtpStore: PendingOtpStore
) : ViewModel() {

    private val _state = MutableStateFlow(VerifyUiState())
    val state: StateFlow<VerifyUiState> = _state.asStateFlow()

    private var timer: Job? = null

    init {
        startTimer()
    }

    fun onOtpChange(value: String) =
        _state.update { it.copy(otp = value.filter { c -> c.isDigit() }.take(6), error = null) }

    fun verify(onSuccess: () -> Unit) {
        val current = _state.value
        if (!current.canSubmit) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val result = when (purpose) {
                VerifyPurpose.RESET_PASSWORD -> repository.verifyResetCode(email, current.otp)
                VerifyPurpose.REGISTRATION -> repository.verifyRegistration(email, current.otp)
            }
            when (result) {

                is AuthResult.Success -> {
                    pendingOtpStore.clear()
                    _state.update { it.copy(isLoading = false,
                            otp = "",
                            error = null
                        )
                    }
                    onSuccess()
                }
                is AuthResult.Error ->
                    _state.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun resend() {
        if (_state.value.resendSeconds > 0) return
        _state.update { it.copy(otp = "", error = null) }
        startTimer()
        viewModelScope.launch {
            val result = when (purpose) {
                VerifyPurpose.RESET_PASSWORD -> repository.sendResetCode(email)
                VerifyPurpose.REGISTRATION -> repository.resendRegistrationCode()
            }
            if (result is AuthResult.Error) {
                _state.update { it.copy(error = result.message) }
            }
        }
    }

    private fun startTimer() {
        timer?.cancel()
        timer = viewModelScope.launch {
            for (seconds in 59 downTo 0) {
                _state.update { it.copy(resendSeconds = seconds) }
                delay(1000)
            }
        }
    }
}