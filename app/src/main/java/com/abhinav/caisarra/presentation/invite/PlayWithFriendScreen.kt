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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.theme.FriendsCardRadius
import com.abhinav.caisarra.core.ui.theme.FriendsScreenPadding
import com.abhinav.caisarra.data.remote.dto.FriendDto
import com.abhinav.caisarra.data.repository.ChallengeManager
import com.abhinav.caisarra.presentation.invite.components.DirectChallengeDialog
import com.abhinav.caisarra.presentation.invite.components.FriendCardItem
import com.abhinav.caisarra.presentation.invite.components.FriendRequestCardItem
import com.abhinav.caisarra.presentation.invite.components.FriendsLeaderboardView
import com.abhinav.caisarra.presentation.invite.components.FriendsSearchBar
import com.abhinav.caisarra.presentation.invite.components.FriendsTabSelector
import com.abhinav.caisarra.presentation.invite.components.PlayerSearchResultItem
import com.abhinav.caisarra.presentation.invite.components.ReceivedChallengeCardItem
import com.abhinav.caisarra.presentation.invite.components.SentChallengeCardItem
import com.caisaara.ui.theme.FriendsBg
import com.caisaara.ui.theme.FriendsButtonDark
import com.caisaara.ui.theme.FriendsCardBg
import com.caisaara.ui.theme.FriendsCardBorder
import com.caisaara.ui.theme.FriendsCyan
import com.caisaara.ui.theme.FriendsDivider
import com.caisaara.ui.theme.FriendsTextMuted
import com.caisaara.ui.theme.FriendsTextWhite
import com.caisaara.ui.theme.GoldColor
import kotlinx.coroutines.launch
import com.abhinav.caisarra.core.ui.components.CaisaaraTopBar

