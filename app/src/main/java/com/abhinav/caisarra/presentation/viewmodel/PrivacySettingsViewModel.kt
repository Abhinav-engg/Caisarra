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
    val hasLoaded: Boolean = false,
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
                    hasLoaded = true,
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
                val current = _uiState.value.settings
                val dm = if (section == PrivacySection.DIRECT_MESSAGE) option.value else current.directMessage
                val club = if (section == PrivacySection.CLUB) option.value else current.club
                val tourn = if (section == PrivacySection.TOURNAMENT) option.value else current.tournament
                val invite = if (section == PrivacySection.INVITE_FRIEND) option.value else current.inviteFriend

                val updated = repository.updatePrivacySettings(
                    directMessage = dm,
                    club = club,
                    tournament = tourn,
                    inviteFriend = invite
                )
                _uiState.value = _uiState.value.copy(
                    updatingSection = null,
                    settings = updated,
                    hasLoaded = true,
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
                    hasLoaded = true,
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
