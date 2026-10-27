package com.abhinav.caisarra.presentation.invite

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.theme.FriendsCardRadius
import com.abhinav.caisarra.core.ui.theme.FriendsScreenPadding
import com.abhinav.caisarra.presentation.invite.components.FriendCardItem
import com.abhinav.caisarra.presentation.invite.components.FriendRequestCardItem
import com.abhinav.caisarra.presentation.invite.components.FriendsLeaderboardView
import com.abhinav.caisarra.presentation.invite.components.FriendsSearchBar
import com.abhinav.caisarra.presentation.invite.components.FriendsTabSelector
import com.abhinav.caisarra.presentation.invite.components.PlayerSearchResultItem
import com.caisaara.ui.theme.FriendsBg
import com.caisaara.ui.theme.FriendsButtonDark
import com.caisaara.ui.theme.FriendsCardBg
import com.caisaara.ui.theme.FriendsCardBorder
import com.caisaara.ui.theme.FriendsCyan
import com.caisaara.ui.theme.FriendsDivider
import com.caisaara.ui.theme.FriendsTextMuted
import com.caisaara.ui.theme.FriendsTextWhite
import com.caisaara.ui.theme.GoldColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayWithFriendScreen(
    viewModel: FriendsViewModel,
    onCreateMatchLink: () -> Unit,
    onBack: () -> Unit,
    onDirectMessage: (Long, String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val pullToRefreshState = rememberPullToRefreshState()
    val onChallenge = { Toast.makeText(context, "Direct challenge coming soon!", Toast.LENGTH_SHORT).show() }

    if (state.showLeaderboard) {
        BackHandler { viewModel.setShowLeaderboard(false) }
        FriendsLeaderboardView(entries = state.leaderboardEntries, onClose = { viewModel.setShowLeaderboard(false) })
        return
    }

    Column(modifier = Modifier.fillMaxSize().background(FriendsBg).padding(top = 12.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = FriendsTextWhite) }
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("FRIENDS", color = FriendsTextWhite, fontWeight = FontWeight.Bold, fontSize = 17.sp, letterSpacing = 1.sp)
            }
            IconButton(onClick = { viewModel.setShowLeaderboard(true) }) {
                Icon(Icons.Default.EmojiEvents, "Leaderboard", tint = GoldColor)
            }
        }
        Spacer(Modifier.height(14.dp))
        Box(modifier = Modifier.padding(horizontal = FriendsScreenPadding)) {
            FriendsSearchBar(query = state.searchQuery, onQueryChange = viewModel::onSearchQueryChange, onClear = viewModel::clearSearch)
        }
        Spacer(Modifier.height(14.dp))
        if (state.searchQuery.isBlank()) {
            Box(modifier = Modifier.padding(horizontal = FriendsScreenPadding)) {
                FriendsTabSelector(selectedTab = state.selectedTab, friendsCount = state.friends.size, requestsCount = state.incomingRequests.size, onTabSelected = viewModel::selectTab)
            }
            Spacer(Modifier.height(18.dp))
        }

        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = viewModel::refresh,
            state = pullToRefreshState,
            modifier = Modifier.fillMaxSize().weight(1f),
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullToRefreshState,
                    isRefreshing = state.isLoading,
                    modifier = Modifier.align(Alignment.TopCenter),
                    containerColor = FriendsCardBg,
                    color = FriendsCyan
                )
            }
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = FriendsScreenPadding), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (state.isSearching) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = FriendsCyan)
                        }
                    }
                }
                if (state.searchQuery.isNotBlank() && !state.isSearching) {
                    if (state.searchResults.isEmpty()) {
                        item { EmptyNotice("No players found", "Try searching with another username.") }
                    } else {
                        item { Text("SEARCH RESULTS", color = FriendsTextMuted, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp) }
                        items(state.searchResults) { player ->
                            val status = state.friendshipStatuses[player.username]
                            val isFriend = status?.status == "friends" || state.friends.any { it.username == player.username }
                            PlayerSearchResultItem(
                                player = player,
                                status = if (isFriend) "friends" else status?.status,
                                isBusy = state.activeUsername == player.username,
                                onDirectMessage = { onDirectMessage(player.userId, player.username) },
                                onChallenge = onChallenge,
                                onAdd = { viewModel.addFriend(player.username) },
                                onCancel = { status?.requestId?.let { viewModel.cancelRequest(it, player.username) } },
                                onAccept = { status?.requestId?.let { viewModel.acceptRequest(it, player.username) } },
                                onReject = { status?.requestId?.let { viewModel.rejectRequest(it, player.username) } }
                            )
                        }
                    }
                } else if (state.searchQuery.isBlank()) {
                    when (state.selectedTab) {
                        FriendsTab.ALL_FRIENDS -> {
                            item {
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text("Your friends", color = FriendsTextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                                    Text("${state.friends.size} friends", color = FriendsTextMuted, fontSize = 13.sp)
                                }
                            }
                            if (state.friends.isEmpty() && !state.isLoading) {
                                item { EmptyNotice("No friends yet", "Search for a player by username above to add friends.") }
                            } else if (state.friends.isNotEmpty()) {
                                item {
                                    val cardShape = RoundedCornerShape(FriendsCardRadius)
                                    Column(modifier = Modifier.fillMaxWidth().clip(cardShape).background(FriendsCardBg).border(1.dp, FriendsCardBorder, cardShape)) {
                                        state.friends.forEachIndexed { index, friend ->
                                            FriendCardItem(friend = friend, onDirectMessage = { onDirectMessage(friend.userId, friend.username) }, onChallenge = onChallenge)
                                            if (index < state.friends.lastIndex) HorizontalDivider(color = FriendsDivider, thickness = 1.dp)
                                        }
                                    }
                                }
                            }
                        }
                        FriendsTab.REQUESTS -> {
                            if (state.incomingRequests.isEmpty() && state.sentRequests.isEmpty() && !state.isLoading) {
                                item { EmptyNotice("No friend requests", "Incoming and sent requests will appear here.") }
                            }
                            if (state.incomingRequests.isNotEmpty()) {
                                item { Text("INCOMING REQUESTS", color = FriendsTextMuted, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp) }
                                items(state.incomingRequests) { request ->
                                    FriendRequestCardItem(
                                        request = request,
                                        onAccept = { request.senderUsername?.let { viewModel.acceptRequest(request.id, it) } },
                                        onDecline = { request.senderUsername?.let { viewModel.rejectRequest(request.id, it) } }
                                    )
                                }
                            }
                            if (state.sentRequests.isNotEmpty()) {
                                item {
                                    Spacer(Modifier.height(8.dp))
                                    Text("SENT REQUESTS", color = FriendsTextMuted, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp)
                                }
                                items(state.sentRequests) { request ->
                                    SentRequestItem(name = request.receiverUsername ?: "Player ${request.receiverId ?: ""}", onCancel = { request.receiverUsername?.let { viewModel.cancelRequest(request.id, it) } })
                                }
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun SentRequestItem(name: String, onCancel: () -> Unit) {
    val cardShape = RoundedCornerShape(FriendsCardRadius)
    Row(
        modifier = Modifier.fillMaxWidth().clip(cardShape).background(FriendsCardBg).border(1.dp, FriendsCardBorder, cardShape).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(name, color = FriendsTextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text("Waiting for response", color = FriendsTextMuted, fontSize = 12.sp)
        }
        Box(
            modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(FriendsButtonDark).clickable(onClick = onCancel).padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Cancel", color = FriendsTextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EmptyNotice(title: String, subtitle: String) {
    val cardShape = RoundedCornerShape(FriendsCardRadius)
    Column(modifier = Modifier.fillMaxWidth().clip(cardShape).background(FriendsCardBg).padding(20.dp)) {
        Text(title, color = FriendsTextWhite, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, color = FriendsTextMuted, fontSize = 13.sp)
    }
}
