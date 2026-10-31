package com.abhinav.caisarra.presentation.club

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.abhinav.caisarra.data.remote.dto.*
import com.abhinav.caisarra.data.repository.ClubRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import com.abhinav.caisarra.data.local.TokenManager
import com.abhinav.caisarra.data.remote.ClubChatSocket


private val DetailBg = Color(0xFF080D13)
private val DetailCard = Color(0xFF101923)
private val DetailGreen = Color(0xFF00D8E8)
private val DetailText = Color(0xFFF5F5F5)
private val DetailMuted = Color(0xFFADB8C6)

private enum class DetailTab { MEMBERS, LEADERBOARD, CHAT, MANAGE }

data class ClubDetailState(
    val club: ClubDto? = null,
    val currentUsername: String = "",
    val members: List<ClubMemberDto> = emptyList(),
    val leaderboard: List<ClubLeaderboardPlayerDto> = emptyList(),
    val messages: List<ClubMessageDto> = emptyList(),
    val bans: List<ClubMemberDto> = emptyList(),
    val selectedTab: String = "MEMBERS",
    val leaderboardMode: String = "rapid",
    val chatInput: String = "",
    val loading: Boolean = true,
    val busy: Boolean = false,
    val chatConnected: Boolean = false,
    val error: String? = null,
    val info: String? = null,
    val clubRemoved: Boolean = false
)

