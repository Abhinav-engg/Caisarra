
package com.abhinav.caisarra.presentation.invite

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.data.remote.dto.FriendDto
import com.abhinav.caisarra.presentation.game.components.setup.SetupColors
import com.abhinav.caisarra.presentation.game.components.setup.SetupHeader
import com.abhinav.caisarra.presentation.game.components.setup.SetupPage
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material3.OutlinedButton
@Composable
fun PlayWithFriendScreen(
    viewModel: FriendsViewModel,
    onCreateMatchLink: () -> Unit,
    onBack: () -> Unit,
    onDirectMessage: (Long, String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    SetupPage {
        SetupHeader("Invite Friends", onBack)

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionTitle("YOUR FRIENDS")

            Button(
                onClick = viewModel::refresh,
                enabled = !state.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SetupColors.Card,
                    contentColor = SetupColors.Accent
                )
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                Text(" Refresh")
            }
        }

        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = SetupColors.Accent
            )
        }

        if (!state.isLoading && state.friends.isEmpty()) {
            InfoCard(
                title = "No friends yet",
                subtitle = "Search for a username below to add your first friend."
            )
        }

        state.friends.forEach { friend ->
            FriendCard(
                friend = friend,
                onInvite = onCreateMatchLink,
                onRemove = {
                    viewModel.removeFriend(friend.userId)
                },
                onDirectMessage = {
                    onDirectMessage(
                        friend.userId,
                        friend.username
                    )
                }
            )

            Spacer(Modifier.height(10.dp))
        }

        Spacer(Modifier.height(24.dp))
        SectionTitle("FIND PLAYERS")
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = viewModel::onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search players",
                    tint = SetupColors.Secondary
                )
            },
            label = { Text("Search by username") },
            placeholder = { Text("Type at least 2 characters") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text
            ),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = SetupColors.Text,
                unfocusedTextColor = SetupColors.Text,
                focusedBorderColor = SetupColors.Accent,
                unfocusedBorderColor = Color(0xFF26384D),
                focusedLabelColor = SetupColors.Accent,
                unfocusedLabelColor = SetupColors.Secondary,
                cursorColor = SetupColors.Accent
            )
        )

        Spacer(Modifier.height(12.dp))
        if (state.isSearching) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = SetupColors.Accent
            )
        }

        if (state.searchQuery.trim().length == 1) {
            InfoCard(
                title = "Keep typing",
                subtitle = "Enter at least 2 characters to search."
            )
        }
        if (
            !state.isSearching &&
            state.searchQuery.trim().length >= 2 &&
            state.searchResults.isEmpty() &&
            state.error == null
        ) {
            InfoCard(
                title = "No players found",
                subtitle = "Try another username."
            )
        }

        state.searchResults.forEach { player ->
            val status =
                state.friendshipStatuses[player.username]

            PlayerCard(
                username = player.username,
                displayName = player.displayName,
                rating = player.rating,
                status = status?.status,
                isBusy = state.activeUsername == player.username,
                onAdd = { viewModel.addFriend(player.username) },
                onCancel = {
                    status?.requestId?.let { id ->
                        viewModel.cancelRequest(id, player.username)
                    }
                },
                onAccept = {status?.requestId?.let { id ->
                        viewModel.acceptRequest(id, player.username)
                    }
                },
                onReject = {
                    status?.requestId?.let { id ->
                        viewModel.rejectRequest(id, player.username)
                    }
                }
            )

            Spacer(Modifier.height(10.dp))
        }
        Spacer(Modifier.height(24.dp))

        SectionTitle("FRIEND REQUESTS")

        if (state.incomingRequests.isEmpty()) {
            InfoCard(
                title = "No pending requests",
                subtitle = "Incoming friend requests will appear here."
            )
        } else {
            state.incomingRequests.forEach { request ->
                RequestCard(
                    title = request.senderUsername
                        ?: "Player ${request.senderId ?: ""}",
                    subtitle = "Rating: ${request.senderRating ?: "Unrated"}",
                    acceptLabel = "Accept",
                    rejectLabel = "Reject",
                    onAccept = {
                        request.senderUsername?.let {
                            viewModel.acceptRequest(request.id, it)
                        }
                    },
                    onReject = {
                        request.senderUsername?.let {
                            viewModel.rejectRequest(request.id, it)
                        }
                    }
                )
                Spacer(Modifier.height(10.dp))
            }
        }

        if (state.sentRequests.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            SectionTitle("SENT REQUESTS")

            state.sentRequests.forEach { request ->
                RequestCard(
                    title = request.receiverUsername
                        ?: "Player ${request.receiverId ?: ""}",
                    subtitle = "Waiting for response",
                    acceptLabel = null,
                    rejectLabel = "Cancel",
                    onAccept = {},
                    onReject = {
                        request.receiverUsername?.let {
                            viewModel.cancelRequest(request.id, it)
                        }
                    }
                )
                Spacer(Modifier.height(10.dp))
            }
        }

        state.error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = Color(0xFFEF4444), fontSize = 13.sp)
        }

        state.message?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = SetupColors.Accent, fontSize = 13.sp)
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun PlayerCard(
    username: String,
    displayName: String?,
    rating: Int?,
    status: String?,
    isBusy: Boolean,
    onAdd: () -> Unit,
    onCancel: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SetupColors.Card, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF26384D), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            displayName?.takeIf { it.isNotBlank() } ?: username,
            color = SetupColors.Text,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        Text(
            "@$username",
            color = SetupColors.Secondary,
            fontSize = 13.sp
        )

        Text(
            "Rating: ${rating ?: "Unrated"}",
            color = SetupColors.Secondary,
            fontSize = 13.sp
        )

        Spacer(Modifier.height(10.dp))

        when (status) {
            null -> {
                Text(
                    "Checking friendship status...",
                    color = SetupColors.Secondary,
                    fontSize = 12.sp
                )
            }

            "none" -> {
                ActionButton(
                    text = if (isBusy) "PLEASE WAIT..." else "ADD FRIEND",
                    enabled = !isBusy,
                    onClick = onAdd
                )
            }

            "request_sent" -> {
                ActionButton(
                    text = if (isBusy) "PLEASE WAIT..." else "CANCEL REQUEST",
                    enabled = !isBusy,
                    onClick = onCancel
                )
            }

            "request_received" -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionButton(
                        text = "ACCEPT",
                        enabled = !isBusy,
                        modifier = Modifier.weight(1f),
                        onClick = onAccept
                    )
                    ActionButton(
                        text = "REJECT",
                        enabled = !isBusy,
                        modifier = Modifier.weight(1f),
                        onClick = onReject
                    )
                }
            }

            "friends" -> {
                Text(
                    "✓ Already friends",
                    color = SetupColors.Accent,
                    fontWeight = FontWeight.SemiBold
                )
            }

            "self" -> {
                Text(
                    "This is your profile",
                    color = SetupColors.Secondary
                )
            }

            else -> {
                Text(
                    "Status: $status",
                    color = SetupColors.Secondary
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    enabled: Boolean,
    modifier: Modifier = Modifier.fillMaxWidth(),
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = SetupColors.Accent,
            contentColor = Color(0xFF07111C)
        )
    ) {
        Text(text, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FriendCard(
    friend: FriendDto,
    onInvite: () -> Unit,
    onRemove: () -> Unit,
    onDirectMessage: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SetupColors.Card, RoundedCornerShape(18.dp))
            .border(1.dp, Color(0xFF26384D), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Text(
            friend.username,
            color = SetupColors.Text,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        Text(
            "Rating: ${friend.rating ?: "Unrated"}",
            color = SetupColors.Secondary,
            fontSize = 13.sp
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onInvite,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = SetupColors.Accent,
                contentColor = Color(0xFF07111C)
            )
        ) {
            Icon(Icons.Default.SportsEsports, contentDescription = null)
            Text("  Invite to Game")
        }
        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = onDirectMessage,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = SetupColors.Accent
            )
        ) {
            Icon(
                imageVector = Icons.Default.ChatBubbleOutline,
                contentDescription = null
            )

            Spacer(Modifier.width(8.dp))

            Text("Direct Message")
        }

        Button(
            onClick = onRemove,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = SetupColors.Secondary
            )
        ) {
            Text("Remove Friend")
        }
    }
}

@Composable
private fun RequestCard(
    title: String,
    subtitle: String,
    acceptLabel: String?,
    rejectLabel: String,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SetupColors.Card, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF26384D), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Text(title, color = SetupColors.Text, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, color = SetupColors.Secondary, fontSize = 12.sp)
        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (acceptLabel != null) {
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SetupColors.Accent,
                        contentColor = Color(0xFF07111C)
                    )
                ) {
                    Text(acceptLabel)
                }
            }

            Button(
                onClick = onReject,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF26384D),
                    contentColor = SetupColors.Text
                )
            ) {
                Text(rejectLabel)
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        color = SetupColors.Secondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
    )
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun InfoCard(
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SetupColors.Card, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(title, color = SetupColors.Text, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, color = SetupColors.Secondary, fontSize = 13.sp)
    }
}
