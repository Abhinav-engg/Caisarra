
package com.abhinav.caisarra.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

private val AdminBackground = Color(0xFF080D13)
private val AdminCardSurface = Color(0xFF101923)
private val AdminCyan = Color(0xFF00D5E8)
private val AdminTextWhite = Color(0xFFFFFFFF)
private val AdminTextSecondary = Color(0xFFB8C0CA)
private val AdminBorder = Color(0xFF293440)
private val AdminDanger = Color(0xFFFF5C65)

private val AdminTabs = listOf(
    "Dashboard",
    "Users",
    "Reports",
    "Activity"
)

@Composable
fun AdminScreen(
    viewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    var search by remember { mutableStateOf("") }

    var selectedReport by remember {
        mutableStateOf<AdminReport?>(null)
    }

    var selectedUser by remember {
        mutableStateOf<AdminUser?>(null)
    }

    var banReason by remember { mutableStateOf("") }
    var banDays by remember { mutableStateOf("7") }

    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            0 -> viewModel.loadDashboard()
            1 -> viewModel.loadUsers()
            2 -> viewModel.loadReports()
            3 -> viewModel.loadActions()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminBackground)
            .statusBarsPadding()
    ) {

        CaisaaraTopBar(
            title = "ADMIN PANEL",
            onBack = onBack,
            titleColor = AdminCyan,
            iconTint = AdminCyan,
            backgroundColor = AdminBackground,
            height = 56.dp
        )

        Text(
            text = "Manage MOSAIC",
            color = AdminTextWhite,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(
                start = 20.dp,
                end = 20.dp,
                top = 8.dp,
                bottom = 20.dp
            )
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AdminTabs.forEachIndexed { index, tab ->

                val isSelected = selectedTab == index

                Box(
                    modifier = Modifier
                        .background(
                            color = if (isSelected) {
                                AdminCardSurface
                            } else {
                                AdminBackground
                            },
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable {
                            selectedTab = index
                        }
                        .padding(
                            horizontal = 20.dp,
                            vertical = 12.dp
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        color = if (isSelected) {
                            AdminCyan
                        } else {
                            AdminTextSecondary
                        },
                        fontWeight = if (isSelected) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Medium
                        },
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {

            Column(
                modifier = Modifier.fillMaxSize()
            ) {

                state.error?.let {
                    Text(
                        text = it,
                        color = AdminDanger,
                        modifier = Modifier.padding(
                            horizontal = 4.dp,
                            vertical = 6.dp
                        )
                    )
                }

                state.message?.let {
                    Text(
                        text = it,
                        color = AdminCyan,
                        modifier = Modifier.padding(
                            horizontal = 4.dp,
                            vertical = 6.dp
                        )
                    )
                }

                if (state.loading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        color = AdminCyan,
                        trackColor = AdminCardSurface
                    )
                }

                when (selectedTab) {

                    0 -> DashboardContent(
                        stats = state.stats
                    )

                    1 -> {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {

                            OutlinedTextField(
                                value = search,
                                onValueChange = {
                                    search = it
                                },
                                modifier = Modifier.fillMaxWidth(),
                                label = {
                                    Text("Search username or email")
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = adminTextFieldColors()
                            )

                            Spacer(Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    viewModel.loadUsers(search)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AdminCardSurface,
                                    contentColor = AdminCyan
                                )
                            ) {
                                Text(
                                    "Search Users",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(
                                    state.users,
                                    key = { it.id }
                                ) { user ->

                                    AdminCard {

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(
                                                modifier = Modifier.weight(1f),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    user.username,
                                                    color = AdminTextWhite,
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.titleMedium
                                                )

                                                Text(
                                                    user.email,
                                                    color = AdminTextSecondary,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }

                                            AdminStatusChip(
                                                text = if (user.isBanned) {
                                                    "BANNED"
                                                } else {
                                                    "ACTIVE"
                                                },
                                                isDanger = user.isBanned
                                            )
                                        }

                                        HorizontalDivider(
                                            color = AdminBorder.copy(alpha = 0.5f)
                                        )

                                        Text(
                                            "Rating: ${user.rating}  •  Role: ${user.role}",
                                            color = AdminTextSecondary,
                                            style = MaterialTheme.typography.bodySmall
                                        )

                                        Spacer(Modifier.height(2.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    selectedUser = user
                                                },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    contentColor = AdminCyan
                                                ),
                                                border = androidx.compose.foundation.BorderStroke(
                                                    1.dp,
                                                    AdminCyan.copy(alpha = 0.6f)
                                                )
                                            ) {
                                                Text("Manage")
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    viewModel.forceLogout(user.id)
                                                },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    contentColor = AdminTextSecondary
                                                ),
                                                border = androidx.compose.foundation.BorderStroke(
                                                    1.dp,
                                                    AdminBorder
                                                )
                                            ) {
                                                Text(
                                                    "Force Logout",
                                                    style = MaterialTheme.typography.labelMedium
                                                )
                                            }
                                        }
                                    }
                                }

                                item {
                                    Spacer(Modifier.height(16.dp))
                                }
                            }
                        }
                    }
                    2 -> {
                        Column(
                            modifier = Modifier.fillMaxSize()
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

                                    val isSelected =
                                        state.reportStatus == status

                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (isSelected) {
                                                    AdminCardSurface
                                                } else {
                                                    AdminBackground
                                                },
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                viewModel.loadReports(status)
                                            }
                                            .padding(
                                                horizontal = 16.dp,
                                                vertical = 10.dp
                                            )
                                    ) {
                                        Text(
                                            status.replaceFirstChar {
                                                it.uppercase()
                                            },
                                            color = if (isSelected) {
                                                AdminCyan
                                            } else {
                                                AdminTextSecondary
                                            },
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(
                                    state.reports,
                                    key = { it.id }
                                ) { report ->

                                    AdminCard {

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "Report #${report.id}",
                                                color = AdminCyan,
                                                fontWeight = FontWeight.Bold
                                            )

                                            AdminStatusChip(
                                                text = report.status.uppercase(),
                                                isDanger = report.status == "pending"
                                            )
                                        }

                                        Text(
                                            "${report.reporterUsername} reported ${report.reportedUsername}",
                                            color = AdminTextWhite,
                                            fontWeight = FontWeight.Medium
                                        )

                                        Text(
                                            "Reason: ${report.reason}",
                                            color = AdminTextWhite
                                        )

                                        Text(
                                            report.details,
                                            color = AdminTextSecondary,
                                            style = MaterialTheme.typography.bodySmall
                                        )

                                        if (report.status == "pending") {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Button(
                                                    onClick = {
                                                        selectedReport = report
                                                    },
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = AdminCardSurface,
                                                        contentColor = AdminCyan
                                                    )
                                                ) {
                                                    Text("Resolve")
                                                }

                                                OutlinedButton(
                                                    onClick = {
                                                        viewModel.closeReport(
                                                            report.id,
                                                            "dismissed"
                                                        )
                                                    },
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = ButtonDefaults.outlinedButtonColors(
                                                        contentColor = AdminDanger
                                                    ),
                                                    border = androidx.compose.foundation.BorderStroke(
                                                        1.dp,
                                                        AdminDanger.copy(alpha = 0.5f)
                                                    )
                                                ) {
                                                    Text("Dismiss")
                                                }
                                            }
                                        }
                                    }
                                }

                                item {
                                    Spacer(Modifier.height(16.dp))
                                }
                            }
                        }
                    }
                    3 -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(
                                state.actions,
                                key = { it.id }
                            ) { action ->

                                AdminCard {

                                    Text(
                                        action.action,
                                        color = AdminCyan,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        "Admin: ${action.adminUsername}",
                                        color = AdminTextWhite
                                    )

                                    Text(
                                        "Target: ${action.targetType} #${action.targetId}",
                                        color = AdminTextWhite
                                    )

                                    Text(
                                        action.details,
                                        color = AdminTextSecondary
                                    )

                                    Text(
                                        action.createdAt,
                                        color = AdminTextSecondary,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                            item {
                                Spacer(Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
    selectedUser?.let { user ->

        AlertDialog(
            onDismissRequest = {
                selectedUser = null
                banReason = ""
                banDays = "7"
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = AdminCardSurface,
            tonalElevation = 0.dp,
            titleContentColor = AdminTextWhite,
            textContentColor = AdminTextSecondary,

            title = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Manage User",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        user.username,
                        color = AdminCyan,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },

            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                AdminBackground,
                                RoundedCornerShape(14.dp)
                            )
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            user.email,
                            color = AdminTextWhite
                        )

                        Text(
                            "Rating: ${user.rating}",
                            color = AdminTextSecondary
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Account Status",
                                color = AdminTextSecondary
                            )

                            AdminStatusChip(
                                text = if (user.isBanned) "BANNED" else "ACTIVE",
                                isDanger = user.isBanned
                            )
                        }
                    }

                    if (user.isBanned) {

                        Text(
                            "This user is currently banned. You can restore their account using the Unban action.",
                            color = AdminTextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )

                    } else {

                        Text(
                            "Ban User",
                            color = AdminTextWhite,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleSmall
                        )

                        OutlinedTextField(
                            value = banReason,
                            onValueChange = {
                                banReason = it
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                Text("Ban reason")
                            },
                            placeholder = {
                                Text("Enter reason for banning")
                            },
                            minLines = 2,
                            maxLines = 3,
                            shape = RoundedCornerShape(14.dp),
                            colors = adminTextFieldColors()
                        )

                        OutlinedTextField(
                            value = banDays,
                            onValueChange = {
                                if (it.all(Char::isDigit)) {
                                    banDays = it
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                Text("Ban duration")
                            },
                            placeholder = {
                                Text("7")
                            },
                            supportingText = {
                                Text("Enter 0 for permanent ban")
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            shape = RoundedCornerShape(14.dp),
                            colors = adminTextFieldColors()
                        )
                    }
                }
            },

            confirmButton = {
                Button(
                    onClick = {
                        if (user.isBanned) {
                            viewModel.unbanUser(user.id)
                            selectedUser = null
                        } else {
                            val days = banDays.toIntOrNull()

                            when {
                                banReason.isBlank() -> Unit

                                banReason.length > 500 -> Unit

                                days == null || days !in 0..365 -> Unit

                                else -> {
                                    viewModel.banUser(
                                        user.id,
                                        banReason.trim(),
                                        days
                                    )

                                    selectedUser = null
                                    banReason = ""
                                    banDays = "7"
                                }
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (user.isBanned) {
                            AdminCyan
                        } else {
                            AdminDanger
                        },
                        contentColor = AdminBackground
                    )
                ) {
                    Text(
                        if (user.isBanned) "Unban User" else "Ban User",
                        fontWeight = FontWeight.Bold
                    )
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        selectedUser = null
                        banReason = ""
                        banDays = "7"
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = AdminTextSecondary
                    )
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    selectedReport?.let { report ->

        AlertDialog(
            onDismissRequest = {
                selectedReport = null
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = AdminCardSurface,
            tonalElevation = 0.dp,
            titleContentColor = AdminTextWhite,
            textContentColor = AdminTextSecondary,

            title = {
                Text(
                    "Resolve Report #${report.id}",
                    fontWeight = FontWeight.Bold
                )
            },

            text = {
                Text(
                    "Are you sure you want to mark this report as resolved?"
                )
            },

            confirmButton = {
                Button(
                    onClick = {
                        viewModel.closeReport(
                            report.id,
                            "resolved"
                        )
                        selectedReport = null
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AdminCyan,
                        contentColor = AdminBackground
                    )
                ) {
                    Text("Resolve")
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        selectedReport = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = AdminTextSecondary
                    )
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DashboardContent(
    stats: AdminStats?
) {
    if (stats == null) return

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        item {
            Text(
                text = "Platform Overview",
                color = AdminTextWhite,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(
                    top = 4.dp,
                    bottom = 4.dp
                )
            )
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total Users",
                    value = stats.totalUsers
                )

                StatCard(
                    title = "New Today",
                    value = stats.newUsersToday
                )
            }
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Banned Users",
                    value = stats.bannedUsers
                )

                StatCard(
                    title = "Active Games",
                    value = stats.activeGames
                )
            }
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Pending Reports",
                    value = stats.pendingReports
                )

                StatCard(
                    title = "Ongoing Tournaments",
                    value = stats.ongoingTournaments
                )
            }
        }

        item {
            Spacer(Modifier.height(12.dp))
        }
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
            .background(
                color = AdminCardSurface,
                shape = RoundedCornerShape(22.dp)
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            color = AdminTextSecondary,
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = value.toString(),
            color = AdminCyan,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
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
            .background(
                color = AdminCardSurface,
                shape = RoundedCornerShape(18.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content
    )
}

@Composable
private fun AdminStatusChip(
    text: String,
    isDanger: Boolean
) {
    Box(
        modifier = Modifier
            .background(
                color = if (isDanger) {
                    AdminDanger.copy(alpha = 0.12f)
                } else {
                    AdminCyan.copy(alpha = 0.12f)
                },
                shape = RoundedCornerShape(50)
            )
            .padding(
                horizontal = 10.dp,
                vertical = 5.dp
            )
    ) {
        Text(
            text = text,
            color = if (isDanger) AdminDanger else AdminCyan,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun adminTextFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = AdminTextWhite,
        unfocusedTextColor = AdminTextWhite,
        disabledTextColor = AdminTextSecondary,

        cursorColor = AdminCyan,
        errorCursorColor = AdminDanger,

        focusedContainerColor = AdminBackground,
        unfocusedContainerColor = AdminBackground,
        disabledContainerColor = AdminBackground,
        errorContainerColor = AdminBackground,

        focusedBorderColor = AdminCyan,
        unfocusedBorderColor = AdminBorder,
        disabledBorderColor = AdminBorder,
        errorBorderColor = AdminDanger,

        focusedLabelColor = AdminCyan,
        unfocusedLabelColor = AdminTextSecondary,
        disabledLabelColor = AdminTextSecondary,
        errorLabelColor = AdminDanger,

        focusedPlaceholderColor = AdminTextSecondary,
        unfocusedPlaceholderColor = AdminTextSecondary
    )
