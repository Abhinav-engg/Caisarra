package com.abhinav.caisarra.features.blockedplayers.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.abhinav.caisarra.core.ui.components.CaisaaraTopBar
import com.abhinav.caisarra.core.ui.theme.StatsScreenPadding
import com.abhinav.caisarra.core.ui.theme.StatsSectionSpacing
import com.abhinav.caisarra.data.remote.dto.PlayerSearchDto
import com.abhinav.caisarra.presentation.invite.EmptyNotice
import com.abhinav.caisarra.presentation.invite.FriendsViewModel
import com.abhinav.caisarra.presentation.invite.components.PlayerSearchResultItem
import com.caisaara.ui.theme.FriendsCyan
import com.caisaara.ui.theme.RedNormal
import com.caisaara.ui.theme.StatsBg

@Composable
fun BlockedPlayersScreen(
    viewModel: FriendsViewModel,
    onBack: () -> Unit,
    onViewProfile: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StatsBg)
            .statusBarsPadding()
    ) {
        CaisaaraTopBar(title = "BLOCKED PLAYERS", onBack = onBack)

        LazyColumn(
            contentPadding = PaddingValues(StatsScreenPadding),
            verticalArrangement = Arrangement.spacedBy(StatsSectionSpacing)
        ) {
            uiState.error?.let { item { Text(it, color = RedNormal) } }
            if (uiState.blockedUsers.isEmpty() && uiState.isLoading) {
                item { CircularProgressIndicator(color = FriendsCyan) }
            } else if (uiState.blockedUsers.isEmpty() && uiState.error == null) {
                item { EmptyNotice("No blocked players", "Players you block will appear here.") }
            }
            items(uiState.blockedUsers, key = { it.userId }) { user ->
                PlayerSearchResultItem(
                    player = PlayerSearchDto(userId = user.userId, username = user.username, rating = user.rating),
                    status = "blocked",
                    isBusy = uiState.activeUsername == user.username,
                    onPlayerClick = { onViewProfile(user.username) },
                    onUnblock = { viewModel.unblockUser(user.userId, user.username) }
                )
            }
        }
    }
}
