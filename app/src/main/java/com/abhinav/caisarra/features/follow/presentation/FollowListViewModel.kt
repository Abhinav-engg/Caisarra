package com.abhinav.caisarra.features.follow.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.core.network.userMessage
import com.abhinav.caisarra.features.follow.data.FollowRepository
import com.abhinav.caisarra.features.follow.domain.FollowListTab
import com.abhinav.caisarra.features.follow.dto.FollowUserDto
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FollowListViewModel(
    private val repository: FollowRepository,
    private val userId: Long,
    initialTab: FollowListTab
) : ViewModel() {
    private val _state = MutableStateFlow(FollowListUiState(tab = initialTab))
    val state = _state.asStateFlow()
    private var loadJob: Job? = null

    fun selectTab(tab: FollowListTab) {
        if (tab == _state.value.tab) return
        loadJob?.cancel()
        _state.value = FollowListUiState(tab = tab)
        loadMore()
    }

    fun loadMore() {
        val current = _state.value
        if (current.isLoading || current.endReached) return
        _state.update { it.copy(isLoading = true, error = null) }
        loadJob = viewModelScope.launch {
            repository.users(userId, current.tab, current.nextBeforeId)
                .onSuccess { page ->
                    _state.update {
                        it.copy(
                            users = (it.users + page.users).distinctBy(FollowUserDto::id),
                            nextBeforeId = page.nextBeforeId,
                            endReached = page.nextBeforeId == null,
                            isLoading = false
                        )
                    }
                }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.userMessage("Unable to load players.")) } }
        }
    }

    fun toggleFollow(user: FollowUserDto) {
        viewModelScope.launch {
            repository.setFollowing(user.id, !user.isFollowing)
                .onSuccess { following -> _state.update { s -> s.copy(users = s.users.map { if (it.id == user.id) it.copy(isFollowing = following) else it }) } }
                .onFailure { e -> _state.update { it.copy(error = e.userMessage("Unable to update follow.")) } }
        }
    }
}
