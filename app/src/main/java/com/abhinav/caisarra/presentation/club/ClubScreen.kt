
package com.abhinav.caisarra.presentation.club

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalFocusManager

private val ClubBg = Color(0xFF080D13)
private val ClubCard = Color(0xFF151F2B)
private val ClubGreen = Color(0xFF10B981)
private val ClubText = Color(0xFFF5F5F5)
private val ClubMuted = Color(0xFFB8C2CC)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubScreen(
    viewModel: ClubViewModel,
    isRegisteredUser: Boolean,
    onBack: () -> Unit,
    onOpenClub: (String) -> Unit,
    onSignIn: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val focusManager = LocalFocusManager.current

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var showCreate by remember { mutableStateOf(false) }
    var showJoin by remember { mutableStateOf(false) }
    var clubName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var joinCode by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { focusManager.clearFocus() },
        containerColor = ClubBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Chess Clubs",
                            color = ClubText,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Find your people. Play together.",
                            color = ClubMuted,
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ClubText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ClubBg
                )
            )
        },
        floatingActionButton = {
            if (isRegisteredUser) {
                FloatingActionButton(
                    onClick = { showCreate = true },
                    containerColor = ClubGreen,
                    contentColor = ClubBg
                ) {
                    Icon(Icons.Default.Add, "Create club")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = 18.dp,
                end = 18.dp,
                top = 8.dp,
                bottom = 100.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ClubHero(
                    clubCount = state.myClubs.size,
                    onJoin = {
                        if (isRegisteredUser) showJoin = true
                        else onSignIn()
                    }
                )
            }

            if (!isRegisteredUser) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = ClubCard
                        ),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(
                            Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Clubs are for registered players",
                                color = ClubText,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Sign in to create clubs, join communities, " +
                                        "and access club features.",
                                color = ClubMuted,
                                fontSize = 13.sp
                            )
                            Button(
                                onClick = onSignIn,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ClubGreen,
                                    contentColor = ClubBg
                                )
                            ) {
                                Text("Sign in")
                            }
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = state.search,
                    onValueChange = viewModel::search,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("Search clubs...", color = ClubMuted)
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = ClubGreen
                        )
                    },
                    trailingIcon = {
                        TextButton(onClick = viewModel::submitSearch) {
                            Text("Search", color = ClubGreen)
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ClubText,
                        unfocusedTextColor = ClubText,
                        focusedLabelColor = ClubGreen,
                        unfocusedLabelColor = ClubMuted,
                        focusedPlaceholderColor = ClubMuted,
                        unfocusedPlaceholderColor = ClubMuted,
                        focusedBorderColor = ClubGreen,
                        unfocusedBorderColor = ClubMuted.copy(alpha = .65f),
                        cursorColor = ClubGreen
                    )
                )
            }

            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(ClubCard, RoundedCornerShape(14.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ClubTab.values().forEach { tab ->
                        val selected = state.tab == tab
                        Box(
                            Modifier
                                .weight(1f)
                                .background(
                                    if (selected) ClubGreen else Color.Transparent,
                                    RoundedCornerShape(11.dp)
                                )
                                .clickable { viewModel.selectTab(tab) }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (tab == ClubTab.DISCOVER) "Discover" else "My Clubs",
                                color = if (selected) ClubBg else ClubMuted,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (state.tab == ClubTab.DISCOVER)
                            "Explore clubs"
                        else "Your communities",
                        color = ClubText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = viewModel::refresh) {
                        Text("Refresh", color = ClubGreen)
                    }
                }
            }

            state.error?.let { message ->
                item {
                    Text(
                        message,
                        color = Color(0xFFFF8F8F),
                        fontSize = 13.sp
                    )
                }
            }

            state.info?.let { message ->
                item {
                    Text(message, color = ClubGreen, fontSize = 13.sp)
                }
            }

            if (state.isLoading) {
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = ClubGreen)
                    }
                }
            } else {
                val displayedClubs =
                    if (state.tab == ClubTab.DISCOVER) state.clubs
                    else state.myClubs

                if (displayedClubs.isEmpty()) {
                    item {
                        EmptyClubs(isMine = state.tab == ClubTab.MINE)
                    }
                }

                items(
                    items = displayedClubs,
                    key = { it.effectiveId }
                ) { club ->
                    ClubCard(
                        name = club.name,
                        description = club.description.orEmpty(),
                        memberCount = club.memberCount,
                        shareCode = club.shareCode,
                        isMine = state.myClubs.any {
                            it.effectiveId == club.effectiveId
                        },
                        onClick = {
                            if (club.effectiveId.isNotBlank()) {
                                onOpenClub(club.effectiveId)
                            }
                        },
                        onJoin = {
                            if (isRegisteredUser) {
                                viewModel.joinById(club.effectiveId)
                            } else {
                                onSignIn()
                            }
                        }
                    )
                }
            }
        }
    }

    if (showCreate) {
        AlertDialog(
            onDismissRequest = { showCreate = false },
            containerColor = ClubCard,
            title = { Text("Create a club", color = ClubText) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = clubName,
                        onValueChange = { clubName = it },
                        label = { Text("Club name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ClubText,
                            unfocusedTextColor = ClubText,
                            focusedLabelColor = ClubGreen,
                            unfocusedLabelColor = ClubMuted,
                            cursorColor = ClubGreen,
                            focusedBorderColor = ClubGreen,
                            unfocusedBorderColor = ClubMuted.copy(alpha = 0.7f)
                        )
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        minLines = 2,
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ClubText,
                            unfocusedTextColor = ClubText,
                            focusedLabelColor = ClubGreen,
                            unfocusedLabelColor = ClubMuted,
                            cursorColor = ClubGreen,
                            focusedBorderColor = ClubGreen,
                            unfocusedBorderColor = ClubMuted.copy(alpha = 0.7f)
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !state.isSubmitting && clubName.isNotBlank(),
                    onClick = {
                        viewModel.createClub(clubName, description)
                        showCreate = false
                        clubName = ""
                        description = ""
                    }
                ) {
                    Text("Create", color = ClubGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreate = false }) {
                    Text("Cancel", color = ClubMuted)
                }
            }
        )
    }

    if (showJoin) {
        AlertDialog(
            onDismissRequest = { showJoin = false },
            containerColor = ClubCard,
            title = { Text("Join with a code", color = ClubText) },
            text = {
                OutlinedTextField(
                    value = joinCode,
                    onValueChange = { joinCode = it },
                    label = { Text("Club share code") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ClubText,
                        unfocusedTextColor = ClubText,
                        focusedLabelColor = ClubGreen,
                        unfocusedLabelColor = ClubMuted,
                        cursorColor = ClubGreen,
                        focusedBorderColor = ClubGreen,
                        unfocusedBorderColor = ClubMuted.copy(alpha = 0.7f)
                    )
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !state.isSubmitting && joinCode.isNotBlank(),
                    onClick = {
                        viewModel.joinByCode(joinCode)
                        showJoin = false
                        joinCode = ""
                    }
                ) {
                    Text("Join club", color = ClubGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showJoin = false }) {
                    Text("Cancel", color = ClubMuted)
                }
            }
        )
    }
}

