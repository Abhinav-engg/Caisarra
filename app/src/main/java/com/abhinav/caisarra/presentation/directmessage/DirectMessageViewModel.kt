package com.abhinav.caisarra.presentation.directmessage

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.remote.DirectMessageSocket
import com.abhinav.caisarra.data.remote.dto.DirectMessageDto
import com.abhinav.caisarra.data.repository.DirectMessageRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class DirectMessageUiState(
    val userId: Long = 0L,
    val username: String = "Player",
    val messages: List<DirectMessageDto> = emptyList(),
    val draft: String = "",
    val loading: Boolean = true,
    val sending: Boolean = false,
    val connected: Boolean = false,
    val error: String? = null
)

class DirectMessageViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = DirectMessageRepository(application)

    private val directSocket = DirectMessageSocket(application)

    private val _state = MutableStateFlow(DirectMessageUiState())
    val state: StateFlow<DirectMessageUiState> = _state.asStateFlow()

    private var reconnectJob: Job? = null
    private var incomingJob: Job? = null
    private var statusJob: Job? = null
    private var errorJob: Job? = null
    private var historyJob: Job? = null

    private var activeChatUserId: Long = 0L

    fun startChat(
        userId: Long,
        username: String
    ) {
        if (userId <= 0L) {
            _state.value = _state.value.copy(
                loading = false,
                error = "The recipient's user ID is invalid."
            )
            return
        }


        if (
            activeChatUserId == userId &&
            reconnectJob?.isActive == true
        ) {
            return
        }

        stopSocketJobs()

        activeChatUserId = userId

        _state.value = DirectMessageUiState(
            userId = userId,
            username = username,
            loading = true
        )

        incomingJob = viewModelScope.launch {
            directSocket.incoming.collect { incomingMessage ->

                val current = _state.value

                if (current.userId != userId) {
                    return@collect
                }

                val belongsToConversation =
                    incomingMessage.senderId == userId ||
                            incomingMessage.receiverId == userId

                if (!belongsToConversation) {
                    return@collect
                }

                addMessage(incomingMessage)
            }
        }

        statusJob = viewModelScope.launch {
            directSocket.connected.collect { connected ->

                val current = _state.value

                if (current.userId != userId) {
                    return@collect
                }

                _state.value = current.copy(
                    connected = connected,
                    error = if (connected) {
                        null
                    } else {
                        current.error
                    }
                )
            }
        }

        errorJob = viewModelScope.launch {
            directSocket.connectionError.collect { socketError ->

                if (_state.value.userId != userId) {
                    return@collect
                }

                if (!socketError.isNullOrBlank()) {
                    _state.value = _state.value.copy(
                        error = socketError
                    )
                }
            }
        }

        historyJob = viewModelScope.launch {
            loadHistory(userId)
        }

        reconnectJob = viewModelScope.launch {
            var retryDelay = 2_000L

            while (isActive) {
                if (
                    !directSocket.connected.value &&
                    !directSocket.connecting.value
                ) {
                    directSocket.connect()
                }

                delay(retryDelay)

                if (directSocket.connected.value) {
                    retryDelay = 2_000L
                } else {
                    retryDelay = (retryDelay * 2)
                        .coerceAtMost(30_000L)
                }
            }
        }
    }

    fun onDraftChange(value: String) {
        _state.value = _state.value.copy(
            draft = value.take(500),
            error = null
        )
    }

    private suspend fun loadHistory(userId: Long) {
        if (_state.value.userId != userId) {
            return
        }

        _state.value = _state.value.copy(
            loading = true,
            error = null
        )

        try {
            val history = repository.getMessages(userId)

            if (_state.value.userId != userId) {
                return
            }

            val mergedMessages = (
                    _state.value.messages + history
                    )
                .distinctBy { it.id }
                .sortedBy { it.createdAt ?: "" }

            _state.value = _state.value.copy(
                messages = mergedMessages,
                loading = false,
                error = null
            )
        } catch (e: Exception) {
            if (_state.value.userId != userId) {
                return
            }

            _state.value = _state.value.copy(
                loading = false,
                error = e.message ?: "Couldn't load chat history."
            )
        }
    }


    private fun addMessage(message: DirectMessageDto) {
        val current = _state.value

        if (current.messages.any { it.id == message.id }) {
            return
        }

        _state.value = current.copy(
            messages = (current.messages + message)
                .distinctBy { it.id }
                .sortedBy { it.createdAt ?: "" },
            error = null
        )
    }

    /**
     * Sends through the existing REST endpoint.
     * The returned server message is added immediately.
     */
    fun send() {
        val current = _state.value
        val text = current.draft.trim()

        if (text.isEmpty()) {
            return
        }

        if (current.sending) {
            return
        }

        if (current.userId <= 0L) {
            _state.value = current.copy(
                error = "Invalid recipient."
            )
            return
        }

        if (text.length > 500) {
            _state.value = current.copy(
                error = "Messages cannot exceed 500 characters."
            )
            return
        }

        val recipientId = current.userId

        viewModelScope.launch {
            _state.value = _state.value.copy(
                sending = true,
                error = null
            )

            try {
                val sentMessage = repository.sendMessage(
                    userId = recipientId,
                    message = text
                )

                if (_state.value.userId != recipientId) {
                    return@launch
                }

                val latest = _state.value

                _state.value = latest.copy(
                    messages = (latest.messages + sentMessage)
                        .distinctBy { it.id }
                        .sortedBy { it.createdAt ?: "" },
                    draft = "",
                    sending = false,
                    error = null
                )
            } catch (e: Exception) {
                if (_state.value.userId == recipientId) {
                    _state.value = _state.value.copy(
                        sending = false,
                        error = e.message
                            ?: "Message could not be sent."
                    )
                }
            }
        }
    }

    private fun stopSocketJobs() {
        reconnectJob?.cancel()
        incomingJob?.cancel()
        statusJob?.cancel()
        errorJob?.cancel()
        historyJob?.cancel()

        reconnectJob = null
        incomingJob = null
        statusJob = null
        errorJob = null
        historyJob = null

        directSocket.close()
    }

    override fun onCleared() {
        stopSocketJobs()
        super.onCleared()
    }
}