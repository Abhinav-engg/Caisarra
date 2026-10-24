package com.abhinav.caisarra.presentation.directmessage

import android.app.Application

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

import com.abhinav.caisarra.data.remote.dto.DirectMessageDto
import com.abhinav.caisarra.data.repository.DirectMessageRepository

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DirectMessageUiState(
    val userId: Long = 0L,
    val username: String = "Player",
    val messages: List<DirectMessageDto> = emptyList(),
    val draft: String = "",
    val loading: Boolean = true,
    val sending: Boolean = false,
    val error: String? = null
)

class DirectMessageViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository =
        DirectMessageRepository(application)

    private val _state =
        MutableStateFlow(DirectMessageUiState())

    val state: StateFlow<DirectMessageUiState> =
        _state.asStateFlow()

    private var pollingJob: Job? = null

    fun startChat(
        userId: Long,
        username: String
    ) {
        if (userId <= 0L) {
            _state.value = _state.value.copy(
                loading = false,
                error = "The player's user ID is missing."
            )
            return
        }

        if (
            _state.value.userId == userId &&
            pollingJob != null
        ) {
            return
        }

        pollingJob?.cancel()

        _state.value = DirectMessageUiState(
            userId = userId,
            username = username,
            loading = true
        )

        refresh()

        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(5_000)
                loadHistory(showLoading = false)
            }
        }
    }

    fun onDraftChange(value: String) {
        _state.value = _state.value.copy(
            draft = value.take(500),
            error = null
        )
    }

    fun refresh() {
        viewModelScope.launch {
            loadHistory(showLoading = true)
        }
    }

    private suspend fun loadHistory(
        showLoading: Boolean
    ) {
        val userId = _state.value.userId

        if (userId <= 0L) return

        if (showLoading) {
            _state.value = _state.value.copy(
                loading = true,
                error = null
            )
        }

        try {
            val messages = repository.getMessages(userId)

            _state.value = _state.value.copy(
                messages = messages.distinctBy { it.id }
                    .sortedBy { it.id },
                loading = false,
                error = null
            )
        } catch (exception: Exception) {
            _state.value = _state.value.copy(
                loading = false,
                error = exception.message
                    ?: "Couldn't load messages."
            )
        }
    }

    fun send() {
        val current = _state.value
        val message = current.draft.trim()

        if (message.isEmpty()) return
        if (current.sending) return
        if (current.userId <= 0L) return

        if (message.length > 500) {
            _state.value = current.copy(
                error = "Messages can contain up to 500 characters."
            )
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(
                sending = true,
                error = null
            )

            try {
                val sentMessage = repository.sendMessage(
                    userId = current.userId,
                    message = message
                )

                _state.value = _state.value.copy(
                    messages = (
                            _state.value.messages + sentMessage
                            ).distinctBy { it.id }
                        .sortedBy { it.id },
                    draft = "",
                    sending = false,
                    error = null
                )
            } catch (exception: Exception) {
                _state.value = _state.value.copy(
                    sending = false,
                    error = exception.message
                        ?: "Message could not be sent."
                )
            }
        }
    }

    override fun onCleared() {
        pollingJob?.cancel()
        super.onCleared()
    }
}