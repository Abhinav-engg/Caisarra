
package com.abhinav.caisarra.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.remote.dto.ProfileData
import com.abhinav.caisarra.data.remote.dto.UpdateProfileRequest
import com.abhinav.caisarra.data.repository.ProfileRepository
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
    private val repository: ProfileRepository
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
                _state.value = _state.value.copy(
                    profile = profile,
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