class ClubDetailViewModel(
    private val appContext: Context,
    private val clubId: String,
    private val repository: ClubRepository
) : ViewModel() {

    private val chatSocket = ClubChatSocket(appContext)

    var state by mutableStateOf(ClubDetailState())
        private set

    init {
        load()
        connectChat()
    }

    fun load() {
        viewModelScope.launch {
            state = state.copy(loading = true, error = null)

            try {
                val detailClub = repository.getClub(clubId)
                val currentUsername = runCatching {
                    TokenManager.get(appContext).getUsername()
                }.getOrNull()?.trim()?.lowercase()

                val myClubRole = runCatching {
                    repository.getMyClubs()
                        .firstOrNull { it.effectiveId == clubId || it.id == detailClub.id }
                        ?.role
                }.onFailure {
                    Log.w("ClubDetail", "Could not resolve role from my clubs", it)
                }.getOrNull()

                val loadedMembers = runCatching {
                    repository.getMembers(clubId)
                }.onFailure {
                    Log.w("ClubDetail", "Could not resolve role from members", it)
                }.getOrDefault(emptyList())

                val currentMemberRole = if (currentUsername.isNullOrBlank()) {
                    null
                } else {
                    loadedMembers.firstOrNull {
                        it.username.trim().lowercase() == currentUsername
                    }?.role
                }

                fun normalizedRole(value: String?): String =
                    value.orEmpty().trim().lowercase()
                        .replace('-', '_')
                        .replace(' ', '_')

                val resolvedRole = currentMemberRole
                    ?: myClubRole
                    ?: detailClub.role

                state = state.copy(
                    club = detailClub.copy(role = resolvedRole),
                    currentUsername = currentUsername.orEmpty(),
                    members = loadedMembers,
                    loading = false
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("ClubDetail", "Failed to load club details", e)
                state = state.copy(
                    loading = false,
                    error = "Couldn't load club details. Please try again."
                )
                return@launch
            }
            launchSection("leaderboard") {
                state = state.copy(
                    leaderboard = repository.getLeaderboard(
                        clubId, state.leaderboardMode
                    )
                )
            }
            launchSection("messages") {
                state = state.copy(messages = repository.getMessages(clubId))
            }
            launchSection("bans") {
                state = state.copy(bans = repository.getBans(clubId))
            }
        }
    }

    private suspend fun launchSection(
        section: String,
        action: suspend () -> Unit
    ) {
        try {
            action()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("ClubDetail", "Failed to load $section", e)
        }
    }

    fun selectTab(tab: String) {
        state = state.copy(selectedTab = tab)
    }

    fun setChatInput(value: String) {
        state = state.copy(chatInput = value)
    }

    fun changeLeaderboardMode(mode: String) {
        state = state.copy(leaderboardMode = mode)
        runAction("Leaderboard updated") {
            state = state.copy(
                leaderboard = repository.getLeaderboard(clubId, mode)
            )
        }
    }

    private fun connectChat() {
        viewModelScope.launch {
            launch {
                chatSocket.connected.collect { connected ->
                    state = state.copy(chatConnected = connected)
                }
            }

            launch {
                chatSocket.messages.collect { message ->
                    val existing = state.messages

                    val alreadyReceived = message.id.isNotBlank() &&
                            existing.any { it.id == message.id }

                    if (!alreadyReceived) {
                        state = state.copy(
                            messages = (existing + message).takeLast(200)
                        )
                    }
                }
            }

            launch {
                chatSocket.errors.collect { message ->
                    state = state.copy(error = clubUiError(message))
                }
            }

            chatSocket.connect(clubId)
        }
    }


    fun reconnectChat() {
        viewModelScope.launch {
            chatSocket.connect(clubId)
        }
    }

    fun sendMessage() {
        val text = state.chatInput.trim()

        if (text.isBlank() || state.busy) return

        viewModelScope.launch {
            state = state.copy(
                busy = true,
                error = null,
                info = null
            )

            try {
                val sentMessage = repository.sendMessage(clubId, text)

                state = state.copy(
                    chatInput = "",
                    messages = (
                            state.messages.filterNot {
                                sentMessage.id.isNotBlank() &&
                                        it.id == sentMessage.id
                            } + sentMessage
                            ).takeLast(200),
                    info = "Message sent successfully."
                )

                try {
                    state = state.copy(
                        messages = repository.getMessages(clubId).takeLast(200)
                    )
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(
                        "ClubDetail",
                        "Message sent, but history refresh failed",
                        e
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("ClubDetail", "Failed to send club message", e)

                state = state.copy(
                    error = "Couldn't send your message. Please try again."
                )
            } finally {
                state = state.copy(busy = false)
            }
        }
    }




    fun leaveClub() = runAction(
        successMessage = "You left the club",
        removesClub = true
    ) {
        repository.leaveClub(clubId)
    }

    fun deleteClub() = runAction(
        successMessage = "Club deleted",
        removesClub = true
    ) {
        repository.deleteClub(clubId)
    }

    fun kick(userId: Long) = runAction("Member removed") {
        repository.kickMember(clubId, userId)
        refreshMembers()
    }

    fun promote(userId: Long) = runAction("Admin promoted") {
        repository.promoteAdmin(clubId, userId)
        refreshMembers()
    }

    fun removeAdmin(userId: Long) = runAction("Admin role removed") {
        repository.removeAdmin(clubId, userId)
        refreshMembers()
    }

    fun transferLeadership(userId: Long) =
        runAction("Leadership transferred") {
            repository.transferLeadership(clubId, userId)
            load()
        }

    fun ban(userId: Long) = runAction("Member banned") {
        repository.banMember(clubId, userId)
        refreshMembers()
        state = state.copy(bans = repository.getBans(clubId))
    }

    fun unblock(userId: Long) = runAction("Member unblocked") {
        repository.unblockMember(clubId, userId)
        state = state.copy(bans = repository.getBans(clubId))
    }

    fun clearError() {
        state = state.copy(error = null)
    }

    fun clearInfo() {
        state = state.copy(info = null)
    }

    private suspend fun refreshMembers() {
        state = state.copy(members = repository.getMembers(clubId))
    }

    private fun runAction(
        successMessage: String,
        removesClub: Boolean = false,
        action: suspend () -> Unit
    ) {
        if (state.busy) return

        viewModelScope.launch {
            state = state.copy(busy = true, error = null, info = null)
            try {
                action()
                state = state.copy(
                    info = successMessage,
                    clubRemoved = removesClub || state.clubRemoved
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("ClubDetail", "Club action failed", e)
                state = state.copy(
                    error = e.message ?: "Club action failed. Please try again."
                )
            } finally {
                state = state.copy(busy = false)
            }
        }
    }



    override fun onCleared() {
        chatSocket.close()
        super.onCleared()
    }

}

private fun clubUiError(raw: String): String {
    val message = raw.trim()
    return if (
        message.contains("Unexpected JSON token", ignoreCase = true) ||
        message.contains("Expected start of the object", ignoreCase = true) ||
        message.contains("JSON input:", ignoreCase = true) ||
        message.startsWith("[{")
    ) {
        "Couldn't load this section because the server response format was unexpected. Please try again."
    } else {
        message.takeIf { it.isNotBlank() } ?: "Something went wrong. Please try again."
    }
}

private fun formatClubMessageTime(timestamp: String?): String {
    if (timestamp.isNullOrBlank()) return ""

    return try {
        val instant = java.time.Instant.parse(timestamp)

        val formatter = java.time.format.DateTimeFormatter
            .ofPattern("h:mm a")
            .withZone(java.time.ZoneId.systemDefault())

        formatter.format(instant)
    } catch (e: Exception) {
        timestamp
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubDetailScreen(
    clubId: String,
    repository: ClubRepository,
    onBack: () -> Unit
) {

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val vm: ClubDetailViewModel = viewModel(
        key = "club-detail-$clubId",
        factory = viewModelFactory {
            initializer {
                ClubDetailViewModel(
                    appContext = context.applicationContext,
                    clubId = clubId,
                    repository = repository
                )
            }
        }
    )

    val state = vm.state
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.error) {
        val message = state.error?.takeIf { it.isNotBlank() }
        if (message != null) {
            snackbarHostState.showSnackbar(clubUiError(message))
            vm.clearError()
        }
    }
    LaunchedEffect(state.info) {
        val message = state.info?.takeIf { it.isNotBlank() }
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            vm.clearInfo()
        }
    }
    BackHandler(enabled = state.selectedTab == "CHAT") {
        vm.selectTab("MEMBERS")
    }
    LaunchedEffect(state.clubRemoved) {
        if (state.clubRemoved) onBack()
    }

    val role = state.club?.role
        .orEmpty()
        .trim()
        .lowercase()
        .replace('-', '_')
        .replace(' ', '_')

    val isOwner = role in setOf("owner", "club_owner", "leader")

    val isAdmin = isOwner || role in setOf("admin", "administrator", "club_admin")
    var selectedMember by remember { mutableStateOf<ClubMemberDto?>(null) }
    var confirmAction by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { focusManager.clearFocus() },
        containerColor = DetailBg,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = DetailCard,
                    contentColor = DetailText,
                    actionColor = DetailGreen,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            state.club?.name ?: "Club details",
                            color = DetailText,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (state.selectedTab == "CHAT") "Club chat"
                            else "Chess community",
                            color = DetailMuted,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (state.selectedTab == "CHAT") {
                                vm.selectTab("MEMBERS")
                            } else {
                                onBack()
                            }
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = DetailText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DetailBg
                )
            )
        }
    ) { padding ->
        if (state.loading) {
            Box(Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DetailGreen)
            }
        } else if (state.club == null) {
            Column(Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
                Text(
                    "Couldn't load this club. Please try again.",
                    color = DetailMuted
                )
                Button(onClick = vm::load) { Text("Retry") }
            }
        }
        else if (state.selectedTab == "CHAT") {
            ClubChatContent(
                state = state,
                onMessageChange = vm::setChatInput,
                onSendMessage = vm::sendMessage,
                onReconnect = vm::reconnectChat,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } else {
            val pullToRefreshState = rememberPullToRefreshState()

            PullToRefreshBox(
                isRefreshing = state.loading,
                onRefresh = {
                    vm.load()
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                state = pullToRefreshState,
                indicator = {
                    PullToRefreshDefaults.Indicator(
                        state = pullToRefreshState,
                        isRefreshing = state.loading,
                        modifier = Modifier.align(Alignment.TopCenter),
                        color = DetailGreen,
                        containerColor = DetailCard
                    )
                }
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Card(
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF151F2B)
                            )
                        ) {
                            Column(
                                Modifier.fillMaxWidth().padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Groups, null,
                                    tint = DetailGreen,
                                    modifier = Modifier.size(32.dp))
                                Text(state.club.name, color = Color.White,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold)
                                Text(
                                    state.club.description ?: "Welcome to this club.",
                                    color = Color(0xFFB8C2CC)
                                )
                                Text(
                                    "${state.club.memberCount} members",
                                    color = DetailGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                                state.club.shareCode?.takeIf { it.isNotBlank() }?.let { shareCode ->
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        color = DetailBg,
                                        border = BorderStroke(1.dp, Color(0xFF1E2838))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable {
                                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                        clipboard.setPrimaryClip(ClipData.newPlainText("club_share_code", shareCode))
                                                        Toast.makeText(context, "Club code copied", Toast.LENGTH_SHORT).show()
                                                    }
                                            ) {
                                                Text(
                                                    "SHARE CODE",
                                                    color = DetailMuted,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 1.sp
                                                )
                                                Spacer(Modifier.height(2.dp))
                                                Text(
                                                    shareCode,
                                                    color = DetailGreen,
                                                    fontSize = 17.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 2.sp
                                                )
                                            }
                                            OutlinedButton(
                                                onClick = {
                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                    clipboard.setPrimaryClip(ClipData.newPlainText("club_share_code", shareCode))
                                                    Toast.makeText(context, "Club code copied", Toast.LENGTH_SHORT).show()
                                                },
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                border = BorderStroke(1.dp, DetailGreen.copy(alpha = 0.5f))
                                            ) {
                                                Icon(
                                                    Icons.Default.ContentCopy,
                                                    contentDescription = null,
                                                    tint = DetailGreen,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(Modifier.width(4.dp))
                                                Text(
                                                    "Copy",
                                                    color = DetailGreen,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                }
                                if (state.busy) {
                                    LinearProgressIndicator(
                                        modifier = Modifier.fillMaxWidth(),
                                        color = DetailGreen
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .background(DetailCard, RoundedCornerShape(16.dp))
                                .padding(5.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(
                                "MEMBERS" to "Members",
                                "LEADERBOARD" to "Ranks",
                                "CHAT" to "Chat",
                                "MANAGE" to "Manage"
                            ).forEach { (key, label) ->
                                val selected = state.selectedTab == key
                                Box(
                                    Modifier.weight(1f)
                                        .background(
                                            if (selected) DetailGreen
                                            else Color.Transparent,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { vm.selectTab(key) }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        label,
                                        color = if (selected) DetailBg else DetailMuted,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    when (state.selectedTab) {
                        "MEMBERS" -> {
                            item {
                                Text("Members (${state.members.size})",
                                    color = DetailText,
                                    style = MaterialTheme.typography.titleLarge)
                            }
                            if (state.members.isEmpty()) {
                                item {
                                    Text("No member records returned by the server.",
                                        color = DetailMuted)
                                }
                            }
                            items(state.members, key = { it.userId }) { member ->
                                Card(colors = CardDefaults.cardColors(
                                    containerColor = DetailCard
                                )) {
                                    Row(
                                        Modifier.fillMaxWidth().padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(Modifier.weight(1f)) {
                                            Text(member.username, color = DetailText,
                                                fontWeight = FontWeight.SemiBold)
                                            Text(member.role, color = DetailGreen)
                                            member.rating?.let {
                                                Text("Rating $it", color = DetailMuted)
                                            }
                                        }
                                        val memberRole = member.role
                                            .trim()
                                            .lowercase()
                                            .replace('-', '_')
                                            .replace(' ', '_')

                                        val isMemberOwner = memberRole in setOf("owner", "club_owner", "leader")
                                        val isMemberAdmin = memberRole in setOf("admin", "administrator", "club_admin")

                                        val canManageThisMember = when {
                                            isOwner -> !isMemberOwner
                                            isAdmin -> !isMemberOwner && !isMemberAdmin
                                            else -> false
                                        }

                                        if (canManageThisMember) {
                                            TextButton(
                                                onClick = {
                                                    selectedMember = member
                                                },
                                                enabled = !state.busy
                                            ) {
                                                Text(
                                                    text = "Actions",
                                                    color = DetailGreen
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        "LEADERBOARD" -> {
                            item {
                                Text("Club leaderboard", color = DetailText,
                                    style = MaterialTheme.typography.titleLarge)
                            }
                            item {
                                var expanded by remember { mutableStateOf(false) }
                                val modes = listOf("bullet", "blitz", "rapid", "classical")

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            color = DetailCard,
                                            shape = RoundedCornerShape(18.dp)
                                        )
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "Rating format",
                                        color = DetailMuted,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Box {
                                        Surface(
                                            onClick = { expanded = true },
                                            shape = RoundedCornerShape(14.dp),
                                            color = DetailBg,
                                            border = BorderStroke(
                                                1.dp,
                                                DetailGreen.copy(alpha = 0.75f)
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 16.dp, vertical = 13.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    Text(
                                                        text = state.leaderboardMode.replaceFirstChar {
                                                            it.uppercase()
                                                        },
                                                        color = DetailGreen,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "View club rankings by time control",
                                                        color = DetailMuted,
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                }
                                                Icon(
                                                    imageVector = Icons.Default.ArrowDropDown,
                                                    contentDescription = "Choose rating format",
                                                    tint = DetailGreen
                                                )
                                            }
                                        }

                                        DropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false },
                                            modifier = Modifier
                                                .widthIn(min = 220.dp, max = 300.dp),
                                            shape = RoundedCornerShape(16.dp),
                                            containerColor = DetailCard,
                                            tonalElevation = 8.dp,
                                            shadowElevation = 10.dp,
                                            border = BorderStroke(
                                                1.dp,
                                                DetailMuted.copy(alpha = 0.18f)
                                            )
                                        ) {
                                            Text(
                                                text = "SELECT FORMAT",
                                                modifier = Modifier.padding(
                                                    start = 16.dp,
                                                    top = 12.dp,
                                                    bottom = 8.dp
                                                ),
                                                color = DetailMuted,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )

                                            modes.forEach { mode ->
                                                val selected = state.leaderboardMode == mode
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            text = mode.replaceFirstChar { it.uppercase() },
                                                            color = if (selected) DetailGreen else DetailText,
                                                            fontWeight = if (selected) {
                                                                FontWeight.Bold
                                                            } else {
                                                                FontWeight.Medium
                                                            }
                                                        )
                                                    },
                                                    leadingIcon = {
                                                        Box(
                                                            modifier = Modifier.size(28.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            if (selected) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Check,
                                                                    contentDescription = "Selected",
                                                                    tint = DetailGreen,
                                                                    modifier = Modifier.size(18.dp)
                                                                )
                                                            } else {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .size(6.dp)
                                                                        .background(
                                                                            DetailMuted.copy(alpha = 0.45f),
                                                                            RoundedCornerShape(50)
                                                                        )
                                                                )
                                                            }
                                                        }
                                                    },
                                                    onClick = {
                                                        expanded = false
                                                        if (!selected) vm.changeLeaderboardMode(mode)
                                                    },
                                                    colors = MenuDefaults.itemColors(
                                                        textColor = DetailText
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            if (state.leaderboard.isEmpty()) {
                                item {
                                    Text("No leaderboard entries available.",
                                        color = DetailMuted)
                                }
                            }
                            items(state.leaderboard) { player ->
                                Card(colors = CardDefaults.cardColors(
                                    containerColor = DetailCard
                                )) {
                                    Row(Modifier.fillMaxWidth().padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(
                                            "${player.rank}. ${player.username}",
                                            color = DetailText
                                        )
                                        Text("${player.rating}", color = DetailGreen)
                                    }
                                }
                            }
                        }

                        "CHAT" -> {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Club chat",
                                        color = DetailText,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            Modifier.size(8.dp).background(
                                                if (state.chatConnected) DetailGreen else Color(0xFFFF9999),
                                                RoundedCornerShape(50)
                                            )
                                        )
                                        Text(
                                            if (state.chatConnected) "Live" else "Reconnecting required",
                                            color = if (state.chatConnected) DetailGreen else Color(0xFFFF9999),
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }
                            }
                            if (!state.chatConnected) {
                                item {
                                    OutlinedButton(
                                        onClick = vm::reconnectChat,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Reconnect chat")
                                    }
                                }
                            }
                            item {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedTextField(
                                        value = state.chatInput,
                                        onValueChange = vm::setChatInput,
                                        modifier = Modifier.weight(1f),
                                        label = { Text("Write a message") },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = DetailText,
                                            unfocusedTextColor = DetailText,
                                            focusedLabelColor = DetailGreen,
                                            unfocusedLabelColor = DetailMuted,
                                            cursorColor = DetailGreen,
                                            focusedBorderColor = DetailGreen,
                                            unfocusedBorderColor = DetailMuted
                                        )
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Button(
                                        enabled = state.chatConnected && !state.busy &&state.chatInput.isNotBlank(),
                                        onClick = vm::sendMessage,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = DetailGreen,
                                            contentColor = DetailBg
                                        )
                                    ) {
                                        Text(if (state.busy) "Sending..." else "Send")
                                    }
                                }
                            }
                            if (state.messages.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 28.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "No messages yet. Say hello!",
                                            color = DetailMuted,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }

                            items(
                                state.messages,
                                key = { message ->
                                    message.id.ifBlank {
                                        "${message.userId}-${message.createdAt}-${message.message}"
                                    }
                                }
                            ) { message ->
                                val isMine = state.currentUsername.isNotBlank() &&
                                        message.username.trim().equals(
                                            state.currentUsername.trim(),
                                            ignoreCase = true
                                        )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .widthIn(max = 300.dp)
                                            .background(
                                                color = if (isMine) Color(0xFF075E54) else Color(0xFF18232F),
                                                shape = RoundedCornerShape(
                                                    topStart = 16.dp,
                                                    topEnd = 16.dp,
                                                    bottomStart = if (isMine) 16.dp else 4.dp,
                                                    bottomEnd = if (isMine) 4.dp else 16.dp
                                                )
                                            )
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = if (isMine) "You" else message.username,
                                            color = if (isMine) Color(0xFF9DE7C7) else Color(0xFF55D6A5),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = message.message,
                                            color = Color(0xFFF5F5F5),
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Spacer(Modifier.height(3.dp))
                                        Text(
                                            text = formatClubMessageTime(message.createdAt),
                                            color = Color(0xFFB8C2CC),
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.align(Alignment.End)
                                        )
                                    }
                                }
                            }
                        }

                        "MANAGE" -> {
                            item {
                                Text("Club management", color = DetailText,
                                    style = MaterialTheme.typography.titleLarge)
                                Text(
                                    if (isOwner) "Owner permissions"
                                    else if (isAdmin) "Administrator permissions"
                                    else "Member permissions",
                                    color = DetailMuted
                                )
                            }
                            item {
                                Button(
                                    onClick = { confirmAction = "leave" },
                                    enabled = !state.busy,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = DetailCard,
                                        contentColor = DetailText
                                    )
                                ) { Text("Leave club") }
                            }
                            if (isOwner) {
                                item {
                                    Button(
                                        onClick = { confirmAction = "delete" },
                                        enabled = !state.busy,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF8C3434)
                                        )
                                    ) { Text("Delete club") }
                                }
                            }
                            if (isAdmin) {
                                item {
                                    Text("Banned members (${state.bans.size})",
                                        color = DetailText,
                                        style = MaterialTheme.typography.titleMedium)
                                }
                                items(state.bans, key = { "ban-${it.userId}" }) { banned ->
                                    Card(colors = CardDefaults.cardColors(
                                        containerColor = DetailCard
                                    )) {
                                        Row(Modifier.fillMaxWidth().padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically) {
                                            Text(banned.username, color = DetailText,
                                                modifier = Modifier.weight(1f))
                                            TextButton(
                                                onClick = { vm.unblock(banned.userId) },
                                                enabled = !state.busy
                                            ) { Text("Unblock", color = DetailGreen) }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    selectedMember?.let { member ->
        AlertDialog(
            onDismissRequest = { selectedMember = null },
            containerColor = DetailCard,
            title = { Text(member.username, color = DetailText) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (isOwner && member.role.lowercase() != "owner") {
                        TextButton(onClick = {
                            selectedMember = null
                            confirmAction = "promote:${member.userId}"
                        }) { Text("Promote to admin") }
                        TextButton(onClick = {
                            selectedMember = null
                            confirmAction = "transfer:${member.userId}"
                        }) { Text("Transfer leadership") }
                    }
                    if (isOwner && member.role.lowercase() == "admin") {
                        TextButton(onClick = {
                            selectedMember = null
                            confirmAction = "removeadmin:${member.userId}"
                        }) { Text("Remove admin") }
                    }
                    TextButton(onClick = {
                        selectedMember = null
                        confirmAction = "kick:${member.userId}"
                    }) { Text("Remove member", color = Color(0xFFFF9999)) }
                    TextButton(onClick = {
                        selectedMember = null
                        confirmAction = "ban:${member.userId}"
                    }) { Text("Ban member", color = Color(0xFFFF9999)) }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedMember = null }) {
                    Text("Close", color = DetailGreen)
                }
            }
        )
    }

    confirmAction?.let { action ->
        val destructive = action == "delete" ||
                action == "leave" ||
                action.startsWith("kick:") ||
                action.startsWith("ban:") ||
                action.startsWith("transfer:")

        AlertDialog(
            onDismissRequest = { confirmAction = null },
            containerColor = DetailCard,
            title = { Text("Confirm action", color = DetailText) },
            text = {
                Text(
                    when {
                        action == "leave" -> "Leave this club?"
                        action == "delete" -> "Permanently delete this club?"
                        action.startsWith("kick:") -> "Remove this member?"
                        action.startsWith("ban:") -> "Ban this member?"
                        action.startsWith("promote:") -> "Promote this member to admin?"
                        action.startsWith("removeadmin:") -> "Remove admin privileges?"
                        action.startsWith("transfer:") -> "Transfer club leadership? You may lose owner permissions."
                        else -> "Continue?"
                    },
                    color = DetailText
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !state.busy,
                    onClick = {
                        val current = confirmAction
                        confirmAction = null
                        when {
                            current == "leave" -> vm.leaveClub()
                            current == "delete" -> vm.deleteClub()
                            current?.startsWith("kick:") == true ->
                                vm.kick(current.substringAfter(":").toLong())
                            current?.startsWith("ban:") == true ->
                                vm.ban(current.substringAfter(":").toLong())
                            current?.startsWith("promote:") == true ->
                                vm.promote(current.substringAfter(":").toLong())
                            current?.startsWith("removeadmin:") == true ->
                                vm.removeAdmin(current.substringAfter(":").toLong())
                            current?.startsWith("transfer:") == true ->
                                vm.transferLeadership(current.substringAfter(":").toLong())
                        }
                    }
                ) {
                    Text("Confirm", color = if (destructive)
                        Color(0xFFFF9999) else DetailGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmAction = null }) {
                    Text("Cancel", color = DetailMuted)
                }
            }
        )
    }
}

@Composable
private fun ClubChatContent(
    state: ClubDetailState,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onReconnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(DetailBg)
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Club chat",
                    color = DetailText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = if (state.chatConnected) "Live conversation"
                    else "Reconnecting...",
                    color = if (state.chatConnected) DetailGreen
                    else Color(0xFFFF9999),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Icon(
                imageVector = Icons.Default.Groups,
                contentDescription = "Club members",
                tint = DetailGreen,
                modifier = Modifier.size(26.dp)
            )
        }

        HorizontalDivider(
            color = DetailMuted.copy(alpha = 0.22f),
            thickness = 1.dp
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(
                top = 16.dp,
                bottom = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.messages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No messages yet. Say hello!",
                            color = DetailMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            items(
                items = state.messages,
                key = { message ->
                    message.id.ifBlank {
                        "${message.userId}-${message.createdAt}-${message.message}"
                    }
                }
            ) { message ->

                val isMine = state.currentUsername.isNotBlank() &&
                        message.username.trim().equals(
                            state.currentUsername.trim(),
                            ignoreCase = true
                        )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMine) {
                        Arrangement.End
                    } else {
                        Arrangement.Start
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 300.dp)
                            .background(
                                color = if (isMine) {
                                    Color(0xFF075E54)
                                } else {
                                    Color(0xFF18232F)
                                },
                                shape = RoundedCornerShape(
                                    topStart = 18.dp,
                                    topEnd = 18.dp,
                                    bottomStart = if (isMine) 18.dp else 4.dp,
                                    bottomEnd = if (isMine) 4.dp else 18.dp
                                )
                            )
                            .padding(
                                horizontal = 14.dp,
                                vertical = 10.dp
                            )
                    ) {
                        Text(
                            text = if (isMine) "You" else message.username,
                            color = if (isMine) {
                                Color(0xFF9DE7C7)
                            } else {
                                Color(0xFF55D6A5)
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = message.message,
                            color = DetailText,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(Modifier.height(5.dp))

                        Text(
                            text = formatClubMessageTime(message.createdAt),
                            color = Color(0xFFB8C2CC),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = state.chatInput,
                onValueChange = onMessageChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = "Write a message...",
                        color = DetailMuted
                    )
                },
                shape = RoundedCornerShape(28.dp),
                singleLine = true,
                enabled = !state.busy,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = DetailText,
                    unfocusedTextColor = DetailText,
                    cursorColor = DetailGreen,
                    focusedContainerColor = DetailCard,
                    unfocusedContainerColor = DetailCard,
                    focusedBorderColor = DetailGreen,
                    unfocusedBorderColor = Color.Transparent,
                    focusedPlaceholderColor = DetailMuted,
                    unfocusedPlaceholderColor = DetailMuted
                )
            )

            IconButton(
                onClick = onSendMessage,
                enabled = state.chatConnected &&
                        !state.busy &&
                        state.chatInput.isNotBlank(),
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        color = if (
                            state.chatConnected &&
                            !state.busy &&
                            state.chatInput.isNotBlank()
                        ) {
                            DetailGreen
                        } else {
                            DetailCard
                        },
                        shape = RoundedCornerShape(50)
                    )
            ) {
                if (state.busy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = DetailBg,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "➤",
                        color = if (
                            state.chatConnected &&
                            state.chatInput.isNotBlank()
                        ) DetailBg else DetailMuted,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }

        if (!state.chatConnected) {
            TextButton(
                onClick = onReconnect,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Reconnect chat", color = DetailGreen)
            }
        }
    }
}

