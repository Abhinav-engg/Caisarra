package com.abhinav.caisarra.presentation.directmessage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Refresh

import androidx.compose.material3.*

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.abhinav.caisarra.core.ui.components.CaisaaraTopBar
import com.abhinav.caisarra.data.remote.dto.DirectConversationDto
import com.abhinav.caisarra.data.remote.dto.DirectPlayerDto
import com.abhinav.caisarra.data.repository.DirectMessageRepository
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.caisaara.ui.theme.StatsBg
import com.caisaara.ui.theme.StatsCardBg
import com.caisaara.ui.theme.StatsCardBorder
import com.caisaara.ui.theme.StatsCyan
import com.caisaara.ui.theme.StatsTextMuted
import com.caisaara.ui.theme.StatsTextWhite

import kotlinx.coroutines.launch

@Composable
fun DirectMessagesListScreen(
    onBack: () -> Unit,
    onOpenChat: (Long, String) -> Unit
) {
    val context = LocalContext.current.applicationContext

    val repository = remember {
        DirectMessageRepository(context)
    }

    val scope = rememberCoroutineScope()

    var conversations by remember {
        mutableStateOf<List<DirectConversationDto>>(emptyList())
    }

    var searchResults by remember {
        mutableStateOf<List<DirectPlayerDto>>(emptyList())
    }

    var query by remember {
        mutableStateOf("")
    }

    var loadingConversations by remember {
        mutableStateOf(true)
    }

    var searching by remember {
        mutableStateOf(false)
    }

    var error by remember {
        mutableStateOf<String?>(null)
    }

    var searchPerformed by remember {
        mutableStateOf(false)
    }

    fun loadConversations() {
        scope.launch {
            loadingConversations = true
            error = null

            try {
                conversations = repository.getConversations()
            } catch (e: Exception) {
                error = e.message
                    ?: "Unable to load conversations."
            } finally {
                loadingConversations = false
            }
        }
    }

    fun searchPlayers() {
        val searchQuery = query.trim()

        if (
            searchQuery.isBlank() ||
            (searchQuery.length < 2 &&
                    !searchQuery.all { it.isDigit() })
        ) {
            error = "Enter at least two characters or a numeric user ID."
            return
        }

        scope.launch {
            searching = true
            searchPerformed = true
            error = null

            try {
                searchResults =
                    repository.searchPlayers(searchQuery)
            } catch (e: Exception) {
                searchResults = emptyList()
                error = e.message
                    ?: "Unable to search players."
            } finally {
                searching = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadConversations()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StatsBg)
            .statusBarsPadding()
    ) {
        CaisaaraTopBar(
            title = "DIRECT MESSAGES",
            onBack = onBack,
            backgroundColor = StatsBg,
            titleColor = StatsTextWhite,
            iconTint = StatsTextWhite,
            titleFontSize = 16.sp,
            actions = {
                IconButton(
                    onClick = {
                        loadConversations()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh conversations",
                        tint = StatsCyan
                    )
                }
            }
        )

        HorizontalDivider(
            color = StatsCardBorder
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 16.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    error = null
                },
                modifier = Modifier
                    .weight(1f)
                    .height(60.dp),
                singleLine = true,
                maxLines = 1,
                textStyle = TextStyle(
                    color = StatsTextWhite,
                    fontSize = 15.sp
                ),
                placeholder = {
                    Text(
                        text = "Search players or ID",
                        color = StatsTextMuted,
                        fontSize = 14.sp,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = StatsCyan,
                        modifier = Modifier.size(22.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        searchPlayers()
                    }
                ),
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = StatsTextWhite,
                    unfocusedTextColor = StatsTextWhite,
                    focusedBorderColor = StatsCyan,
                    unfocusedBorderColor = StatsCardBorder,
                    cursorColor = StatsCyan,
                    focusedLeadingIconColor = StatsCyan,
                    unfocusedLeadingIconColor = StatsCyan
                )
            )

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(StatsCyan)
                    .clickable(
                        enabled = !searching,
                        onClick = { searchPlayers() }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (searching) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = StatsBg,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search players",
                        tint = StatsBg,
                        modifier = Modifier.size(25.dp)
                    )
                }
            }
        }

        error?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 4.dp
                )
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (searchPerformed) {
                item {
                    SectionTitle("SEARCH RESULTS")
                }

                if (
                    searchResults.isEmpty() &&
                    !searching
                ) {
                    item {
                        Text(
                            text = "No players found. Try another username.",
                            color = StatsTextMuted,
                            modifier = Modifier.padding(
                                vertical = 12.dp
                            )
                        )
                    }
                }

                items(
                    items = searchResults,
                    key = { "player_${it.userId}" }
                ) { player ->
                    DirectPlayerRow(
                        name = player.displayName
                            ?.takeIf { it.isNotBlank() }
                            ?: player.username,
                        username = player.username,
                        subtitle = player.rating?.let {
                            "Rating: $it"
                        } ?: "Player",
                        onClick = {
                            onOpenChat(
                                player.userId,
                                player.username
                            )
                        }
                    )
                }

                item {
                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    HorizontalDivider(
                        color = StatsCardBorder
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )
                }
            }

            item {
                SectionTitle("YOUR CONVERSATIONS")
            }

            if (loadingConversations) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = StatsCyan
                        )
                    }
                }
            } else if (conversations.isEmpty()) {
                item {
                    Text(
                        text = "No conversations yet. Search for a player above to start chatting.",
                        color = StatsTextMuted,
                        modifier = Modifier.padding(
                            vertical = 12.dp
                        )
                    )
                }
            }

            items(
                items = conversations,
                key = { "conversation_${it.userId}" }
            ) { conversation ->
                DirectPlayerRow(
                    name = conversation.displayName
                        ?.takeIf { it.isNotBlank() }
                        ?: conversation.username,
                    username = conversation.username,
                    subtitle = conversation.lastMessage
                        ?: "Start a conversation",
                    unreadCount = conversation.unreadCount,
                    onClick = {
                        onOpenChat(
                            conversation.userId,
                            conversation.username
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(
    title: String
) {
    Text(
        text = title,
        fontFamily = JetBrainsMono,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        color = StatsTextMuted,
        modifier = Modifier.padding(
            top = 10.dp,
            bottom = 4.dp
        )
    )
}

@Composable
private fun DirectPlayerRow(
    name: String,
    username: String,
    subtitle: String,
    unreadCount: Int = 0,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(StatsCardBg)
            .border(
                width = 1.dp,
                color = StatsCardBorder,
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(StatsBg)
                .border(
                    width = 1.dp,
                    color = StatsCyan,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name
                    .take(1)
                    .uppercase(),
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Bold,
                color = StatsCyan,
                fontSize = 20.sp
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = name,
                color = StatsTextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "@$username",
                color = StatsCyan,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = subtitle,
                color = StatsTextMuted,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (unreadCount > 0) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(StatsCyan)
                    .padding(
                        horizontal = 8.dp,
                        vertical = 4.dp
                    )
            ) {
                Text(
                    text = unreadCount.toString(),
                    color = Color(0xFF071017),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        } else {
            Icon(
                imageVector = Icons.Default.Chat,
                contentDescription = "Open chat",
                tint = StatsCyan
            )
        }
    }
}