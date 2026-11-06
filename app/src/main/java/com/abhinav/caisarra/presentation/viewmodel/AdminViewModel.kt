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
    val users: List<AdminUser> = emptyList(),
    val reports: List<AdminReport> = emptyList(),
    val actions: List<AdminAction> = emptyList(),

    val searchQuery: String = "",
    val reportStatus: String = "pending"
)

class AdminViewModel(
    private val repository: AdminRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AdminUiState())
    val state = _state.asStateFlow()

    fun loadDashboard() {
        execute {
            _state.value = _state.value.copy(
                stats = repository.getStats(),
                loading = false
            )
        }
    }

    fun loadUsers(query: String = "") {
        execute {
            val users = repository.getUsers(query)
            _state.value = _state.value.copy(
                users = users,
                searchQuery = query,
                loading = false
            )
        }
    }

    fun loadReports(status: String = "pending") {
        execute {
            val reports = repository.getReports(status)
            _state.value = _state.value.copy(
                reports = reports,
                reportStatus = status,
                loading = false
            )
        }
    }

    fun loadActions() {
        execute {
            _state.value = _state.value.copy(
                actions = repository.getActions(),
                loading = false
            )
        }
    }

    fun banUser(id: Long, reason: String, days: Int) {
        execute {
            repository.banUser(id, reason, days)
            _state.value = _state.value.copy(
                message = "User banned successfully",
                users = repository.getUsers(_state.value.searchQuery),
                loading = false
            )
        }
    }

    fun unbanUser(id: Long) {
        execute {
            repository.unbanUser(id)
            _state.value = _state.value.copy(
                message = "User unbanned successfully",
                users = repository.getUsers(_state.value.searchQuery),
                loading = false
            )
        }
    }

    fun forceLogout(id: Long) {
        execute {
            repository.forceLogout(id)
            _state.value = _state.value.copy(
                message = "User logged out from all devices",
                loading = false
            )
        }
    }

    fun closeReport(id: Long, status: String, note: String = "") {
        execute {
            repository.closeReport(id, status, note)
            _state.value = _state.value.copy(
                reports = repository.getReports(_state.value.reportStatus),
                message = "Report updated successfully",
                loading = false
            )
        }
    }

    fun deletePost(id: Long) {
        execute {
            repository.deletePost(id)
            _state.value = _state.value.copy(
                message = "Post deleted successfully",
                loading = false
            )
        }
    }

    fun deleteComment(id: Long) {
        execute {
            repository.deleteComment(id)
            _state.value = _state.value.copy(
                message = "Comment deleted successfully",
                loading = false
            )
        }
    }

    fun deleteClub(id: String) {
        execute {
            repository.deleteClub(id)
            _state.value = _state.value.copy(
                message = "Club deleted successfully",
                loading = false
            )
        }
    }

    fun cancelTournament(id: Long) {
        execute {
            repository.cancelTournament(id)
            _state.value = _state.value.copy(
                message = "Tournament cancelled successfully",
                loading = false
            )
            loadDashboard()
        }
    }

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
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    loading = false,
                    error = e.message ?: "Something went wrong"
                )
            }
        }
    }
}