
package com.abhinav.caisarra.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.remote.dto.ModeRating
import com.abhinav.caisarra.data.remote.dto.ProfileData
import com.abhinav.caisarra.data.remote.dto.UpdateProfileRequest
import com.abhinav.caisarra.data.repository.ProfileRepository
import com.abhinav.caisarra.data.repository.RatingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: ProfileData? = null,
    val loading: Boolean = true,
    val saving: Boolean = false,
    val error: String? = null,
    val message: String? = null
)

class ProfileViewModel(
    private val repository: ProfileRepository,
    private val ratingRepository: RatingRepository? = null
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state = _state.asStateFlow()

    fun loadProfile() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                loading = true,
                error = null,
                message = null
            )

            try {
                val profile = repository.getMyProfile()
                val enrichedRatings = profile.ratings?.toMutableMap() ?: mutableMapOf()

                if (ratingRepository != null && profile.username.isNotBlank()) {
                    val publicProfile = runCatching { ratingRepository.getPublicProfile(profile.username) }.getOrNull()
                    publicProfile?.ratings?.forEach { (key, modeRating) ->
                        if (!enrichedRatings.keys.any { it.equals(key, ignoreCase = true) } && modeRating.rating != null) {
                            enrichedRatings[key.lowercase()] = modeRating
                        }
                    }

                    if (!enrichedRatings.keys.any { it.equals("daily", ignoreCase = true) }) {
                        val dailyStats = runCatching { ratingRepository.getModeStats(profile.username, "daily") }.getOrNull()
                        if (dailyStats != null && dailyStats.rating > 0) {
                            enrichedRatings["daily"] = ModeRating(
                                rating = dailyStats.rating,
                                provisional = dailyStats.provisional
                            )
                        }
                    }
                }

                val finalProfile = if (enrichedRatings.isNotEmpty()) {
                    profile.copy(ratings = enrichedRatings)
                } else {
                    profile
                }

                _state.value = _state.value.copy(
                    profile = finalProfile,
                    loading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    loading = false,
                    error = e.message ?: "Unable to load profile"
                )
            }
        }
    }

    fun updateProfile(
        displayName: String,
        country: String,
        bio: String,
        avatarUrl: String
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                saving = true,
                error = null,
                message = null
            )

            try {
                val updated = repository.updateProfile(
                    UpdateProfileRequest(
                        displayName = displayName.trim().ifEmpty { "" },
                        country = country.trim().uppercase().ifEmpty { "" },
                        bio = bio.trim().ifEmpty { "" },
                        avatarUrl = avatarUrl.trim().ifEmpty { "" }
                    )
                )

                _state.value = _state.value.copy(
                    profile = updated,
                    saving = false,
                    message = "Profile updated successfully"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    saving = false,
                    error = e.message ?: "Unable to update profile"
                )
            }
        }
    }
}
