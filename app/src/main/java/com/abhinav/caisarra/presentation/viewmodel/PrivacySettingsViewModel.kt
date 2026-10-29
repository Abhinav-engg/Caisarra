package com.abhinav.caisarra.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.caisarra.data.remote.ApiErrorInterpreter
import com.abhinav.caisarra.data.remote.dto.PrivacyOption
import com.abhinav.caisarra.data.remote.dto.PrivacySettingsDto
import com.abhinav.caisarra.data.repository.PrivacyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class PrivacySection(val key: String, val title: String) {
    DIRECT_MESSAGE("direct_message", "Direct Messages"),
    CLUB("club", "Clubs"),
    TOURNAMENT("tournament", "Tournaments"),
    INVITE_FRIEND("invite_friend", "Friend Invites")
}

data class PrivacySettingsUiState(
    val isLoading: Boolean = true,
    val updatingSection: PrivacySection? = null,
    val settings: PrivacySettingsDto = PrivacySettingsDto(),
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class PrivacySettingsViewModel(
    private val repository: PrivacyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrivacySettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    fun loadSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )
            try {
                val data = repository.getPrivacySettings()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    settings = data,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = ApiErrorInterpreter.toUserMessage(e)
                )
            }
        }
    }

    fun updateSection(section: PrivacySection, option: PrivacyOption) {
        val currentSetting = when (section) {
            PrivacySection.DIRECT_MESSAGE -> _uiState.value.settings.directMessageOption
            PrivacySection.CLUB -> _uiState.value.settings.clubOption
            PrivacySection.TOURNAMENT -> _uiState.value.settings.tournamentOption
            PrivacySection.INVITE_FRIEND -> _uiState.value.settings.inviteFriendOption
        }
        if (currentSetting == option) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                updatingSection = section,
                errorMessage = null,
                successMessage = null
            )

            try {
                val updated = when (section) {
                    PrivacySection.DIRECT_MESSAGE -> repository.updatePrivacySettings(directMessage = option.value)
                    PrivacySection.CLUB -> repository.updatePrivacySettings(club = option.value)
                    PrivacySection.TOURNAMENT -> repository.updatePrivacySettings(tournament = option.value)
                    PrivacySection.INVITE_FRIEND -> repository.updatePrivacySettings(inviteFriend = option.value)
                }
                _uiState.value = _uiState.value.copy(
                    updatingSection = null,
                    settings = updated,
                    successMessage = "${section.title} privacy updated"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    updatingSection = null,
                    errorMessage = ApiErrorInterpreter.toUserMessage(e)
                )
            }
        }
    }

    fun updateAll(
        directMessage: PrivacyOption,
        club: PrivacyOption,
        tournament: PrivacyOption,
        inviteFriend: PrivacyOption
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )
            try {
                val updated = repository.updatePrivacySettings(
                    directMessage = directMessage.value,
                    club = club.value,
                    tournament = tournament.value,
                    inviteFriend = inviteFriend.value
                )
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    settings = updated,
                    successMessage = "Privacy settings updated successfully"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = ApiErrorInterpreter.toUserMessage(e)
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }
}
