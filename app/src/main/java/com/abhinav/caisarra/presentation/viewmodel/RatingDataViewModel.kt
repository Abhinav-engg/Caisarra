package com.abhinav.caisarra.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.remote.dto.ClubLeaderboardEntry
import com.abhinav.caisarra.data.remote.dto.ModeStatsData
import com.abhinav.caisarra.data.remote.dto.PlayerRatingProfile
import com.abhinav.caisarra.data.repository.RatingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RatingDataUiState(
    val isLoading: Boolean = false,
    val publicProfile: PlayerRatingProfile? = null,
    val modeStats: ModeStatsData? = null,
    val leaderboard: List<ClubLeaderboardEntry> = emptyList(),
    val error: String? = null
)

class RatingDataViewModel(
    private val repository: RatingRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RatingDataUiState())
    val state = _state.asStateFlow()

    fun loadPublicProfile(username: String) {
        loadData {
            _state.value = _state.value.copy(
                publicProfile = repository.getPublicProfile(username),
                modeStats = null
            )
        }
    }

    fun loadModeStats(username: String, mode: String) {
        loadData {
            _state.value = _state.value.copy(
                modeStats = repository.getModeStats(username, mode)
            )
        }
    }

    fun loadClubLeaderboard(clubId: String) {
        loadData {
            _state.value = _state.value.copy(
                leaderboard = repository.getClubLeaderboard(clubId)
            )
        }
    }

    private fun loadData(
        request: suspend () -> Unit
    ) {
        if (_state.value.isLoading) return

        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                error = null
            )

            try {
                request()
                _state.value = _state.value.copy(
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unable to load rating data"
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
