
package com.abhinav.caisarra.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.remote.admin.*
import com.abhinav.caisarra.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val message: String? = null,

    val stats: AdminStats? = null,
    val health: AdminHealth? = null,
    val users: List<AdminUser> = emptyList(),
    val selectedUserDetails: AdminUserDetails? = null,
    val reports: List<AdminReport> = emptyList(),
    val actions: List<AdminAction> = emptyList(),
    val games: List<AdminGame> = emptyList(),
    val selectedGame: AdminGameDetailsResponse? = null,
    val matchmaking: List<AdminMatchmakingQueue> = emptyList(),

    val searchQuery: String = "",
    val reportStatus: String = "pending"
)

class AdminViewModel(
    private val repository: AdminRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AdminUiState())
    val state = _state.asStateFlow()

    private fun execute(
        block: suspend () -> Unit
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                loading = true,
                error = null,
                message = null
            )

            try {
                block()
                _state.value = _state.value.copy(loading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    loading = false,
                    error = e.message ?: "Something went wrong"
                )
            }
        }
    }

    fun loadDashboard() {
        execute {
            val stats = repository.getStats()
            val health = repository.getHealth()
            val matchmaking = repository.getMatchmaking()

            _state.value = _state.value.copy(
                stats = stats,
                health = health,
                matchmaking = matchmaking
            )
        }
    }

    fun loadUsers(query: String = "") {
        execute {
            val users = repository.getUsers(query)

            _state.value = _state.value.copy(
                users = users,
                searchQuery = query
            )
        }
    }

    fun loadUserDetails(id: Long) {
        execute {
            val details = repository.getUserDetails(id)

            _state.value = _state.value.copy(
                selectedUserDetails = details
            )
        }
    }

    fun clearSelectedUser() {
        _state.value = _state.value.copy(
            selectedUserDetails = null
        )
    }

    fun loadReports(status: String = "pending") {
        execute {
            val reports = repository.getReports(status)

            _state.value = _state.value.copy(
                reports = reports,
                reportStatus = status
            )
        }
    }

    fun loadGames() {
        execute {
            _state.value = _state.value.copy(
                games = repository.getGames()
            )
        }
    }

    fun loadGameDetails(id: String) {
        execute {
            _state.value = _state.value.copy(
                selectedGame = repository.getGameDetails(id)
            )
        }
    }

    fun clearSelectedGame() {
        _state.value = _state.value.copy(
            selectedGame = null
        )
    }

    fun loadMatchmaking() {
        execute {
            _state.value = _state.value.copy(
                matchmaking = repository.getMatchmaking()
            )
        }
    }

    fun loadActions() {
        execute {
            _state.value = _state.value.copy(
                actions = repository.getActions()
            )
        }
    }

    fun banUser(id: Long, reason: String, days: Int) {
        execute {
            repository.banUser(id, reason, days)

            _state.value = _state.value.copy(
                message = "User banned successfully",
                users = repository.getUsers(_state.value.searchQuery)
            )
        }
    }

    fun unbanUser(id: Long) {
        execute {
            repository.unbanUser(id)

            _state.value = _state.value.copy(
                message = "User unbanned successfully",
                users = repository.getUsers(_state.value.searchQuery)
            )
        }
    }

    fun forceLogout(id: Long) {
        execute {
            repository.forceLogout(id)

            _state.value = _state.value.copy(
                message = "User logged out from all devices"
            )
        }
    }

    fun closeReport(
        id: Long,
        status: String,
        note: String = ""
    ) {
        execute {
            repository.closeReport(id, status, note)

            _state.value = _state.value.copy(
                reports = repository.getReports(_state.value.reportStatus),
                message = "Report updated successfully"
            )
        }
    }

    fun abortGame(id: String, reason: String) {
        execute {
            repository.abortGame(id, reason)

            _state.value = _state.value.copy(
                games = repository.getGames(),
                message = "Game aborted successfully",
                selectedGame = null
            )
        }
    }

    fun deletePost(id: Long) {
        execute {
            repository.deletePost(id)
            _state.value = _state.value.copy(
                message = "Post deleted successfully"
            )
        }
    }

    fun deleteComment(id: Long) {
        execute {
            repository.deleteComment(id)
            _state.value = _state.value.copy(
                message = "Comment deleted successfully"
            )
        }
    }

    fun deleteClub(id: String) {
        execute {
            repository.deleteClub(id)
            _state.value = _state.value.copy(
                message = "Club deleted successfully"
            )
        }
    }

    fun cancelTournament(id: Long) {
        execute {
            repository.cancelTournament(id)
            _state.value = _state.value.copy(
                message = "Tournament cancelled successfully",
                stats = repository.getStats()
            )
        }
    }
}
