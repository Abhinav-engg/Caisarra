package com.abhinav.caisarra.presentation.directmessage

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.abhinav.caisarra.data.remote.RetrofitInstance
import com.caisaara.ui.theme.StatsBg
import com.caisaara.ui.theme.StatsCardBg
import com.caisaara.ui.theme.StatsCardBorder
import com.caisaara.ui.theme.StatsCyan
import com.caisaara.ui.theme.StatsTextMuted
import com.caisaara.ui.theme.StatsTextWhite
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DirectMessageScreen(
    userId: Long,
    username: String,
    initialAvatarUrl: String? = null,
    onViewProfile: ((String) -> Unit)? = null,
    onBack: () -> Unit,
    viewModel: DirectMessageViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()

    BackHandler {
        onBack()
    }

    LaunchedEffect(userId, username, initialAvatarUrl) {
        viewModel.startChat(
            userId = userId,
            username = username,
            initialAvatarUrl = initialAvatarUrl
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = StatsTextWhite
                )
            }

            val recipientAvatar = state.avatarUrl ?: initialAvatarUrl
            val resolvedUrl = recipientAvatar?.trim()?.takeIf { it.isNotBlank() }?.let { url ->
                if (url.startsWith("/")) "${RetrofitInstance.BASE_URL}$url" else url
            }
            var imageFailed by remember(resolvedUrl) { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(StatsCardBg)
                    .border(1.dp, StatsCyan.copy(alpha = 0.6f), CircleShape)
                    .clickable(
                        enabled = onViewProfile != null,
                        onClick = { onViewProfile?.invoke(username) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (resolvedUrl != null && !imageFailed) {
                    AsyncImage(
                        model = resolvedUrl,
                        contentDescription = "$username's profile picture",
                        contentScale = ContentScale.Crop,
                        onError = { imageFailed = true },
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else {
                    Text(
                        text = username.trim().take(1).uppercase().ifBlank { "?" },
                        color = StatsCyan,
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        enabled = onViewProfile != null,
                        onClick = { onViewProfile?.invoke(username) }
                    )
            ) {
                Text(
                    text = username,
                    color = StatsTextWhite,
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
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
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(end = 6.dp)
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
                itemsIndexed(
                    items = state.messages,
                    key = { _, item -> item.id }
                ) { index, message ->
                    val messageDate = remember(message.createdAt) {
                        message.createdAt?.let { parseToLocalDate(it) }
                    }
                    val prevMessageDate = remember(index, state.messages.getOrNull(index - 1)?.createdAt) {
                        if (index > 0) {
                            state.messages.getOrNull(index - 1)?.createdAt?.let { parseToLocalDate(it) }
                        } else null
                    }

                    val isFirstMessageOfDay = messageDate != null && (index == 0 || messageDate != prevMessageDate)

                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isFirstMessageOfDay) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        top = if (index == 0) 0.dp else 8.dp,
                                        bottom = 12.dp
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                DateHeaderBadge(text = formatDateHeader(messageDate))
                            }
                        }

                        val mine = message.senderId != userId

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (mine) {
                                Arrangement.End
                            } else {
                                Arrangement.Start
                            },
                            verticalAlignment = Alignment.Bottom
                        ) {
                            if (!mine) {
                                val recipientAvatar = state.avatarUrl ?: initialAvatarUrl
                                val resolvedUrl = recipientAvatar?.trim()?.takeIf { it.isNotBlank() }?.let { url ->
                                    if (url.startsWith("/")) "${RetrofitInstance.BASE_URL}$url" else url
                                }
                                var msgAvatarFailed by remember(resolvedUrl) { mutableStateOf(false) }

                                Box(
                                    modifier = Modifier
                                        .padding(bottom = 2.dp, end = 6.dp)
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(StatsCardBg)
                                        .border(1.dp, StatsCardBorder, CircleShape)
                                        .clickable(
                                            enabled = onViewProfile != null,
                                            onClick = { onViewProfile?.invoke(username) }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (resolvedUrl != null && !msgAvatarFailed) {
                                        AsyncImage(
                                            model = resolvedUrl,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            onError = { msgAvatarFailed = true },
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                        )
                                    } else {
                                        Text(
                                            text = username.trim().take(1).uppercase().ifBlank { "?" },
                                            color = StatsCyan,
                                            fontFamily = JetBrainsMono,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .widthIn(max = 280.dp)
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
                enabled = !state.sending && !state.blocked,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (state.draft.isNotBlank() && !state.sending && !state.blocked) {
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
                enabled = state.draft.isNotBlank() && !state.sending && !state.blocked,
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

@Composable
private fun DateHeaderBadge(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(StatsCardBg)
            .border(
                width = 1.dp,
                color = StatsCardBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 14.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = StatsTextMuted,
            fontSize = 11.sp,
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun parseToLocalDate(isoString: String): LocalDate? {
    if (isoString.isBlank()) return null
    return try {
        Instant.parse(isoString.trim())
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    } catch (_: Exception) {
        try {
            OffsetDateTime.parse(isoString.trim())
                .atZoneSameInstant(ZoneId.systemDefault())
                .toLocalDate()
        } catch (_: Exception) {
            try {
                val normalized = isoString.trim().replace(" ", "T")
                LocalDateTime.parse(normalized)
                    .atZone(ZoneId.of("UTC"))
                    .withZoneSameInstant(ZoneId.systemDefault())
                    .toLocalDate()
            } catch (_: Exception) {
                null
            }
        }
    }
}

private fun formatDateHeader(date: LocalDate): String {
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)

    return when (date) {
        today -> "Today"
        yesterday -> "Yesterday"
        else -> {
            val pattern = if (date.year == today.year) {
                "MMM d"
            } else {
                "MMM d, yyyy"
            }
            DateTimeFormatter.ofPattern(pattern, Locale.getDefault()).format(date)
        }
    }
}

private fun String.toChatTime(): String {
    if (this.isBlank()) return ""
    return try {
        val instant = try {
            Instant.parse(this.trim())
        } catch (_: Exception) {
            try {
                OffsetDateTime.parse(this.trim()).toInstant()
            } catch (_: Exception) {
                val normalized = this.trim().replace(" ", "T")
                LocalDateTime.parse(normalized).atZone(ZoneId.of("UTC")).toInstant()
            }
        }
        val formatter = DateTimeFormatter
            .ofPattern("HH:mm", Locale.getDefault())
            .withZone(ZoneId.systemDefault())
        formatter.format(instant)
    } catch (_: Exception) {
        this
    }
}