package com.abhinav.caisarra.presentation.invite

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.local.TokenManager
import com.abhinav.caisarra.data.remote.RetrofitInstance
import com.abhinav.caisarra.data.remote.dto.FriendDto
import com.abhinav.caisarra.data.remote.dto.FriendRequestDto
import com.abhinav.caisarra.data.remote.dto.FriendshipStatusData
import com.abhinav.caisarra.data.remote.dto.PlayerSearchDto
import com.abhinav.caisarra.data.remote.dto.ProfileData
import com.abhinav.caisarra.data.repository.FriendsRepository
import com.abhinav.caisarra.data.repository.ProfileRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class FriendsTab {
    ALL_FRIENDS,
    REQUESTS
}

data class FriendsLeaderboardItem(
    val username: String,
    val displayName: String?,
    val avatarUrl: String?,
    val rating: Int,
    val isMe: Boolean
)

data class FriendsUiState(
    val friends: List<FriendDto> = emptyList(),
    val incomingRequests: List<FriendRequestDto> = emptyList(),
    val sentRequests: List<FriendRequestDto> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<PlayerSearchDto> = emptyList(),
    val friendshipStatuses: Map<String, FriendshipStatusData> = emptyMap(),
    val selectedTab: FriendsTab = FriendsTab.ALL_FRIENDS,
    val showLeaderboard: Boolean = false,
    val myProfile: ProfileData? = null,
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val activeUsername: String? = null,
    val message: String? = null,
    val error: String? = null
) {
    val leaderboardEntries: List<FriendsLeaderboardItem>
        get() {
            val list = mutableListOf<FriendsLeaderboardItem>()
            myProfile?.let {
                list.add(
                    FriendsLeaderboardItem(
                        username = it.username,
                        displayName = it.displayName,
                        avatarUrl = it.avatarUrl,
                        rating = it.ratings?.get("rapid")?.rating ?: it.rating ?: 1200,
                        isMe = true
                    )
                )
            }
            friends.forEach { friend ->
                if (friend.username != myProfile?.username) {
                    list.add(
                        FriendsLeaderboardItem(
                            username = friend.username,
                            displayName = friend.displayName,
                            avatarUrl = friend.avatarUrl,
                            rating = friend.rating ?: 1200,
                            isMe = false
                        )
                    )
                }
            }
            return list.sortedWith(
                compareByDescending<FriendsLeaderboardItem> { it.rating }
                    .thenBy { it.username }
            )
        }
}

class FriendsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = FriendsRepository(application)
    private val profileRepository = ProfileRepository(
        RetrofitInstance.createProfileService(TokenManager.get(application))
    )

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var searchVersion = 0

    init {
        refresh()
    }

    fun selectTab(tab: FriendsTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun setShowLeaderboard(show: Boolean) {
        _uiState.update { it.copy(showLeaderboard = show) }
    }

    fun clearSearch() {
        onSearchQueryChange("")
    }

    fun onSearchQueryChange(value: String) {
        val query = value.trimStart()

        _uiState.update {
            it.copy(
                searchQuery = value,
                error = null,
                message = null,
                searchResults = if (query.trim().isEmpty()) {
                    emptyList()
                } else {
                    it.searchResults
                },
                friendshipStatuses = if (query.trim().isEmpty()) {
                    emptyMap()
                } else {
                    it.friendshipStatuses
                }
            )
        }

        searchJob?.cancel()
        val version = ++searchVersion

        if (query.trim().isEmpty()) {
            _uiState.update { it.copy(isSearching = false) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(300)

            _uiState.update { it.copy(isSearching = true) }

            repository.searchPlayers(query.trim())
                .onSuccess { players ->
                    if (version != searchVersion) return@onSuccess

                    _uiState.update {
                        it.copy(
                            searchResults = players,
                            friendshipStatuses = emptyMap(),
                            isSearching = false,
                            error = null
                        )
                    }

                    players.forEach { player ->
                        launch {
                            repository.getFriendshipStatus(player.username)
                                .onSuccess { status ->
                                    if (version == searchVersion) {
                                        _uiState.update {
                                            it.copy(
                                                friendshipStatuses =
                                                    it.friendshipStatuses +
                                                            (player.username to status)
                                            )
                                        }
                                    }
                                }
                        }
                    }
                }
                .onFailure { error ->
                    if (version == searchVersion) {
                        _uiState.update {
                            it.copy(
                                isSearching = false,
                                searchResults = emptyList(),
                                error = error.message
                                    ?: "Could not search players."
                            )
                        }
                    }
                }
        }
    }

    fun refresh() {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, error = null)
            }

            val friendsTask = async { repository.getFriends() }
            val incomingTask = async { repository.getIncomingRequests() }
            val sentTask = async { repository.getSentRequests() }
            val profileTask = async { runCatching { profileRepository.getMyProfile() }.getOrNull() }

            val friendsResult = friendsTask.await()
            val incomingResult = incomingTask.await()
            val sentResult = sentTask.await()
            val profileResult = profileTask.await()

            _uiState.update { current ->
                current.copy(
                    isLoading = false,
                    myProfile = profileResult ?: current.myProfile,
                    friends = friendsResult.getOrNull() ?: current.friends,
                    incomingRequests = incomingResult.getOrNull()
                        ?: current.incomingRequests,
                    sentRequests = sentResult.getOrNull()
                        ?: current.sentRequests,
                    error = listOf(
                        friendsResult.exceptionOrNull(),
                        incomingResult.exceptionOrNull(),
                        sentResult.exceptionOrNull()
                    ).filterNotNull().firstOrNull()?.message
                )
            }

            refreshSearchStatuses()
        }
    }

    private fun refreshSearchStatuses() {
        val players = _uiState.value.searchResults
        val version = searchVersion

        viewModelScope.launch {
            players.forEach { player ->
                launch {
                    repository.getFriendshipStatus(player.username)
                        .onSuccess { status ->
                            if (version == searchVersion) {
                                _uiState.update {
                                    it.copy(
                                        friendshipStatuses =
                                            it.friendshipStatuses +
                                                    (player.username to status)
                                    )
                                }
                            }
                        }
                }
            }
        }
    }

    fun addFriend(username: String) {
        performPlayerAction(username) {
            repository.sendFriendRequest(username)
        }
    }

    fun acceptRequest(requestId: Long, username: String) {
        performPlayerAction(username) {
            repository.acceptRequest(requestId)
        }
    }

    fun rejectRequest(requestId: Long, username: String) {
        performPlayerAction(username) {
            repository.rejectRequest(requestId)
        }
    }

    fun cancelRequest(requestId: Long, username: String) {
        performPlayerAction(username) {
            repository.cancelRequest(requestId)
        }
    }

    fun removeFriend(userId: Long) {
        viewModelScope.launch {
            repository.removeFriend(userId)
                .onSuccess {
                    _uiState.update {
                        it.copy(message = "Friend removed.", error = null)
                    }
                    refresh()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(error = error.message)
                    }
                }
        }
    }

    private fun performPlayerAction(
        username: String,
        action: suspend () -> Result<Unit>
    ) {
        if (_uiState.value.activeUsername != null) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    activeUsername = username,
                    error = null,
                    message = null
                )
            }

            action()
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            activeUsername = null,
                            message = "Friend status updated.",
                            error = null
                        )
                    }

                    repository.getFriendshipStatus(username)
                        .onSuccess { status ->
                            _uiState.update {
                                it.copy(
                                    friendshipStatuses =
                                        it.friendshipStatuses +
                                                (username to status)
                                )
                            }
                        }

                    refresh()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            activeUsername = null,
                            error = error.message
                                ?: "Could not update friendship."
                        )
                    }
                }
        }
    }
}
