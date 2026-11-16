package com.abhinav.caisarra.presentation.notifications

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.core.network.userMessage
import com.abhinav.caisarra.data.remote.dto.NotificationItemDto
import com.abhinav.caisarra.data.repository.NotificationRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationsUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val notifications: List<NotificationItemDto> = emptyList(),
    val hasMore: Boolean = true,
    val unreadCount: Int = 0,
    val isClearing: Boolean = false,
    val error: String? = null
)

class NotificationsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NotificationRepository.get(application)

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        loadInitial()

        viewModelScope.launch {
            repository.unreadCount.collectLatest { count ->
                _uiState.update { it.copy(unreadCount = count) }
            }
        }

        viewModelScope.launch {
            repository.incomingNotifications.collectLatest { incoming ->
                if (!incoming.isRead) launch { repository.markAsRead(incoming.id) }
                _uiState.update { state ->
                    if (state.notifications.none { it.id == incoming.id }) {
                        state.copy(notifications = listOf(incoming) + state.notifications)
                    } else {
                        state
                    }
                }
            }
        }
    }

    fun loadInitial(isRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = !isRefresh && it.notifications.isEmpty(),
                    isRefreshing = isRefresh,
                    isClearing = false,
                    error = null
                )
            }

            repository.fetchUnreadCount()

            repository.getNotifications(limit = 30, beforeId = null)
                .onSuccess { list ->
                    if (list.any { !it.isRead }) repository.markAllAsRead()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            notifications = list,
                            hasMore = list.size >= 30,
                            error = null
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = err.userMessage("Failed to load notifications")
                        )
                    }
                }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoading || state.isRefreshing || state.isLoadingMore || !state.hasMore) return

        val beforeId = state.notifications.lastOrNull()?.id ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }

            repository.getNotifications(limit = 30, beforeId = beforeId)
                .onSuccess { nextBatch ->
                    _uiState.update { current ->
                        val existingIds = current.notifications.map { it.id }.toSet()
                        val newUnique = nextBatch.filter { it.id !in existingIds }
                        current.copy(
                            isLoadingMore = false,
                            notifications = current.notifications + newUnique,
                            hasMore = nextBatch.size >= 30
                        )
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoadingMore = false) }
                }
        }
    }

    fun delete(id: Long) {
        if (_uiState.value.isClearing) return
        _uiState.update { current ->
            val remaining = current.notifications.filterNot { it.id == id }
            current.copy(notifications = remaining)
        }
        viewModelScope.launch { repository.delete(id) }
    }

    fun deleteAll() {
        val count = _uiState.value.notifications.size
        val maxId = _uiState.value.notifications.maxOfOrNull { it.id } ?: 0L
        _uiState.update { it.copy(isClearing = true, hasMore = false) }
        viewModelScope.launch {
            val animDuration = (count.coerceAtMost(10) * 70L) + 300L
            delay(animDuration)
            _uiState.update { it.copy(notifications = emptyList(), isClearing = false) }
            repository.deleteAll(maxId)
        }
    }
}
