
package com.abhinav.caisarra.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abhinav.caisarra.core.ui.components.CaisaaraTopBar
import com.abhinav.caisarra.data.remote.admin.*
import com.abhinav.caisarra.presentation.viewmodel.AdminViewModel

private val AdminBg = Color(0xFF080D13)
private val AdminSurface = Color(0xFF101923)
private val AdminCyan = Color(0xFF00D5E8)
private val AdminWhite = Color.White
private val AdminMuted = Color(0xFFB8C0CA)
private val AdminBorder = Color(0xFF293440)
private val AdminRed = Color(0xFFFF5C65)

private val adminTabs = listOf(
    "Dashboard",
    "Users",
    "Reports",
    "Live Games",
    "Content",
    "Activity"
)

@Composable
fun AdminScreen(
    viewModel: AdminViewModel,
    onLogout: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    var search by remember { mutableStateOf("") }

    var selectedUser by remember {
        mutableStateOf<AdminUser?>(null)
    }

    var selectedReport by remember {
        mutableStateOf<AdminReport?>(null)
    }

    var selectedGame by remember {
        mutableStateOf<AdminGame?>(null)
    }
    var forceLogoutUser by remember {
        mutableStateOf<AdminUser?>(null)
    }
    var showAdminLogoutDialog by remember {
        mutableStateOf(false)
    }

    var confirmAdminLogout by remember {
        mutableStateOf(false)
    }

    var banReason by remember { mutableStateOf("") }
    var banDays by remember { mutableStateOf("7") }
    var reportNote by remember { mutableStateOf("") }
    var abortReason by remember { mutableStateOf("") }

    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            0 -> viewModel.loadDashboard()
            1 -> viewModel.loadUsers()
            2 -> viewModel.loadReports()
            3 -> viewModel.loadGames()
            4 -> Unit
            5 -> viewModel.loadActions()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminBg)
            .statusBarsPadding()
    ) {
        CaisaaraTopBar(
            title = "MOSAIC ADMIN",
            titleColor = AdminCyan,
            backgroundColor = AdminBg,
            height = 56.dp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Administration",
                    color = AdminWhite,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Platform control center",
                    color = AdminMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            TextButton(
                onClick = {
                    confirmAdminLogout = false
                    showAdminLogoutDialog = true
                }
            ) {
                Text("Logout", color = AdminRed)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            adminTabs.forEachIndexed { index, title ->
                val selected = selectedTab == index

                Surface(
                    onClick = { selectedTab = index },
                    color = if (selected) AdminSurface else AdminBg,
                    shape = RoundedCornerShape(12.dp),
                    border = if (selected) null else
                        androidx.compose.foundation.BorderStroke(
                            1.dp,
                            AdminBorder
                        )
                ) {
                    Text(
                        text = title,
                        modifier = Modifier.padding(
                            horizontal = 16.dp,
                            vertical = 11.dp
                        ),
                        color = if (selected) AdminCyan else AdminMuted,
                        fontWeight = if (selected) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Medium
                        },
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        state.error?.let {
            Text(
                it,
                color = AdminRed,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        state.message?.let {
            Text(
                it,
                color = AdminCyan,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        if (state.loading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                color = AdminCyan,
                trackColor = AdminSurface
            )
        }

        when (selectedTab) {

            0 -> DashboardSection(state)

            1 -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    OutlinedTextField(
                        value = search,
                        onValueChange = { search = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("Search username or email") },
                        shape = RoundedCornerShape(14.dp),
                        colors = adminFieldColors()
                    )

                    Spacer(Modifier.height(10.dp))

                    AdminButton(
                        text = "Search Users",
                        onClick = { viewModel.loadUsers(search.trim()) }
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        items(state.users, key = { it.id }) { user ->
                            AdminCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            user.username,
                                            color = AdminWhite,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            user.email.ifBlank { "No email" },
                                            color = AdminMuted,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }

                                    StatusChip(
                                        if (user.isBanned) "BANNED" else "ACTIVE",
                                        user.isBanned
                                    )
                                }

                                Text(
                                    "ID: ${user.id}  •  Rating: ${user.rating}",
                                    color = AdminMuted,
                                    style = MaterialTheme.typography.bodySmall
                                )

                                Text(
                                    "Role: ${user.role}",
                                    color = AdminMuted
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            selectedUser = user
                                            viewModel.loadUserDetails(user.id)
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Manage", color = AdminCyan)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            forceLogoutUser = user
                                        },
                                        modifier = Modifier.weight(1f),
                                        enabled = user.role != "admin"
                                    ) {
                                        Text("Force Logout", color = AdminMuted)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "pending",
                            "resolved",
                            "dismissed"
                        ).forEach { status ->
                            FilterChip(
                                selected = state.reportStatus == status,
                                onClick = {
                                    viewModel.loadReports(status)
                                },
                                label = {
                                    Text(status.replaceFirstChar { it.uppercase() })
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AdminSurface,
                                    selectedLabelColor = AdminCyan
                                )
                            )
                        }
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(state.reports, key = { it.id }) { report ->
                            AdminCard {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        "Report #${report.id}",
                                        color = AdminCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                    StatusChip(
                                        report.status.uppercase(),
                                        report.status == "pending"
                                    )
                                }

                                Text(
                                    "${report.reporterUsername} reported ${report.reportedUsername}",
                                    color = AdminWhite
                                )

                                Text(
                                    "Reason: ${report.reason}",
                                    color = AdminMuted
                                )

                                Text(
                                    report.details.ifBlank { "No additional details" },
                                    color = AdminMuted
                                )

                                if (report.status == "pending") {
                                    OutlinedButton(
                                        onClick = {
                                            selectedReport = report
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Review Report", color = AdminCyan)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            3 -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    AdminButton(
                        text = "Refresh Live Games",
                        onClick = { viewModel.loadGames() }
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(state.games, key = { it.id }) { game ->
                            AdminCard {
                                Text(
                                    "${game.whiteUsername} vs ${game.blackUsername}",
                                    color = AdminWhite,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    "Game ID: ${game.id}",
                                    color = AdminMuted,
                                    style = MaterialTheme.typography.bodySmall
                                )

                                Text(
                                    "${game.timeControlMode} • ${game.timeControlMinutes} min • ${game.moveCount} moves",
                                    color = AdminMuted
                                )

                                Text(
                                    if (game.rated) "Rated Game" else "Unrated Game",
                                    color = AdminCyan
                                )

                                OutlinedButton(
                                    onClick = {
                                        selectedGame = game
                                        viewModel.loadGameDetails(game.id)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("View Game Details", color = AdminCyan)
                                }
                            }
                        }
                    }
                }
            }

            4 -> ContentManagementSection(viewModel)

            5 -> {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(state.actions, key = { it.id }) { action ->
                        AdminCard {
                            Text(
                                action.action.replace("_", " ").uppercase(),
                                color = AdminCyan,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                "Admin: ${action.adminUsername}",
                                color = AdminWhite
                            )

                            Text(
                                "Target: ${action.targetType} #${action.targetId}",
                                color = AdminMuted
                            )

                            Text(
                                action.details,
                                color = AdminMuted
                            )

                            Text(
                                action.createdAt,
                                color = AdminMuted,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }

    selectedUser?.let { user ->
        val details = state.selectedUserDetails
            ?.takeIf { it.id == user.id }

        var reason by remember(user.id) { mutableStateOf("") }
        var days by remember(user.id) { mutableStateOf("7") }

        AlertDialog(
            onDismissRequest = {
                selectedUser = null
                viewModel.clearSelectedUser()
            },
            containerColor = AdminSurface,
            titleContentColor = AdminWhite,
            textContentColor = AdminMuted,
            title = {
                Text("Manage ${user.username}")
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (details == null) {
                        CircularProgressIndicator(color = AdminCyan)
                    } else {
                        Text("Email: ${details.email}")
                        Text("Rating: ${details.rating}")
                        Text("Games played: ${details.gamesPlayed}")
                        Text("Reports against: ${details.reportsAgainst}")
                        Text("Email verified: ${details.emailVerified}")
                        Text(
                            if (details.isBanned) "BANNED" else "ACTIVE",
                            color = if (details.isBanned) AdminRed else AdminCyan
                        )

                        if (details.isBanned) {
                            Text("Reason: ${details.banReason}")

                            if (!details.isPermanentBan) {
                                Text("Banned until: ${details.bannedUntil}")
                            }
                        } else if (details.role != "admin") {
                            OutlinedTextField(
                                value = reason,
                                onValueChange = {
                                    if (it.length <= 500) reason = it
                                },
                                label = { Text("Ban reason") },
                                minLines = 2,
                                colors = adminFieldColors()
                            )

                            OutlinedTextField(
                                value = days,
                                onValueChange = {
                                    if (it.all(Char::isDigit)) days = it
                                },
                                label = { Text("Ban days (0 = permanent)") },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                ),
                                colors = adminFieldColors()
                            )
                        }
                    }
                }
            },
            confirmButton = {
                if (details != null) {
                    TextButton(
                        enabled = details.role != "admin",
                        onClick = {
                            if (details.isBanned) {
                                viewModel.unbanUser(user.id)
                            } else {
                                val duration = days.toIntOrNull()
                                if (reason.isNotBlank() &&
                                    duration != null &&
                                    duration in 0..365
                                ) {
                                    viewModel.banUser(
                                        user.id,
                                        reason.trim(),
                                        duration
                                    )
                                }
                            }
                            selectedUser = null
                            viewModel.clearSelectedUser()
                        }
                    ) {
                        Text(
                            if (details.isBanned) "Unban" else "Ban User",
                            color = if (details.isBanned) AdminCyan else AdminRed
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        selectedUser = null
                        viewModel.clearSelectedUser()
                    }
                ) {
                    Text("Close", color = AdminMuted)
                }
            }
        )
    }

    if (selectedUser != null &&
        state.selectedUserDetails?.id != selectedUser?.id
    ) {
    }


    selectedReport?.let { report ->
        AlertDialog(
            onDismissRequest = {
                selectedReport = null
                reportNote = ""
            },
            containerColor = AdminSurface,
            titleContentColor = AdminWhite,
            textContentColor = AdminMuted,
            title = {
                Text("Resolve Report #${report.id}")
            },
            text = {
                Column {
                    Text("Reported user: ${report.reportedUsername}")
                    Text("Reason: ${report.reason}")

                    OutlinedTextField(
                        value = reportNote,
                        onValueChange = {
                            if (it.length <= 500) reportNote = it
                        },
                        label = { Text("Admin note (optional)") },
                        colors = adminFieldColors()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.closeReport(
                            report.id,
                            "resolved",
                            reportNote.trim()
                        )
                        selectedReport = null
                        reportNote = ""
                    }
                ) {
                    Text("Resolve", color = AdminCyan)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.closeReport(
                            report.id,
                            "dismissed",
                            reportNote.trim()
                        )
                        selectedReport = null
                        reportNote = ""
                    }
                ) {
                    Text("Dismiss", color = AdminRed)
                }
            }
        )
    }

    selectedGame?.let { game ->
        val details = state.selectedGame
            ?.takeIf { it.game.id == game.id }

        AlertDialog(
            onDismissRequest = {
                selectedGame = null
                viewModel.clearSelectedGame()
                abortReason = ""
            },
            containerColor = AdminSurface,
            titleContentColor = AdminWhite,
            textContentColor = AdminMuted,
            title = {
                Text("Game Details")
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (details == null) {
                        CircularProgressIndicator(color = AdminCyan)
                    } else {
                        val gameData = details.game

                        Text(
                            "${gameData.whiteUsername ?: "White"} vs ${gameData.blackUsername ?: "Black"}",
                            color = AdminWhite
                        )
                        Text("Status: ${gameData.status}")
                        Text("Mode: ${gameData.timeControlMode}")
                        Text("Moves: ${details.moves?.size ?: 0}")
                        Text("Turn: ${gameData.currentTurn ?: "Unknown"}")

                        OutlinedTextField(
                            value = abortReason,
                            onValueChange = {
                                if (it.length <= 500) abortReason = it
                            },
                            label = { Text("Abort reason") },
                            colors = adminFieldColors()
                        )
                    }
                }
            },
            confirmButton = {
                if (details?.game?.status == "active") {
                    TextButton(
                        onClick = {
                            if (abortReason.isNotBlank()) {
                                viewModel.abortGame(
                                    game.id,
                                    abortReason.trim()
                                )
                                selectedGame = null
                                abortReason = ""
                            }
                        }
                    ) {
                        Text("Abort Game", color = AdminRed)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        selectedGame = null
                        viewModel.clearSelectedGame()
                        abortReason = ""
                    }
                ) {
                    Text("Close", color = AdminMuted)
                }
            }
        )
    }
    forceLogoutUser?.let { user ->
        AlertDialog(
            onDismissRequest = {
                forceLogoutUser = null
            },
            containerColor = AdminSurface,
            titleContentColor = AdminWhite,
            textContentColor = AdminMuted,
            title = {
                Text("Force Logout")
            },
            text = {
                Text(
                    "Are you sure you want to log out ${user.username} " +
                            "from all devices? This will invalidate their sessions."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.forceLogout(user.id)
                        forceLogoutUser = null
                    }
                ) {
                    Text("Confirm", color = AdminRed)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        forceLogoutUser = null
                    }
                ) {
                    Text("Cancel", color = AdminMuted)
                }
            }
        )
    }

    if (showAdminLogoutDialog) {
        AlertDialog(
            onDismissRequest = {
                showAdminLogoutDialog = false
                confirmAdminLogout = false
            },
            containerColor = AdminSurface,
            titleContentColor = AdminWhite,
            textContentColor = AdminMuted,

            title = {
                Text(
                    text = "Confirm Logout",
                    fontWeight = FontWeight.Bold
                )
            },

            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Are you sure you want to logout from the MOSAIC Admin Panel?"
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = confirmAdminLogout,
                            onCheckedChange = {
                                confirmAdminLogout = it
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = AdminCyan,
                                uncheckedColor = AdminMuted,
                                checkmarkColor = AdminBg
                            )
                        )

                        Text(
                            text = "Yes, I confirm that I want to logout",
                            color = AdminWhite,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },

            confirmButton = {
                TextButton(
                    enabled = confirmAdminLogout,
                    onClick = {
                        showAdminLogoutDialog = false
                        confirmAdminLogout = false
                        onLogout()
                    }
                ) {
                    Text(
                        text = "Logout",
                        color = if (confirmAdminLogout) AdminRed else AdminMuted
                    )
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        showAdminLogoutDialog = false
                        confirmAdminLogout = false
                    }
                ) {
                    Text(
                        text = "Cancel",
                        color = AdminMuted
                    )
                }
            }
        )
    }
}

@Composable
private fun DashboardSection(state: com.abhinav.caisarra.presentation.viewmodel.AdminUiState) {
    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Text(
                "Platform Overview",
                color = AdminWhite,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        val stats = state.stats

        if (stats != null) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("Total Users", stats.totalUsers)
                    StatCard("New Today", stats.newUsersToday)
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("Banned Users", stats.bannedUsers)
                    StatCard("Active Games", stats.activeGames)
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("Pending Reports", stats.pendingReports)
                    StatCard("Tournaments", stats.ongoingTournaments)
                }
            }
        }

        item {
            AdminCard {
                Text(
                    "System Health",
                    color = AdminWhite,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Database: ${state.health?.database ?: "Loading"}",
                    color = if (state.health?.database == "ok") AdminCyan else AdminRed
                )

                Text(
                    "Redis: ${state.health?.redis ?: "Loading"}",
                    color = if (state.health?.redis == "ok") AdminCyan else AdminRed
                )
            }
        }

        item {
            AdminCard {
                Text(
                    "Matchmaking Queues",
                    color = AdminWhite,
                    fontWeight = FontWeight.Bold
                )

                if (state.matchmaking.isEmpty()) {
                    Text("No players waiting", color = AdminMuted)
                } else {
                    state.matchmaking.forEach { queue ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(queue.queue, color = AdminMuted)
                            Text(
                                "${queue.players} players",
                                color = AdminCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContentManagementSection(
    viewModel: AdminViewModel
) {
    var postId by remember { mutableStateOf("") }
    var commentId by remember { mutableStateOf("") }
    var clubId by remember { mutableStateOf("") }
    var tournamentId by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Text(
                "Content Moderation",
                color = AdminWhite,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Enter an existing ID to perform a moderation action.",
                color = AdminMuted,
                style = MaterialTheme.typography.bodySmall
            )
        }

        item {
            ContentActionCard(
                title = "Delete Community Post",
                description = "Permanently deletes a post and its dependent comments, reactions and bookmarks.",
                value = postId,
                onValueChange = { postId = it },
                buttonText = "Delete Post"
            ) {
                postId.toLongOrNull()?.takeIf { it > 0 }?.let {
                    viewModel.deletePost(it)
                    postId = ""
                }
            }
        }

        item {
            ContentActionCard(
                title = "Delete Comment",
                description = "Permanently deletes an individual comment.",
                value = commentId,
                onValueChange = { commentId = it },
                buttonText = "Delete Comment"
            ) {
                commentId.toLongOrNull()?.takeIf { it > 0 }?.let {
                    viewModel.deleteComment(it)
                    commentId = ""
                }
            }
        }

        item {
            ContentActionCard(
                title = "Delete Club",
                description = "Permanently deletes a club and its dependent records.",
                value = clubId,
                onValueChange = { clubId = it },
                buttonText = "Delete Club",
                numeric = false
            ) {
                if (clubId.isNotBlank()) {
                    viewModel.deleteClub(clubId.trim())
                    clubId = ""
                }
            }
        }

        item {
            ContentActionCard(
                title = "Cancel Tournament",
                description = "Force-cancels an ongoing tournament and aborts its unfinished active games.",
                value = tournamentId,
                onValueChange = { tournamentId = it },
                buttonText = "Cancel Tournament"
            ) {
                tournamentId.toLongOrNull()?.takeIf { it > 0 }?.let {
                    viewModel.cancelTournament(it)
                    tournamentId = ""
                }
            }
        }
    }
}

@Composable
private fun ContentActionCard(
    title: String,
    description: String,
    value: String,
    onValueChange: (String) -> Unit,
    buttonText: String,
    numeric: Boolean = true,
    onAction: () -> Unit
) {
    AdminCard {
        Text(title, color = AdminWhite, fontWeight = FontWeight.Bold)
        Text(description, color = AdminMuted)

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(if (numeric) "Enter ID" else "Enter UUID") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (numeric) {
                    KeyboardType.Number
                } else {
                    KeyboardType.Text
                }
            ),
            colors = adminFieldColors()
        )

        AdminButton(
            text = buttonText,
            onClick = onAction,
            enabled = value.isNotBlank()
        )
    }
}

@Composable
private fun AdminCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AdminSurface, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content
    )
}

@Composable
private fun AdminButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AdminSurface,
            contentColor = AdminCyan
        )
    ) {
        Text(text, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StatusChip(
    text: String,
    danger: Boolean
) {
    Surface(
        color = if (danger) {
            AdminRed.copy(alpha = 0.12f)
        } else {
            AdminCyan.copy(alpha = 0.12f)
        },
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            color = if (danger) AdminRed else AdminCyan,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun RowScope.StatCard(
    title: String,
    value: Int
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .background(AdminSurface, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(title, color = AdminMuted)
        Text(
            value.toString(),
            color = AdminCyan,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun adminFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = AdminWhite,
        unfocusedTextColor = AdminWhite,
        cursorColor = AdminCyan,
        focusedBorderColor = AdminCyan,
        unfocusedBorderColor = AdminBorder,
        focusedLabelColor = AdminCyan,
        unfocusedLabelColor = AdminMuted,
        focusedContainerColor = AdminBg,
        unfocusedContainerColor = AdminBg,
        focusedPlaceholderColor = AdminMuted,
        unfocusedPlaceholderColor = AdminMuted
    )