data class ChallengeTarget(
    val userId: Long,
    val username: String,
    val avatarUrl: String? = null,
    val rating: Int? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayWithFriendScreen(
    viewModel: FriendsViewModel,
    onCreateMatchLink: () -> Unit,
    onBack: () -> Unit,
    onDirectMessage: (Long, String) -> Unit,
    onViewProfile: (String) -> Unit = {},
    onEnterGame: (String) -> Unit = {},
    onJoinByCode: ((String) -> Unit)? = null
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pullToRefreshState = rememberPullToRefreshState()
    val challengeManager = remember { ChallengeManager.get(context) }

    val receivedChallenges by challengeManager.receivedChallenges.collectAsState()
    val sentChallenges by challengeManager.sentChallenges.collectAsState()

    var challengeTarget by remember { mutableStateOf<ChallengeTarget?>(null) }
    var showQrScanner by remember { mutableStateOf(false) }
    var removeTarget by remember { mutableStateOf<FriendDto?>(null) }

    LaunchedEffect(Unit) {
        challengeManager.refreshChallenges()
    }

    if (state.showLeaderboard) {
        BackHandler { viewModel.setShowLeaderboard(false) }
        FriendsLeaderboardView(
            entries = state.leaderboardEntries,
            onClose = { viewModel.setShowLeaderboard(false) },
            onPlayerClick = onViewProfile
        )
        return
    }

    challengeTarget?.let { target ->
        DirectChallengeDialog(
            opponentId = target.userId,
            opponentUsername = target.username,
            opponentAvatarUrl = target.avatarUrl,
            opponentRating = target.rating,
            onDismiss = { challengeTarget = null },
            onGameStarted = { gameId ->
                challengeTarget = null
                onEnterGame(gameId)
            }
        )
    }

    removeTarget?.let { friend ->
        AlertDialog(
            onDismissRequest = { removeTarget = null },
            containerColor = FriendsCardBg,
            title = {
                Text("Remove friend", color = FriendsTextWhite, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Remove ${friend.displayName ?: friend.username} from your friends?",
                    color = FriendsTextMuted
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.removeFriend(friend.userId)
                        removeTarget = null
                    }
                ) {
                    Text("Remove", color = Color(0xFFF87171), fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { removeTarget = null }) {
                    Text("Cancel", color = FriendsTextMuted)
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(FriendsBg).statusBarsPadding().padding(top = 12.dp)) {
        CaisaaraTopBar(
            title = "FRIENDS",
            onBack = onBack,
            backgroundColor = FriendsBg,
            titleColor = FriendsTextWhite,
            iconTint = FriendsTextWhite,
            actions = {
                if (onJoinByCode != null) {
                    IconButton(
                        onClick = {
                            showQrScanner = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan QR to join",
                            tint = FriendsCyan
                        )
                    }
                }

                IconButton(
                    onClick = {
                        viewModel.setShowLeaderboard(true)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Leaderboard",
                        tint = GoldColor
                    )
                }
            }
        )
        Spacer(Modifier.height(14.dp))
        Box(modifier = Modifier.padding(horizontal = FriendsScreenPadding)) {
            FriendsSearchBar(query = state.searchQuery, onQueryChange = viewModel::onSearchQueryChange, onClear = viewModel::clearSearch)
        }
        Spacer(Modifier.height(14.dp))
        if (state.searchQuery.isBlank()) {
            Box(modifier = Modifier.padding(horizontal = FriendsScreenPadding)) {
                FriendsTabSelector(
                    selectedTab = state.selectedTab,
                    friendsCount = state.friends.size,
                    requestsCount = state.incomingRequests.size + receivedChallenges.size,
                    onTabSelected = viewModel::selectTab
                )
            }
            Spacer(Modifier.height(18.dp))
        }

        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = {
                viewModel.refresh()
                challengeManager.refreshChallenges()
            },
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
                } else if (state.searchQuery.isNotBlank()) {
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
                                onPlayerClick = { onViewProfile(player.username) },
                                onDirectMessage = { onDirectMessage(player.userId, player.username) },
                                onChallenge = {
                                    challengeTarget = ChallengeTarget(
                                        userId = player.userId,
                                        username = player.username,
                                        avatarUrl = player.avatarUrl,
                                        rating = player.rating
                                    )
                                },
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
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "MY FRIENDS",
                                        color = FriendsTextMuted,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        letterSpacing = 1.sp,
                                        modifier = Modifier.weight(1f)
                                    )

                                    if (onJoinByCode != null) {
                                        Text(
                                            text = "SCAN QR",
                                            color = FriendsCyan,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            letterSpacing = 1.sp,
                                            modifier = Modifier.clickable { showQrScanner = true }
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                    }

                                    Text(
                                        text = "INVITE LINK",
                                        color = FriendsCyan,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        letterSpacing = 1.sp,
                                        modifier = Modifier.clickable(onClick = onCreateMatchLink)
                                    )
                                }
                            }

                            if (state.friends.isNotEmpty()) {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val filterAllSelected = state.presenceFilter == FriendsPresenceFilter.ALL
                                        val filterOnlineSelected = state.presenceFilter == FriendsPresenceFilter.ONLINE_ONLY
                                        val chipShape = RoundedCornerShape(20.dp)

                                        Box(
                                            modifier = Modifier
                                                .clip(chipShape)
                                                .background(
                                                    if (filterAllSelected) {
                                                        Color(0xFF007A87)
                                                    } else {
                                                        FriendsCardBg
                                                    }
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    color = if (filterAllSelected) {
                                                        Color(0xFF00CED1)
                                                    } else {
                                                        FriendsCardBorder
                                                    },
                                                    shape = chipShape
                                                )
                                                .clickable {
                                                    viewModel.selectPresenceFilter(FriendsPresenceFilter.ALL)
                                                }
                                                .padding(horizontal = 14.dp, vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "All (${state.friends.size})",
                                                color = if (filterAllSelected) {
                                                    FriendsTextWhite
                                                } else {
                                                    FriendsTextMuted
                                                },
                                                fontSize = 12.sp,
                                                fontWeight = if (filterAllSelected) {
                                                    FontWeight.Bold
                                                } else {
                                                    FontWeight.Medium
                                                }
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(chipShape)
                                                .background(
                                                    if (filterOnlineSelected) {
                                                        Color(0xFF007A87)
                                                    } else {
                                                        FriendsCardBg
                                                    }
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    color = if (filterOnlineSelected) {
                                                        Color(0xFF00CED1)
                                                    } else {
                                                        FriendsCardBorder
                                                    },
                                                    shape = chipShape
                                                )
                                                .clickable {
                                                    viewModel.selectPresenceFilter(FriendsPresenceFilter.ONLINE_ONLY)
                                                }
                                                .padding(horizontal = 14.dp, vertical = 6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(7.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFF22C55E))
                                                )

                                                Text(
                                                    text = "Online (${state.onlineFriendsCount})",
                                                    color = if (filterOnlineSelected) {
                                                        FriendsTextWhite
                                                    } else {
                                                        FriendsTextMuted
                                                    },
                                                    fontSize = 12.sp,
                                                    fontWeight = if (filterOnlineSelected) {
                                                        FontWeight.Bold
                                                    } else {
                                                        FontWeight.Medium
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (state.friends.isEmpty() && !state.isLoading) {
                                item {
                                    EmptyNotice(
                                        title = "No friends yet",
                                        subtitle = "Search for a player by username above to add friends."
                                    )
                                }
                            } else if (state.visibleFriends.isEmpty() && state.presenceFilter == FriendsPresenceFilter.ONLINE_ONLY && !state.isLoading) {
                                item {
                                    EmptyNotice(
                                        title = "No friends online",
                                        subtitle = "None of your friends are currently online. Check back later or invite them to play!"
                                    )
                                }
                            } else if (state.visibleFriends.isNotEmpty()) {
                                item {
                                    val cardShape = RoundedCornerShape(FriendsCardRadius)
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(cardShape)
                                            .background(FriendsCardBg)
                                            .border(1.dp, FriendsCardBorder, cardShape)
                                    ) {
                                        state.visibleFriends.forEachIndexed { index, friend ->
                                            FriendCardItem(
                                                friend = friend,
                                                onProfileClick = {
                                                    onViewProfile(friend.username)
                                                },
                                                onDirectMessage = {
                                                    onDirectMessage(friend.userId, friend.username)
                                                },
                                                onChallenge = {
                                                    challengeTarget = ChallengeTarget(
                                                        userId = friend.userId,
                                                        username = friend.username,
                                                        avatarUrl = friend.avatarUrl,
                                                        rating = friend.rating
                                                    )
                                                },
                                                onRemove = {
                                                    removeTarget = friend
                                                }
                                            )
                                            if (index < state.visibleFriends.lastIndex) {
                                                HorizontalDivider(
                                                    color = FriendsDivider,
                                                    thickness = 1.dp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        FriendsTab.REQUESTS -> {
                            // 1. Incoming Challenges
                            if (receivedChallenges.isNotEmpty()) {
                                item { Text("INCOMING CHALLENGES", color = FriendsTextMuted, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp) }
                                items(receivedChallenges) { challenge ->
                                    ReceivedChallengeCardItem(
                                        challenge = challenge,
                                        onAccept = {
                                            scope.launch {
                                                challengeManager.acceptChallenge(challenge.code)
                                                    .onSuccess { res ->
                                                        onEnterGame(res.gameId.ifBlank { challenge.gameId })
                                                    }
                                                    .onFailure { err ->
                                                        Toast.makeText(context, err.message ?: "Failed to accept challenge", Toast.LENGTH_SHORT).show()
                                                    }
                                            }
                                        },
                                        onDecline = {
                                            scope.launch {
                                                challengeManager.declineChallenge(challenge.code)
                                            }
                                        },
                                        onProfileClick = { onViewProfile(challenge.challengerUsername) }
                                    )
                                }
                                item { Spacer(Modifier.height(8.dp)) }
                            }

                            // 2. Sent Challenges
                            if (sentChallenges.isNotEmpty()) {
                                item { Text("SENT CHALLENGES", color = FriendsTextMuted, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp) }
                                items(sentChallenges) { challenge ->
                                    SentChallengeCardItem(
                                        challenge = challenge,
                                        onEnterGame = { onEnterGame(challenge.gameId) },
                                        onCancel = {
                                            scope.launch {
                                                challengeManager.cancelChallenge(challenge.code)
                                            }
                                        },
                                        onProfileClick = { onViewProfile(challenge.opponentUsername) }
                                    )
                                }
                                item { Spacer(Modifier.height(8.dp)) }
                            }

                            // 3. Friend requests
                            if (state.incomingRequests.isEmpty() && state.sentRequests.isEmpty() && receivedChallenges.isEmpty() && sentChallenges.isEmpty() && !state.isLoading) {
                                item { EmptyNotice("No pending challenges or requests", "Incoming and sent challenges and friend requests will appear here.") }
                            }
                            if (state.incomingRequests.isNotEmpty()) {
                                item { Text("INCOMING FRIEND REQUESTS", color = FriendsTextMuted, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp) }
                                items(state.incomingRequests) { request ->
                                    FriendRequestCardItem(
                                        request = request,
                                        onProfileClick = { request.senderUsername?.let(onViewProfile) },
                                        onAccept = { request.senderUsername?.let { viewModel.acceptRequest(request.id, it) } },
                                        onDecline = { request.senderUsername?.let { viewModel.rejectRequest(request.id, it) } }
                                    )
                                }
                            }
                            if (state.sentRequests.isNotEmpty()) {
                                item {
                                    Spacer(Modifier.height(8.dp))
                                    Text("SENT FRIEND REQUESTS", color = FriendsTextMuted, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp)
                                }
                                items(state.sentRequests) { request ->
                                    SentRequestItem(
                                        name = request.receiverUsername ?: "Player ${request.receiverId ?: ""}",
                                        onProfileClick = { request.receiverUsername?.let(onViewProfile) },
                                        onCancel = { request.receiverUsername?.let { viewModel.cancelRequest(request.id, it) } }
                                    )
                                }
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }

    if (showQrScanner && onJoinByCode != null) {
        QrCodeScannerDialog(
            onDismiss = { showQrScanner = false },
            onQrScanned = { rawCode ->
                showQrScanner = false
                val code = InviteCodeParser.extractCode(rawCode)
                if (code.isNotBlank()) {
                    onJoinByCode(code)
                }
            }
        )
    }
}

@Composable
private fun SentRequestItem(name: String, onProfileClick: (() -> Unit)? = null, onCancel: () -> Unit) {
    val cardShape = RoundedCornerShape(FriendsCardRadius)
    Row(
        modifier = Modifier.fillMaxWidth().clip(cardShape).background(FriendsCardBg).border(1.dp, FriendsCardBorder, cardShape).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .then(if (onProfileClick != null) Modifier.clickable(onClick = onProfileClick) else Modifier)
        ) {
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
    val shape = RoundedCornerShape(FriendsCardRadius)
    Column(
        modifier = Modifier.fillMaxWidth().clip(shape).background(FriendsCardBg).border(1.dp, FriendsCardBorder, shape).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(title, color = FriendsTextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Text(subtitle, color = FriendsTextMuted, fontSize = 13.sp)
    }
}
