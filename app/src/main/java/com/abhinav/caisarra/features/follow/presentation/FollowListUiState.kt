package com.abhinav.caisarra.features.follow.presentation

import com.abhinav.caisarra.features.follow.domain.FollowListTab
import com.abhinav.caisarra.features.follow.dto.FollowUserDto

data class FollowListUiState(
    val tab: FollowListTab,
    val users: List<FollowUserDto> = emptyList(),
    val nextBeforeId: Long? = null,
    val endReached: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)
