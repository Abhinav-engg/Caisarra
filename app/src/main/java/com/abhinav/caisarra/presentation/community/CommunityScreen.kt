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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import com.abhinav.caisarra.data.remote.dto.CommunityCommentDto
import com.abhinav.caisarra.data.remote.dto.CommunityPostDto
import kotlinx.coroutines.launch
import java.util.Base64
import org.json.JSONObject
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import com.abhinav.caisarra.R

private val CommunityBackground = Color(0xFF080D13)
private val CommunityCard = Color(0xFF101923)
private val CommunityText = Color(0xFFF4F6FA)
private val CommunityMuted = Color(0xFFADB8C6)
private val CommunityGreen = Color(0xFF00D8E8)
private val CommunityPurple = Color(0xFF00C8D8)
private val CommunityOutline = Color(0xFF233542)
private val CommunityDanger = Color(0xFFFF8989)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    viewModel: CommunityViewModel,
    isRegisteredUser: Boolean,
    currentUsername: String? = null,
    currentAvatarUrl: String? = null,
    currentDisplayName: String? = null,
    accessToken: String? = null,
    onBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val resolvedUsername = currentUsername ?: usernameFromJwt(accessToken)
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var postText by remember { mutableStateOf("") }
    var showWritePost by remember { mutableStateOf(false) }
    var showGamePicker by remember { mutableStateOf(false) }
    var gameCaption by remember { mutableStateOf("") }
    var publishingFromWriteScreen by remember { mutableStateOf(false) }
    var selectedPost by remember { mutableStateOf<CommunityPostDto?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.info, state.error) {
        val message = state.info ?: state.error
        if (!message.isNullOrBlank()) snackbarHostState.showSnackbar(message)
    }

    LaunchedEffect(state.info) {
        if (publishingFromWriteScreen && state.info == "Post published successfully.") {
            publishingFromWriteScreen = false
            postText = ""
            showWritePost = false
        }
    }

    if (selectedPost != null) {
        CommunityPostDetailScreen(
            post = state.posts.firstOrNull { it.id == selectedPost!!.id } ?: selectedPost!!,
            viewModel = viewModel,
            isRegisteredUser = isRegisteredUser,
            currentUsername = resolvedUsername,
            onBack = { selectedPost = null },
            onReact = { reaction -> selectedPost?.let { viewModel.react(it.id, reaction) } },
            onBookmark = { selectedPost?.let { viewModel.toggleBookmark(it.id) } },
            onDeletePost = {
                selectedPost?.let { viewModel.deletePost(it.id) }
                selectedPost = null
            }
        )
    } else if (showWritePost && isRegisteredUser) {
        CommunityWritePostScreen(
            username = resolvedUsername,
            postText = postText,
            onPostTextChange = { if (it.count { ch -> !ch.isWhitespace() } <= 2000) postText = it },
            isPublishing = state.isPublishing,
            error = state.error,
            onBack = {
                focusManager.clearFocus()
                keyboardController?.hide()
                showWritePost = false
            },
            onPost = {
                val body = postText.trim()
                if (body.isNotBlank() && !state.isPublishing) {
                    publishingFromWriteScreen = true
                    viewModel.createPost(body)
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
            }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CommunityBackground)
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.showingBookmarks) {
                    TextButton(
                        onClick = viewModel::showFeed,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Text("‹", color = CommunityGreen, fontSize = 30.sp, lineHeight = 30.sp)
                    }
                }
                Text(
                    text = if (state.showingBookmarks) "SAVED POSTS" else "COMMUNITY",
                    modifier = Modifier.weight(1f),
                    color = CommunityText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                if (isRegisteredUser && !state.showingBookmarks) {
                    TextButton(onClick = viewModel::loadBookmarks) {
                        Text("▣ Saved", color = CommunityGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }

            PullToRefreshBox(
                isRefreshing = state.isLoading && state.posts.isNotEmpty(),
                onRefresh = viewModel::refresh,
                modifier = Modifier.weight(1f)
            ) {
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
                    if (isRegisteredUser && !state.showingBookmarks) {

                        item(key = "community_composer") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        CommunityCard,
                                        RoundedCornerShape(28.dp)
                                    )
                                    .padding(18.dp)
                            ) {


                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    CommunityAvatar(
                                        avatarUrl = currentAvatarUrl,
                                        displayName = currentDisplayName,
                                        username = resolvedUsername.orEmpty()
                                            .ifBlank { "You" },
                                        size = 48.dp
                                    )

                                    Text(
                                        text = "What's your next thought?",
                                        color = CommunityMuted,
                                        fontSize = 17.sp,
                                        lineHeight = 24.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }


                                HorizontalDivider(
                                    modifier = Modifier.padding(
                                        horizontal = 8.dp,
                                        vertical = 8.dp
                                    ),
                                    thickness = 1.dp,
                                    color = CommunityMuted.copy(alpha = 0.16f)
                                )


                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "A move. A lesson. A good game.",
                                        color = CommunityMuted,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Button(
                                        onClick = {
                                            showWritePost = true
                                        },
                                        enabled = !state.isPublishing,
                                        shape = RoundedCornerShape(17.dp),
                                        contentPadding = PaddingValues(
                                            horizontal = 16.dp,
                                            vertical = 14.dp
                                        ),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = CommunityGreen,
                                            contentColor = Color(0xFF002128)
                                        )
                                    ) {
                                        Text(
                                            text = "＋ Write a post",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
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
                                        .padding(top = 20.dp)
                                        .heightIn(min = 60.dp),
                                    shape = RoundedCornerShape(20.dp),
                                    contentPadding = PaddingValues(
                                        horizontal = 18.dp,
                                        vertical = 16.dp
                                    ),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CommunityGreen,
                                        contentColor = Color(0xFF002128)
                                    )
                                ) {
                                    Text(
                                        text = "♟  Share a Game",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }


                    } else if (!isRegisteredUser && !state.showingBookmarks) {
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

                    item(key = "community_posts_heading") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "From the community",
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
                                modifier = Modifier.fillMaxWidth().height(220.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = CommunityGreen)
                            }
                        }
                    } else if (state.posts.isEmpty()) {
                        item(key = "community_empty") {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
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
                        items(items = state.posts, key = { it.id }) { post ->
                            CommunityPostCard(
                                post = post,
                                isRegisteredUser = isRegisteredUser,
                                canDelete = isRegisteredUser &&
                                        !resolvedUsername.isNullOrBlank() &&
                                        post.author.username.equals(resolvedUsername, ignoreCase = true),
                                onReact = { reaction -> viewModel.react(post.id, reaction) },
                                onDelete = { viewModel.deletePost(post.id) },
                                onComments = { selectedPost = post },
                                onBookmark = { viewModel.toggleBookmark(post.id) },
                                onOpenPost = { selectedPost = post }
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
                                        Text("Load more posts", color = CommunityGreen)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showGamePicker) {
        AlertDialog(
            onDismissRequest = { if (!state.isPublishing) showGamePicker = false },
            containerColor = CommunityCard,
            title = {
                Text("Share a completed game", color = CommunityText, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp)
                ) {
                    Text("Add a caption (optional)", color = CommunityMuted, fontSize = 13.sp)
                    OutlinedTextField(
                        value = gameCaption,
                        onValueChange = { if (it.length <= 2000) gameCaption = it },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        placeholder = { Text("Tell everyone about this game") },
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
                                modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp),
                                color = CommunityGreen
                            )
                        }
                        state.shareableGames.isEmpty() -> {
                            Text(
                                "No completed online games found. Play and finish a game first.",
                                color = CommunityMuted,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                        else -> {
                            Text(
                                "SELECT A GAME",
                                color = CommunityMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp, bottom = 6.dp)
                            )
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(items = state.shareableGames, key = { it.id }) { game ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(CommunityBackground)
                                            .clickable(enabled = !state.isPublishing) {
                                                viewModel.shareGame(gameId = game.id, caption = gameCaption)
                                                showGamePicker = false
                                            }
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            "${game.whiteUsername ?: "White"} vs ${game.blackUsername ?: "Black"}",
                                            color = CommunityText,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "Result: ${game.result ?: "Unknown"}",
                                            color = CommunityGreen,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                        Text(
                                            "${game.timeControlMode} • ${game.timeControlMinutes} min",
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
                    onClick = { showGamePicker = false },
                    enabled = !state.isPublishing
                ) { Text("Cancel", color = CommunityMuted) }
            }
        )
    }

    if (state.commentsPostId != null) {
        CommunityCommentsDialog(
            postId = state.commentsPostId!!,
            viewModel = viewModel,
            isRegisteredUser = isRegisteredUser,
            currentUsername = resolvedUsername,
            canDeletePostComments = state.posts.firstOrNull { it.id == state.commentsPostId }
                ?.author?.username?.equals(resolvedUsername, ignoreCase = true) == true,
            onDismiss = viewModel::closeComments
        )
    }
}


@Composable
private fun CommunityPostDetailScreen(
    post: CommunityPostDto,
    viewModel: CommunityViewModel,
    isRegisteredUser: Boolean,
    currentUsername: String?,
    onBack: () -> Unit,
    onReact: (String) -> Unit,
    onBookmark: () -> Unit,
    onDeletePost: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var comments by remember(post.id) { mutableStateOf<List<CommunityCommentDto>>(emptyList()) }
    var nextAfterId by remember(post.id) { mutableStateOf<Long?>(null) }
    var commentText by remember(post.id) { mutableStateOf("") }
    var isLoading by remember(post.id) { mutableStateOf(true) }
    var isSending by remember(post.id) { mutableStateOf(false) }
    var isLoadingOlder by remember(post.id) { mutableStateOf(false) }
    var error by remember(post.id) { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(post.id) {
        try {
            val result = viewModel.loadComments(post.id)
            comments = result.comments
            nextAfterId = result.nextAfterId
        } catch (e: Exception) {
            error = e.message ?: "Unable to load comments."
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(error) {
        error?.let { snackbarHostState.showSnackbar(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CommunityBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onBack,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("‹", color = CommunityGreen, fontSize = 32.sp, lineHeight = 32.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("POST", color = CommunityText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Community conversation", color = CommunityMuted, fontSize = 12.sp)
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "detail_post_${post.id}") {
                CommunityPostCard(
                    post = post,
                    isRegisteredUser = isRegisteredUser,
                    canDelete = isRegisteredUser &&
                            !currentUsername.isNullOrBlank() &&
                            post.author.username.equals(currentUsername, ignoreCase = true),
                    onReact = onReact,
                    onDelete = onDeletePost,
                    onComments = {},
                    onBookmark = onBookmark,
                    onOpenPost = {}
                )
            }

            item(key = "detail_comments_header_${post.id}") {
                Text(
                    text = "Comments ${comments.size.takeIf { it > 0 }?.let { "· $it loaded" } ?: ""}",
                    color = CommunityText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                )
            }

            if (isLoading && comments.isEmpty()) {
                item(key = "detail_comments_loading") {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(140.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = CommunityGreen) }
                }
            } else if (comments.isEmpty()) {
                item(key = "detail_comments_empty") {
                    Text(
                        "No comments yet. Start the conversation.",
                        color = CommunityMuted,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            } else {
                items(comments, key = { "detail_comment_${it.id}" }) { comment ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(CommunityCard)
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        CommunityAvatar(
                            avatarUrl = comment.author.avatarUrl,
                            displayName = comment.author.displayName,
                            username = comment.author.username,
                            size = 38.dp
                        )
                        Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    comment.author.displayName?.takeIf { it.isNotBlank() }
                                        ?: comment.author.username,
                                    color = CommunityText,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    formatCommunityDate(comment.createdAt),
                                    color = CommunityMuted,
                                    fontSize = 10.sp
                                )
                            }
                            Text(
                                comment.body,
                                color = CommunityText,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(top = 5.dp)
                            )
                            if (isRegisteredUser && (
                                        post.author.username.equals(currentUsername, ignoreCase = true) ||
                                                (!currentUsername.isNullOrBlank() &&
                                                        comment.author.username.equals(currentUsername, ignoreCase = true))
                                        )) {
                                Text(
                                    "Delete",
                                    color = CommunityDanger,
                                    fontSize = 12.sp,
                                    modifier = Modifier
                                        .clickable {
                                            scope.launch {
                                                try {
                                                    viewModel.deleteComment(comment.id)
                                                    comments = comments.filterNot { it.id == comment.id }
                                                    viewModel.refresh()
                                                    snackbarHostState.showSnackbar("Comment deleted")
                                                } catch (e: Exception) {
                                                    snackbarHostState.showSnackbar(
                                                        e.message ?: "Unable to delete comment"
                                                    )
                                                }
                                            }
                                        }
                                        .padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (nextAfterId != null) {
                item(key = "detail_load_older_comments") {
                    TextButton(
                        enabled = !isLoadingOlder,
                        onClick = {
                            scope.launch {
                                isLoadingOlder = true
                                try {
                                    val result = viewModel.loadComments(post.id, nextAfterId)
                                    comments = (comments + result.comments).distinctBy { it.id }
                                    nextAfterId = result.nextAfterId
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar(
                                        e.message ?: "Unable to load older comments"
                                    )
                                } finally {
                                    isLoadingOlder = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (isLoadingOlder) "Loading comments…" else "Load older comments",
                            color = CommunityGreen
                        )
                    }
                }
            }
        }

        if (isRegisteredUser) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CommunityCard)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { if (it.length <= 500) commentText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Write a reply…", color = CommunityMuted) },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = CommunityText),
                    maxLines = 4,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CommunityText,
                        unfocusedTextColor = CommunityText,
                        cursorColor = CommunityGreen,
                        focusedBorderColor = CommunityGreen,
                        unfocusedBorderColor = CommunityOutline,
                        focusedContainerColor = CommunityBackground,
                        unfocusedContainerColor = CommunityBackground
                    ),
                    enabled = !isSending
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    enabled = commentText.isNotBlank() && !isSending,
                    onClick = {
                        scope.launch {
                            isSending = true
                            try {
                                val created = viewModel.addComment(post.id, commentText.trim())
                                comments = (comments + created).distinctBy { it.id }
                                commentText = ""
                                viewModel.refresh()
                                snackbarHostState.showSnackbar("Reply posted")
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar(e.message ?: "Unable to post reply")
                            } finally {
                                isSending = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CommunityGreen,
                        contentColor = Color(0xFF002128)
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp)
                ) {
                    Text(if (isSending) "…" else "Reply", fontWeight = FontWeight.Bold)
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CommunityWritePostScreen(
    username: String?,
    postText: String,
    onPostTextChange: (String) -> Unit,
    isPublishing: Boolean,
    error: String?,
    onBack: () -> Unit,
    onPost: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val displayUsername = username?.takeIf { it.isNotBlank() } ?: "chess_player"
    val displayName = displayUsername
        .replace(Regex("[._-]+"), " ")
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CommunityBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        onBack()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("‹", color = CommunityText, fontSize = 32.sp, lineHeight = 32.sp)
            }
            Text(
                text = "WRITE A POST",
                modifier = Modifier.weight(1f),
                color = CommunityText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.size(36.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(CommunityCard)
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CommunityAvatar(
                    avatarUrl = null,
                    displayName = displayName,
                    username = displayUsername,
                    size = 42.dp
                )
                Column(
                    modifier = Modifier.weight(1f).padding(start = 10.dp)
                ) {
                    Text(displayName, color = CommunityText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text("@$displayUsername", color = CommunityMuted, fontSize = 12.sp)
                }
            }

            OutlinedTextField(
                value = postText,
                onValueChange = onPostTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .heightIn(min = 110.dp, max = 220.dp),
                placeholder = {
                    Text(
                        "What's on your mind?",
                        color = CommunityMuted.copy(alpha = 0.8f),
                        fontSize = 15.sp
                    )
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = CommunityText,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                enabled = !isPublishing,
                maxLines = 8,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = CommunityText,
                    unfocusedTextColor = CommunityText,
                    disabledTextColor = CommunityMuted,
                    cursorColor = CommunityGreen,
                    focusedBorderColor = CommunityOutline,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = CommunityCard,
                    unfocusedContainerColor = CommunityCard,
                    disabledContainerColor = CommunityCard,
                    focusedPlaceholderColor = CommunityMuted,
                    unfocusedPlaceholderColor = CommunityMuted
                )
            )

            val charCount = postText.count { !it.isWhitespace() }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("$charCount / 2000 characters", color = CommunityMuted, fontSize = 11.sp)
                Spacer(Modifier.weight(1f))
                Text("${(2000 - charCount).coerceAtLeast(0)} left", color = CommunityGreen, fontSize = 11.sp)
            }

            error?.let {
                Text(
                    text = it,
                    color = CommunityDanger,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "A move. A lesson.",
                    color = CommunityMuted.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        onPost()
                    },
                    enabled = postText.isNotBlank() && !isPublishing,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 22.dp,
                        vertical = 10.dp
                    ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CommunityGreen,
                        contentColor = Color(0xFF001318),
                        disabledContainerColor = CommunityGreen.copy(alpha = 0.35f),
                        disabledContentColor = CommunityMuted
                    )
                ) {
                    Text(
                        if (isPublishing) "Posting..." else "➤  Post",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
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
private fun CommunityReactionIcon(
    iconRes: Int,
    contentDescription: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = iconRes),
        contentDescription = contentDescription,
        modifier = modifier,
        colorFilter = ColorFilter.tint(tint)
    )
}

@Composable
private fun CommunityPostCard(
    post: CommunityPostDto,
    isRegisteredUser: Boolean,
    canDelete: Boolean,
    onReact: (String) -> Unit,
    onDelete: () -> Unit,
    onComments: () -> Unit,
    onBookmark: () -> Unit,
    onOpenPost: () -> Unit
) {
    var showPostMenu by remember(post.id) { mutableStateOf(false) }
    var showDeleteConfirmation by remember(post.id) { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CommunityCard)
            .clickable { onOpenPost() }
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CommunityAvatar(
                avatarUrl = post.author.avatarUrl,
                displayName = post.author.displayName,
                username = post.author.username,
                size = 40.dp
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp)
            ) {
                Text(
                    text = post.author.displayName
                        ?.takeIf { it.isNotBlank() }
                        ?: post.author.username,
                    color = CommunityText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Text(
                    text = "${post.author.username} · ${formatCommunityDate(post.createdAt)}",
                    color = CommunityMuted.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }

            Box {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF192633))
                        .clickable { showPostMenu = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text("•••", color = CommunityMuted, fontSize = 15.sp)
                }

                androidx.compose.material3.DropdownMenu(
                    expanded = showPostMenu,
                    onDismissRequest = { showPostMenu = false },
                    modifier = Modifier.background(CommunityCard)
                ) {
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text(if (post.isBookmarked) "Remove bookmark" else "Save post", color = CommunityText) },
                        onClick = {
                            showPostMenu = false
                            onBookmark()
                        }
                    )
                    if (canDelete) {
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text("Delete post", color = CommunityDanger) },
                            onClick = {
                                showPostMenu = false
                                showDeleteConfirmation = true
                            }
                        )
                    }
                }
            }
        }

        if (post.body.isNotBlank()) {
            Text(
                text = post.body,
                color = CommunityText,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(top = 10.dp, bottom = 10.dp)
            )
        }

        if (post.kind == "game" || post.kind == "bot_game") {
            post.game?.let { game ->
                CommunityGamePostCard(
                    game = game,
                    fen = post.fen
                )
                Spacer(Modifier.height(8.dp))
            }
        }


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val reactions = listOf(
                Triple("like", R.drawable.like, "Like"),
                Triple("love", R.drawable.heart, "Love"),
                Triple("fire", R.drawable.fire, "Fire"),
                Triple("wow", R.drawable.wow, "Wow"),
                Triple("dislike", R.drawable.dislike, "Dislike")
            )

            reactions.forEach { (reaction, iconRes, label) ->
                val isSelected = post.myReaction == reaction
                val reactionTint = if (isSelected) CommunityGreen else CommunityMuted

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) {
                                CommunityGreen.copy(alpha = 0.15f)
                            } else {
                                Color(0xFF20303D)
                            }
                        )
                        .clickable(enabled = isRegisteredUser) {
                            onReact(reaction)
                        }
                        .padding(horizontal = 3.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CommunityReactionIcon(
                        iconRes = iconRes,
                        contentDescription = label,
                        tint = reactionTint,
                        modifier = Modifier.size(19.dp)
                    )

                    Spacer(modifier = Modifier.width(3.dp))

                    Text(
                        text = (post.reactions[reaction] ?: 0).toString(),
                        color = reactionTint,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "◯  ${post.commentCount} comments",
                color = CommunityMuted,
                fontSize = 11.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onComments() }
                    .padding(vertical = 4.dp, horizontal = 2.dp)
            )
            Spacer(Modifier.weight(1f))
            Image(
                painter = painterResource(id = R.drawable.bookmarked),
                contentDescription = if (post.isBookmarked) "Remove bookmark" else "Bookmark post",
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .clickable { onBookmark() }
                    .padding(2.dp),
                colorFilter = ColorFilter.tint(
                    if (post.isBookmarked) CommunityGreen else CommunityMuted
                )
            )
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            containerColor = CommunityCard,
            title = { Text("Delete post?", color = CommunityText, fontWeight = FontWeight.Bold) },
            text = { Text("This post will be removed from the community.", color = CommunityMuted) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete()
                    }
                ) {
                    Text("Delete", color = CommunityDanger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel", color = CommunityMuted)
                }
            }
        )
    }
}

@Composable
fun CommunityCommentsDialog(
    postId: Long,
    viewModel: CommunityViewModel,
    isRegisteredUser: Boolean,
    currentUsername: String?,
    canDeletePostComments: Boolean,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var comments by remember(postId) { mutableStateOf<List<CommunityCommentDto>>(emptyList()) }
    var nextAfterId by remember(postId) { mutableStateOf<Long?>(null) }
    var commentText by remember(postId) { mutableStateOf("") }
    var isLoading by remember(postId) { mutableStateOf(true) }
    var isSending by remember(postId) { mutableStateOf(false) }
    var isLoadingOlder by remember(postId) { mutableStateOf(false) }
    var error by remember(postId) { mutableStateOf<String?>(null) }

    suspend fun loadInitialComments() {
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

    LaunchedEffect(postId) { loadInitialComments() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .heightIn(max = 720.dp),
            shape = RoundedCornerShape(28.dp),
            color = CommunityCard
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("CONVERSATION", color = CommunityGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                        Text("Comments", color = CommunityText, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = onDismiss) {
                        Text("✕", color = CommunityMuted, fontSize = 18.sp)
                    }
                }

                error?.let {
                    Text(it, color = CommunityDanger, modifier = Modifier.padding(bottom = 8.dp))
                }

                Box(modifier = Modifier.weight(1f, fill = false)) {
                    when {
                        isLoading && comments.isEmpty() -> Box(
                            modifier = Modifier.fillMaxWidth().height(180.dp),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator(color = CommunityGreen) }

                        comments.isEmpty() -> Box(
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            contentAlignment = Alignment.Center
                        ) { Text("No comments yet. Start the conversation.", color = CommunityMuted) }

                        else -> LazyColumn(
                            modifier = Modifier.fillMaxWidth().heightIn(max = 430.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(comments, key = { it.id }) { comment ->
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(CommunityBackground)
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    CommunityAvatar(
                                        avatarUrl = comment.author.avatarUrl,
                                        displayName = comment.author.displayName,
                                        username = comment.author.username,
                                        size = 38.dp
                                    )
                                    Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                                        Text(
                                            text = comment.author.displayName?.takeIf { it.isNotBlank() }
                                                ?: comment.author.username,
                                            color = CommunityText,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = comment.body,
                                            color = CommunityText,
                                            fontSize = 14.sp,
                                            lineHeight = 20.sp,
                                            modifier = Modifier.padding(top = 3.dp)
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = formatCommunityDate(comment.createdAt),
                                                color = CommunityMuted,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(top = 3.dp)
                                            )
                                            if (isRegisteredUser && (canDeletePostComments ||
                                                        (!currentUsername.isNullOrBlank() && comment.author.username.equals(currentUsername, ignoreCase = true)))) {
                                                Text(
                                                    text = "  ·  Delete",
                                                    color = CommunityDanger,
                                                    fontSize = 12.sp,
                                                    modifier = Modifier
                                                        .clickable {
                                                            scope.launch {
                                                                try {
                                                                    viewModel.deleteComment(comment.id)
                                                                    comments = comments.filterNot { it.id == comment.id }
                                                                    viewModel.refresh()
                                                                } catch (e: Exception) {
                                                                    error = e.message ?: "Unable to delete comment."
                                                                }
                                                            }
                                                        }
                                                        .padding(top = 3.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            if (nextAfterId != null) {
                                item(key = "load_older_comments") {
                                    TextButton(
                                        enabled = !isLoadingOlder,
                                        onClick = {
                                            scope.launch {
                                                isLoadingOlder = true
                                                try {
                                                    val result = viewModel.loadComments(postId, nextAfterId)
                                                    comments = (comments + result.comments).distinctBy { it.id }
                                                    nextAfterId = result.nextAfterId
                                                } catch (e: Exception) {
                                                    error = e.message ?: "Unable to load older comments."
                                                } finally {
                                                    isLoadingOlder = false
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(if (isLoadingOlder) "Loading..." else "Load older comments", color = CommunityGreen)
                                    }
                                }
                            }
                        }
                    }
                }

                if (isRegisteredUser) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { if (it.length <= 500) commentText = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        placeholder = { Text("Write a comment", color = CommunityMuted) },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = CommunityText),
                        supportingText = { Text("${commentText.length}/500", color = CommunityMuted) },
                        enabled = !isSending,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CommunityText,
                            unfocusedTextColor = CommunityText,
                            cursorColor = CommunityGreen,
                            focusedBorderColor = CommunityGreen,
                            unfocusedBorderColor = CommunityOutline,
                            focusedContainerColor = CommunityBackground,
                            unfocusedContainerColor = CommunityBackground,
                            focusedPlaceholderColor = CommunityMuted,
                            unfocusedPlaceholderColor = CommunityMuted
                        ),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 4
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Close", color = CommunityMuted) }
                    if (isRegisteredUser) {
                        TextButton(
                            enabled = commentText.isNotBlank() && !isSending,
                            onClick = {
                                scope.launch {
                                    isSending = true
                                    error = null
                                    try {
                                        val created = viewModel.addComment(postId, commentText.trim())
                                        comments = (comments + created).distinctBy { it.id }
                                        commentText = ""
                                        // Refresh the feed so the post's displayed comment count updates too.
                                        viewModel.refresh()
                                    } catch (e: Exception) {
                                        error = e.message ?: "Unable to add comment."
                                    } finally {
                                        isSending = false
                                    }
                                }
                            }
                        ) {
                            Text(if (isSending) "Sending..." else "Send", color = CommunityGreen)
                        }
                    }
                }
            }
        }
    }
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


private fun usernameFromJwt(token: String?): String? {
    if (token.isNullOrBlank()) return null
    return try {
        val parts = token.split('.')
        if (parts.size < 2) return null
        val payload = String(Base64.getUrlDecoder().decode(parts[1]), Charsets.UTF_8)
        val claims = JSONObject(payload)
        sequenceOf("username", "preferred_username", "user_name", "name", "sub")
            .mapNotNull { key -> claims.optString(key).takeIf { it.isNotBlank() && it != "null" } }
            .firstOrNull()
    } catch (_: Exception) {
        null
    }
}