@Composable
private fun ClubHero(
    clubCount: Int,
    onJoin: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ClubCard)
    ) {
        Column(
            Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Groups,
                contentDescription = null,
                tint = ClubGreen,
                modifier = Modifier.size(34.dp)
            )
            Text(
                "Your next move starts here.",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 30.sp
            )
            Text(
                "$clubCount clubs in your collection",
                color = ClubMuted,
                fontSize = 13.sp
            )
            OutlinedButton(
                onClick = onJoin,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ClubGreen
                )
            ) {
                Icon(Icons.Default.Key, null)
                Spacer(Modifier.width(8.dp))
                Text("Join with code")
            }
        }
    }
}

@Composable
private fun ClubCard(
    name: String,
    description: String,
    memberCount: Int,
    shareCode: String?,
    isMine: Boolean,
    onClick: () -> Unit,
    onJoin: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ClubCard)
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(48.dp)
                        .background(
                            Color(0xFF203344),
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.SportsEsports,
                        contentDescription = null,
                        tint = ClubGreen
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        name,
                        color = ClubText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Text(
                        "$memberCount members",
                        color = ClubMuted,
                        fontSize = 12.sp
                    )
                }
                if (isMine) {
                    Surface(
                        color = Color(0xFF153B32),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            "JOINED",
                            modifier = Modifier.padding(
                                horizontal = 9.dp,
                                vertical = 5.dp
                            ),
                            color = ClubGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (description.isNotBlank()) {
                Text(
                    description,
                    color = ClubMuted,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }

            if (!shareCode.isNullOrBlank()) {
                Text(
                    "Share code: $shareCode",
                    color = ClubGreen,
                    fontSize = 12.sp
                )
            }

            if (!isMine) {
                Button(
                    onClick = onJoin,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ClubGreen,
                        contentColor = ClubBg
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Join club", fontWeight = FontWeight.Bold)
                }
            } else {
                Text("Tap to view club", color = ClubGreen, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun EmptyClubs(isMine: Boolean) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Groups,
            contentDescription = null,
            tint = ClubMuted,
            modifier = Modifier.size(44.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            if (isMine) "Your club journey starts here" else "No clubs found",
            color = ClubText,
            fontWeight = FontWeight.Bold
        )
        Text(
            if (isMine)
                "Create a club or join one with a share code."
            else
                "Try another search or check back later.",
            color = ClubMuted,
            fontSize = 13.sp
        )
    }
}
