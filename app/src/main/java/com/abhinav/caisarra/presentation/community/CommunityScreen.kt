
package com.abhinav.caisarra.presentation.community

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.abhinav.caisarra.core.ui.components.MainTabTopBar
import com.abhinav.caisarra.data.remote.dto.CommunityCommentDto
import com.abhinav.caisarra.data.remote.dto.CommunityPostDto
import com.caisaara.ui.theme.StatsCyan
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.layout.heightIn


private val CommunityBackground = Color(0xFF080D13)
private val CommunityCard = Color(0xFF151F2B)
private val CommunityText = Color(0xFFF4F6FA)
private val CommunityMuted = Color(0xFFADB8C6)
private val CommunityGreen = Color(0xFF10B981)
private val CommunityPurple = Color(0xFF7054AE)
private val CommunityOutline = Color(0xFF465264)
private val CommunityDanger = Color(0xFFFF8989)

@Composable
fun CommunityScreen(
    viewModel: CommunityViewModel,
    isRegisteredUser: Boolean,
    onBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var postText by remember { mutableStateOf("") }
    var showGamePicker by remember { mutableStateOf(false) }
    var gameCaption by remember { mutableStateOf("") }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CommunityBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .clickable(
                interactionSource = remember {
                    androidx.compose.foundation.interaction.MutableInteractionSource()
                },
                indication = null
            ) {
                focusManager.clearFocus()
                keyboardController?.hide()
            }
    ) {
        MainTabTopBar(
            title = "COMMUNITY",
            icon = Icons.Filled.Groups,
            trailingContent = {
                IconButton(
                    onClick = viewModel::refresh,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Refresh",
                        tint = StatsCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                top = 0.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isRegisteredUser) {
                item(key = "community_composer") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                CommunityCard,
                                RoundedCornerShape(20.dp)
                            )
                            .padding(14.dp)
                    ) {
                        Text(
                            "Share with the community",
                            color = CommunityText,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "Your chess moments, thoughts and experiences.",
                            color = CommunityMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = postText,
                            onValueChange = {
                                if (it.length <= 2000) postText = it
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(126.dp),
                            placeholder = {
                                Text(
                                    "What's happening on your chessboard?",
                                    color = CommunityMuted.copy(alpha = 0.8f)
                                )
                            },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = CommunityText,
                                fontSize = 16.sp
                            ),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences
                            ),
                            minLines = 3,
                            maxLines = 5,
                            enabled = !state.isPublishing,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = CommunityText,
                                unfocusedTextColor = CommunityText,
                                disabledTextColor = CommunityMuted,
                                cursorColor = CommunityPurple,
                                focusedBorderColor = CommunityPurple,
                                unfocusedBorderColor = CommunityOutline,
                                focusedContainerColor = CommunityBackground,
                                unfocusedContainerColor = CommunityBackground,
                                disabledContainerColor = CommunityBackground,
                                focusedPlaceholderColor = CommunityMuted,
                                unfocusedPlaceholderColor = CommunityMuted
                            )
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${postText.length}/2000",
                                color = CommunityMuted,
                                fontSize = 12.sp
                            )

                            Spacer(Modifier.weight(1f))

                            Button(
                                onClick = {
                                    viewModel.createPost(postText.trim())
                                    postText = ""
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                },
                                enabled = postText.isNotBlank() && !state.isPublishing,
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CommunityPurple,
                                    contentColor = Color.White,
                                    disabledContainerColor = CommunityPurple.copy(alpha = 0.3f),
                                    disabledContentColor = CommunityMuted
                                )
                            ) {
                                Text(
                                    if (state.isPublishing) "Posting..." else "Post",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Button(
                            onClick = {
                                gameCaption = ""
                                viewModel.loadShareableGames()
                                showGamePicker = true
                            },
                            enabled = !state.isPublishing,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CommunityGreen,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                "♟  Share a Game",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                item(key = "community_sign_in_notice") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = CommunityCard,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            "Sign in to publish posts, react and comment.",
                            color = CommunityMuted,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            state.error?.let { message ->
                item(key = "community_error") {
                    Text(
                        message,
                        color = CommunityDanger,
                        fontSize = 13.sp
                    )
                }
            }

            state.info?.let { message ->
                item(key = "community_info") {
                    Text(
                        message,
                        color = CommunityGreen,
                        fontSize = 13.sp
                    )
                }
            }

            item(key = "community_posts_heading") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Community posts",
                        color = CommunityText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.weight(1f))

                    if (state.isLoading && state.posts.isNotEmpty()) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = CommunityGreen,
                            strokeWidth = 2.dp
                        )
                    }
                }
            }

            if (state.isLoading && state.posts.isEmpty()) {
                item(key = "community_loading") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = CommunityGreen)
                    }
                }
            } else if (state.posts.isEmpty()) {
                item(key = "community_empty") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("♟", fontSize = 42.sp)

                        Text(
                            "No posts yet",
                            color = CommunityText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 10.dp)
                        )

                        Text(
                            "Be the first to share a chess moment.",
                            color = CommunityMuted,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 5.dp)
                        )
                    }
                }
            } else {
                items(
                    items = state.posts,
                    key = { it.id }
                ) { post ->
                    CommunityPostCard(
                        post = post,
                        isRegisteredUser = isRegisteredUser,
                        onReact = { reaction ->
                            viewModel.react(post.id, reaction)
                        },
                        onDelete = {
                            viewModel.deletePost(post.id)
                        },
                        onComments = {
                            viewModel.openComments(post.id)
                        }
                    )
                }

                if (state.nextBeforeId != null) {
                    item(key = "community_load_more") {
                        TextButton(
                            onClick = viewModel::loadMore,
                            enabled = !state.isLoadingMore,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (state.isLoadingMore) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = CommunityGreen,
                                    strokeWidth = 2.dp
                                )
                                Text("  Loading...", color = CommunityGreen)
                            } else {
                                Text(
                                    "Load more posts",
                                    color = CommunityGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }


    if (showGamePicker) {
        AlertDialog(
            onDismissRequest = {
                if (!state.isPublishing) {
                    showGamePicker = false
                }
            },
            containerColor = CommunityCard,
            title = {
                Text(
                    text = "Share a completed game",
                    color = CommunityText,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                ) {
                    Text(
                        text = "Add a caption (optional)",
                        color = CommunityMuted,
                        fontSize = 13.sp
                    )

                    OutlinedTextField(
                        value = gameCaption,
                        onValueChange = {
                            if (it.length <= 2000) {
                                gameCaption = it
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        placeholder = {
                            Text("Tell everyone about this game")
                        },
                        enabled = !state.isPublishing,
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CommunityText,
                            unfocusedTextColor = CommunityText,
                            cursorColor = CommunityGreen,
                            focusedBorderColor = CommunityGreen,
                            unfocusedBorderColor = CommunityOutline,
                            focusedContainerColor = CommunityBackground,
                            unfocusedContainerColor = CommunityBackground
                        )
                    )

                    when {
                        state.isLoadingGames -> {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(16.dp),
                                color = CommunityGreen
                            )
                        }

                        state.shareableGames.isEmpty() -> {
                            Text(
                                text = "No completed online games found. " +
                                        "Play and finish a game first.",
                                color = CommunityMuted,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }

                        else -> {
                            Text(
                                text = "SELECT A GAME",
                                color = CommunityMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(
                                    top = 8.dp,
                                    bottom = 6.dp
                                )
                            )

                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(
                                    items = state.shareableGames,
                                    key = { it.id }
                                ) { game ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(CommunityBackground)
                                            .clickable(
                                                enabled = !state.isPublishing
                                            ) {
                                                viewModel.shareGame(
                                                    gameId = game.id,
                                                    caption = gameCaption
                                                )
                                                showGamePicker = false
                                            }
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = "${game.whiteUsername ?: "White"} " +
                                                    "vs ${game.blackUsername ?: "Black"}",
                                            color = CommunityText,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Text(
                                            text = "Result: " +
                                                    (game.result ?: "Unknown"),
                                            color = CommunityGreen,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )

                                        Text(
                                            text = "${game.timeControlMode} • " +
                                                    "${game.timeControlMinutes} min",
                                            color = CommunityMuted,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(top = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showGamePicker = false
                    },
                    enabled = !state.isPublishing
                ) {
                    Text("Cancel", color = CommunityMuted)
                }
            }
        )
    }

    if (state.commentsPostId != null) {
        CommunityCommentsDialog(
            postId = state.commentsPostId!!,
            viewModel = viewModel,
            isRegisteredUser = isRegisteredUser,
            onDismiss = viewModel::closeComments
        )
    }
}

@Composable
private fun CommunityAvatar(
    avatarUrl: String?,
    displayName: String?,
    username: String,
    size: androidx.compose.ui.unit.Dp = 46.dp
) {
    val initials = (displayName
        ?.takeIf { it.isNotBlank() }
        ?: username)
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "?" }

    val validUrl = avatarUrl
        ?.trim()
        ?.takeIf {
            it.startsWith("https://", ignoreCase = true) ||
                    it.startsWith("http://", ignoreCase = true)
        }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(CommunityPurple),
        contentAlignment = Alignment.Center
    ) {
        if (validUrl != null) {
            AsyncImage(
                model = validUrl,
                contentDescription = "Avatar of $username",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        } else {
            Text(
                text = initials,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun CommunityPostCard(
    post: CommunityPostDto,
    isRegisteredUser: Boolean,
    onReact: (String) -> Unit,
    onDelete: () -> Unit,
    onComments: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CommunityCard, RoundedCornerShape(22.dp))
            .padding(horizontal = 16.dp, vertical = 17.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CommunityAvatar(
                avatarUrl = post.author.avatarUrl,
                displayName = post.author.displayName,
                username = post.author.username
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = post.author.displayName
                        ?.takeIf { it.isNotBlank() }
                        ?: post.author.username,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CommunityText
                )

                Text(
                    text = "@${post.author.username}",
                    style = MaterialTheme.typography.bodySmall,
                    color = CommunityMuted
                )

                Text(
                    text = formatCommunityDate(post.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = CommunityMuted.copy(alpha = 0.8f)
                )
            }
        }


        if (post.body.isNotBlank()) {
            Text(
                text = post.body,
                style = MaterialTheme.typography.bodyLarge,
                color = CommunityText,
                lineHeight = 25.sp,
                modifier = Modifier.padding(
                    top = 17.dp,
                    bottom = 14.dp
                )
            )
        }

        if (post.kind == "game" || post.kind == "bot_game") {
            post.game?.let { game ->
                CommunityGamePostCard(
                    game = game,
                    fen = post.fen
                )
            }
        }
        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    CommunityBackground.copy(alpha = 0.75f),
                    RoundedCornerShape(15.dp)
                )
                .padding(horizontal = 2.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                Triple("like", "👍", "Like"),
                Triple("love", "❤️", "Love"),
                Triple("fire", "🔥", "Fire"),
                Triple("wow", "😮", "Wow")
            ).forEach { (reaction, emoji, label) ->
                TextButton(
                    onClick = { onReact(reaction) },
                    enabled = isRegisteredUser,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 5.dp,
                        vertical = 4.dp
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = emoji,
                            fontSize = 19.sp
                        )
                        Text(
                            text = "${post.reactions[reaction] ?: 0}",
                            color = if (post.myReaction == reaction) {
                                CommunityGreen
                            } else {
                                CommunityText
                            },
                            fontSize = 12.sp,
                            fontWeight = if (post.myReaction == reaction) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Medium
                            }
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onComments,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 4.dp,
                    vertical = 6.dp
                )
            ) {
                Text(
                    text = "Comments (${post.commentCount})",
                    color = CommunityGreen,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.weight(1f))

            if (isRegisteredUser) {
                TextButton(
                    onClick = onDelete,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 4.dp,
                        vertical = 6.dp
                    )
                ) {
                    Text(
                        text = "Delete",
                        color = CommunityDanger,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CommunityCommentsDialog(
    postId: Long,
    viewModel: CommunityViewModel,
    isRegisteredUser: Boolean,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var comments by remember(postId) {
        mutableStateOf<List<CommunityCommentDto>>(emptyList())
    }
    var nextAfterId by remember(postId) {
        mutableStateOf<Long?>(null)
    }
    var commentText by remember(postId) {
        mutableStateOf("")
    }
    var isLoading by remember(postId) {
        mutableStateOf(true)
    }
    var error by remember(postId) {
        mutableStateOf<String?>(null)
    }

    fun loadComments() {
        scope.launch {
            isLoading = true
            error = null

            try {
                val result = viewModel.loadComments(postId)
                comments = result.comments
                nextAfterId = result.nextAfterId
            } catch (e: Exception) {
                error = e.message ?: "Unable to load comments."
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(postId) {
        loadComments()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Comments",
                color = CommunityText,
                fontWeight = FontWeight.Bold
            )
        },
        containerColor = CommunityCard,
        textContentColor = CommunityText,
        text = {
            Column {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = CommunityGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }

                error?.let {
                    Text(it, color = CommunityDanger)
                }

                comments.forEach { comment ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        CommunityAvatar(
                            avatarUrl = comment.author.avatarUrl,
                            displayName = comment.author.displayName,
                            username = comment.author.username,
                            size = 34.dp
                        )

                        Column(
                            modifier = Modifier.padding(start = 10.dp)
                        ) {
                            Text(
                                text = comment.author.displayName
                                    ?.takeIf { it.isNotBlank() }
                                    ?: comment.author.username,
                                style = MaterialTheme.typography.titleSmall,
                                color = CommunityText,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = comment.body,
                                color = CommunityText
                            )
                            Text(
                                text = formatCommunityDate(comment.createdAt),
                                style = MaterialTheme.typography.bodySmall,
                                color = CommunityMuted
                            )
                        }
                    }
                }

                if (nextAfterId != null) {
                    TextButton(
                        onClick = {
                            scope.launch {
                                try {
                                    val result = viewModel.loadComments(
                                        postId,
                                        nextAfterId
                                    )
                                    comments = comments + result.comments
                                    nextAfterId = result.nextAfterId
                                } catch (e: Exception) {
                                    error = e.message ?: "Unable to load more."
                                }
                            }
                        }
                    ) {
                        Text("Load older comments", color = CommunityGreen)
                    }
                }

                if (isRegisteredUser) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = {
                            if (it.length <= 500) commentText = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "Write a comment",
                                color = CommunityMuted
                            )
                        },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = CommunityText
                        ),
                        supportingText = {
                            Text(
                                "${commentText.length}/500",
                                color = CommunityMuted
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CommunityText,
                            unfocusedTextColor = CommunityText,
                            cursorColor = CommunityPurple,
                            focusedBorderColor = CommunityPurple,
                            unfocusedBorderColor = CommunityOutline,
                            focusedContainerColor = CommunityBackground,
                            unfocusedContainerColor = CommunityBackground,
                            focusedPlaceholderColor = CommunityMuted,
                            unfocusedPlaceholderColor = CommunityMuted
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        },
        confirmButton = {
            if (isRegisteredUser) {
                TextButton(
                    enabled = commentText.isNotBlank(),
                    onClick = {
                        scope.launch {
                            try {
                                viewModel.addComment(postId, commentText.trim())
                                commentText = ""
                                loadComments()
                            } catch (e: Exception) {
                                error = e.message ?: "Unable to add comment."
                            }
                        }
                    }
                ) {
                    Text("Send", color = CommunityGreen)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = CommunityMuted)
            }
        }
    )
}

private fun formatCommunityDate(value: String): String {
    return try {
        DateTimeFormatter.ofPattern("dd MMM, HH:mm")
            .withZone(ZoneId.systemDefault())
            .format(Instant.parse(value))
    } catch (_: Exception) {
        value
    }
}
