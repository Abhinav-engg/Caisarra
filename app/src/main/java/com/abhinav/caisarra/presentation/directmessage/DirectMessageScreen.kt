package com.abhinav.caisarra.presentation.directmessage

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.caisaara.ui.theme.StatsBg
import com.caisaara.ui.theme.StatsCardBg
import com.caisaara.ui.theme.StatsCardBorder
import com.caisaara.ui.theme.StatsCyan
import com.caisaara.ui.theme.StatsTextMuted
import com.caisaara.ui.theme.StatsTextWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DirectMessageScreen(
    userId: Long,
    username: String,
    onBack: () -> Unit,
    viewModel: DirectMessageViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()

    BackHandler {
        onBack()
    }

    LaunchedEffect(userId, username) {
        viewModel.startChat(
            userId = userId,
            username = username
        )
    }

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StatsBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Header: back button, username and WebSocket status.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = StatsTextWhite
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = username,
                    color = StatsTextWhite,
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Direct message",
                    color = StatsTextMuted,
                    fontSize = 12.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (state.connected) StatsCyan
                            else StatsTextMuted
                        )
                )

                Text(
                    text = if (state.connected) "Live" else "Connecting",
                    color = if (state.connected) StatsCyan else StatsTextMuted,
                    fontSize = 11.sp
                )
            }
        }

        HorizontalDivider(
            color = StatsCardBorder,
            thickness = 1.dp
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            state = listState,
            contentPadding = PaddingValues(
                horizontal = 14.dp,
                vertical = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.loading && state.messages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = StatsCyan)
                    }
                }
            } else if (state.messages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Start the conversation",
                                color = StatsTextWhite,
                                fontFamily = JetBrainsMono,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Send a message to $username.",
                                color = StatsTextMuted,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                items(
                    items = state.messages,
                    key = { it.id }
                ) { message ->
                    val mine = message.senderId != userId

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (mine) {
                            Arrangement.End
                        } else {
                            Arrangement.Start
                        }
                    ) {
                        Column(
                            modifier = Modifier
                                .widthIn(max = 300.dp)
                                .background(
                                    color = if (mine) StatsCyan else StatsCardBg,
                                    shape = RoundedCornerShape(
                                        topStart = 18.dp,
                                        topEnd = 18.dp,
                                        bottomStart = if (mine) 18.dp else 4.dp,
                                        bottomEnd = if (mine) 4.dp else 18.dp
                                    )
                                )
                                .padding(
                                    horizontal = 14.dp,
                                    vertical = 10.dp
                                )
                        ) {
                            Text(
                                text = if (mine) "You" else username,
                                color = if (mine) StatsBg else StatsCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = message.message,
                                color = if (mine) StatsBg else StatsTextWhite,
                                fontSize = 15.sp
                            )

                            message.createdAt?.let { timestamp ->
                                Spacer(modifier = Modifier.height(5.dp))

                                Text(
                                    text = timestamp.toChatTime(),
                                    color = if (mine) {
                                        StatsBg.copy(alpha = 0.65f)
                                    } else {
                                        StatsTextMuted
                                    },
                                    fontSize = 10.sp,
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                    }
                }
            }
        }

        state.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 5.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StatsBg)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = state.draft,
                onValueChange = viewModel::onDraftChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = "Write a message...",
                        color = StatsTextMuted,
                        fontSize = 14.sp,
                        maxLines = 1
                    )
                },
                singleLine = true,
                enabled = !state.sending,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (state.draft.isNotBlank() && !state.sending) {
                            viewModel.send()
                        }
                    }
                ),
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = StatsTextWhite,
                    unfocusedTextColor = StatsTextWhite,
                    cursorColor = StatsCyan,
                    focusedContainerColor = StatsCardBg,
                    unfocusedContainerColor = StatsCardBg,
                    disabledContainerColor = StatsCardBg,
                    focusedBorderColor = StatsCyan,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                    focusedPlaceholderColor = StatsTextMuted,
                    unfocusedPlaceholderColor = StatsTextMuted
                )
            )

            IconButton(
                onClick = viewModel::send,
                enabled = state.draft.isNotBlank() && !state.sending,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        if (state.draft.isNotBlank() && !state.sending) {
                            StatsCyan
                        } else {
                            StatsCardBg
                        }
                    )
            ) {
                if (state.sending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(21.dp),
                        color = StatsTextWhite,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send message",
                        tint = if (state.draft.isNotBlank()) {
                            StatsBg
                        } else {
                            StatsTextMuted
                        }
                    )
                }
            }
        }
    }
}

private fun String.toChatTime(): String {
    return try {
        val instant = java.time.Instant.parse(this)

        SimpleDateFormat(
            "HH:mm",
            Locale.getDefault()
        ).format(Date.from(instant))
    } catch (_: Exception) {
        this
    }
}