
package com.abhinav.caisarra.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.local.entity.GameEntity
import com.abhinav.caisarra.data.remote.api.DefaultAvatarItem
import com.abhinav.caisarra.data.remote.dto.ModeRating
import com.abhinav.caisarra.data.remote.dto.ProfileData
import com.abhinav.caisarra.data.remote.dto.UpdateProfileRequest
import com.abhinav.caisarra.data.repository.ProfileRepository
import com.abhinav.caisarra.data.repository.RatingRepository
import com.abhinav.caisarra.data.repository.RemoteGameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: ProfileData? = null,
    val loading: Boolean = true,
    val saving: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val defaultAvatars: List<DefaultAvatarItem> = emptyList(),
    val isOwnProfile: Boolean = true,
    val targetUsername: String? = null,
    val recentGames: List<GameEntity> = emptyList(),
    val loadingGames: Boolean = false
)

class ProfileViewModel(
    private val repository: ProfileRepository,
    private val ratingRepository: RatingRepository? = null,
    private val remoteGameRepository: RemoteGameRepository? = null,
    private val initialUsername: String? = null
) : ViewModel() {

    private val _state = MutableStateFlow(
        ProfileUiState(
            isOwnProfile = initialUsername.isNullOrBlank(),
            targetUsername = initialUsername?.trim()?.ifEmpty { null }
        )
    )
    val state = _state.asStateFlow()

    fun loadProfile(isGuest: Boolean = false, username: String? = null) {
        viewModelScope.launch {
            val targetUser = username?.trim()?.ifEmpty { null }
                ?: _state.value.targetUsername
                ?: initialUsername?.trim()?.ifEmpty { null }

            _state.value = _state.value.copy(
                loading = true,
                error = null,
                message = null,
                targetUsername = targetUser
            )

            try {
                if (targetUser != null) {
                    var myProfile: ProfileData? = null
                    if (!isGuest) {
                        myProfile = runCatching { repository.getMyProfile() }.getOrNull()
                    }

                    val isOwn = myProfile != null && (
                        myProfile.username.equals(targetUser, ignoreCase = true) ||
                        (myProfile.userId != null && myProfile.userId.toString() == targetUser)
                    )

                    if (isOwn) {
                        loadOwnProfileInternal(isGuest, myProfile!!)
                    } else {
                        loadPublicProfileInternal(targetUser)
                    }
                } else {
                    loadOwnProfileInternal(isGuest, null)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    loading = false,
                    error = e.message ?: "Unable to load profile"
                )
            }
        }
    }

    private suspend fun loadPublicProfileInternal(targetUser: String) {
        var profile = repository.getPublicProfile(targetUser)
        val enrichedRatings = profile.ratings?.toMutableMap() ?: mutableMapOf()
        val ratingRepo = ratingRepository

        if (ratingRepo != null && profile.username.isNotBlank()) {
            val publicRating = runCatching {
                ratingRepo.getPublicProfile(profile.username)
            }.getOrNull()

            publicRating?.ratings?.forEach { (key, modeRating) ->
                if (
                    enrichedRatings.keys.none {
                        it.equals(key, ignoreCase = true)
                    } && modeRating.rating != null
                ) {
                    enrichedRatings[key.lowercase()] = modeRating
                }
            }

            if (enrichedRatings.keys.none { it.equals("daily", ignoreCase = true) }) {
                val dailyStats = runCatching {
                    ratingRepo.getModeStats(profile.username, "daily")
                }.getOrNull()

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
            isOwnProfile = false,
            loading = false,
            loadingGames = true,
            error = null
        )

        val gameRepo = remoteGameRepository
        val games = if (gameRepo != null && profile.username.isNotBlank()) {
            runCatching {
                gameRepo.getPlayerGames(profile.username, limit = 10)
            }.getOrElse { emptyList() }
        } else {
            emptyList()
        }

        _state.value = _state.value.copy(
            recentGames = games,
            loadingGames = false
        )
    }

    private suspend fun loadOwnProfileInternal(isGuest: Boolean, existingProfile: ProfileData?) {
        var profile = existingProfile ?: repository.getMyProfile()
        val avatars = try { repository.getDefaultAvatars() } catch (_: Exception) {
            emptyList()
        }
        if (!isGuest && profile.avatarUrl.isNullOrBlank()) {
            val defaultAvatar = avatars.firstOrNull {
                it.id.equals("king", ignoreCase = true)
            } ?: avatars.firstOrNull()

            if (defaultAvatar != null) {
                profile = repository.setDefaultAvatar(defaultAvatar.id)
            }
        }
        val enrichedRatings = profile.ratings?.toMutableMap() ?: mutableMapOf()
        val ratingRepo = ratingRepository
        if (ratingRepo != null && profile.username.isNotBlank()) {
            val publicProfile = runCatching {
                ratingRepo.getPublicProfile(profile.username)
            }.getOrNull()

            publicProfile?.ratings?.forEach { (key, modeRating) ->
                if (
                    enrichedRatings.keys.none {
                        it.equals(key, ignoreCase = true)
                    } && modeRating.rating != null
                ) {
                    enrichedRatings[key.lowercase()] = modeRating
                }
            }

            if (
                enrichedRatings.keys.none {
                    it.equals("daily", ignoreCase = true)
                }
            ) {
                val dailyStats = runCatching {
                    ratingRepo.getModeStats(
                        profile.username,
                        "daily"
                    )
                }.getOrNull()

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
            defaultAvatars = avatars,
            isOwnProfile = true,
            loading = false,
            loadingGames = true,
            error = null
        )

        val gameRepo = remoteGameRepository
        val games = if (gameRepo != null && profile.username.isNotBlank()) {
            runCatching {
                gameRepo.getPlayerGames(profile.username, limit = 10)
            }.getOrElse { emptyList() }
        } else {
            emptyList()
        }

        _state.value = _state.value.copy(
            recentGames = games,
            loadingGames = false
        )
    }

    fun selectDefaultAvatar(avatarId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                saving = true,
                error = null,
                message = null
            )

            try {
                val profile = repository.setDefaultAvatar(avatarId)

                _state.value = _state.value.copy(
                    profile = profile,
                    saving = false,
                    message = "Avatar updated successfully",
                    error = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    saving = false,
                    error = e.message ?: "Unable to select avatar"
                )
            }
        }
    }

    fun uploadAvatar(context: Context, uri: Uri) {
        viewModelScope.launch {
            _state.value = _state.value.copy(saving = true, error = null, message = null)
            try {
                val profile = repository.uploadAvatar(context, uri)
                _state.value = _state.value.copy(profile = profile, saving = false, message = "Profile photo uploaded successfully")
            } catch (e: Exception) {
                _state.value = _state.value.copy(saving = false, error = e.message ?: "Unable to upload photo")
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
            val cleanedUrl = avatarUrl.trim()

            if (cleanedUrl.isNotEmpty() &&
                !cleanedUrl.startsWith("https://", ignoreCase = true)
            ) {
                _state.value = _state.value.copy(
                    saving = false,
                    error = "Avatar URL must start with https://",
                    message = null
                )
                return@launch
            }

            _state.value = _state.value.copy(
                saving = true,
                error = null,
                message = null
            )

            try {
                val updated = repository.updateProfile(
                    UpdateProfileRequest(
                        displayName = displayName.trim(),
                        country = country.trim().uppercase(),
                        bio = bio.trim(),
                        avatarUrl = cleanedUrl
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
