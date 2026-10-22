
package com.abhinav.caisarra.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.presentation.viewmodel.ProfileViewModel
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.abhinav.caisarra.presentation.game.viewmodel.GameMode
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem



private val ProfileBackground = Color(0xFF080D13)
private val ProfileCard = Color(0xFF161F2D)
private val ProfileCardSecondary = Color(0xFF101722)
private val ProfileCyan = Color(0xFF00D9E9)
private val ProfileTeal = Color(0xFF208A94)
private val ProfileWhite = Color(0xFFF4F7FB)
private val ProfileMuted = Color(0xFF9CAABD)
private val ProfileBorder = Color(0xFF273446)
private val ProfileError = Color(0xFFFF7777)

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    isGuest: Boolean,
    onBack: () -> Unit,
    onEditProfile: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current
    var editing by remember { mutableStateOf(false) }
    var displayName by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    LaunchedEffect(state.profile) {
        state.profile?.let { profile ->
            displayName = profile.displayName.orEmpty()
            country = profile.country.orEmpty()
            bio = profile.bio.orEmpty()
            avatarUrl = profile.avatarUrl.orEmpty()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ProfileBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        focusManager.clearFocus()
                    }
                )
            }
            .padding(
                start = 22.dp,
                end = 22.dp,
                top = 16.dp,
                bottom = 24.dp
            ),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(ProfileCard)
                    .border(
                        width = 1.dp,
                        color = ProfileBorder,
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ProfileWhite,
                        modifier = Modifier.size(23.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Player Profile",
                    color = ProfileWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.sp,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "Your chess identity",
                    color = ProfileMuted,
                    fontSize = 12.sp
                )
            }

            var settingsExpanded by remember { mutableStateOf(false) }
            Box {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ProfileCard)
                        .border(1.dp, ProfileBorder, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { settingsExpanded = true },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Profile settings",
                            tint = ProfileWhite,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = settingsExpanded,
                    onDismissRequest = { settingsExpanded = false },
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(ProfileCard)
                        .border(1.dp, ProfileBorder, RoundedCornerShape(18.dp)),
                    containerColor = ProfileCard,
                    shape = RoundedCornerShape(18.dp),
                    shadowElevation = 10.dp,
                    tonalElevation = 0.dp
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Edit Profile",
                                color = ProfileWhite,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        onClick = {
                            settingsExpanded = false
                            onEditProfile()
                        },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }

        if (state.loading && state.profile == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = ProfileCyan
                )
            }
        }

        state.error?.let { message ->
            MessageCard(
                message = message,
                color = ProfileError
            )
        }

        state.message?.let { message ->
            MessageCard(
                message = message,
                color = ProfileCyan
            )
        }

        state.profile?.let { profile ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1A2B3A),
                                ProfileCard,
                                Color(0xFF13232E)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = ProfileBorder,
                        shape = RoundedCornerShape(26.dp)
                    )
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        ProfileCyan,
                                        ProfileTeal
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (
                                    profile.displayName
                                        ?.takeIf { it.isNotBlank() }
                                        ?: profile.username
                                    ).take(1).uppercase(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = ProfileBackground
                        )

                        profile.avatarUrl
                            ?.trim()
                            ?.takeIf { it.startsWith("https://", ignoreCase = true) }
                            ?.let { url ->
                                AsyncImage(
                                    model = url,
                                    contentDescription = "Profile picture",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(76.dp)
                                        .clip(CircleShape)
                                )
                            }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = profile.displayName
                                ?.takeIf { it.isNotBlank() }
                                ?: profile.username,
                            color = ProfileWhite,
                            fontSize = 23.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "@${profile.username}",
                            color = ProfileCyan,
                            fontSize = 14.sp
                        )

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFF073743))
                                .border(1.dp, Color(0xFF0B5965), RoundedCornerShape(50))
                                .padding(horizontal = 9.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(ProfileCyan)
                            )
                            Text(
                                text = if (isGuest) "GUEST PLAYER" else "MOSAIC PLAYER",
                                color = ProfileCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.7.sp,
                                maxLines = 1
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(ProfileBorder)
                )

                if (profile.email.isNotBlank()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SectionLabel("EMAIL ADDRESS")
                        Text(
                            text = profile.email,
                            color = ProfileWhite,
                            fontSize = 14.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InfoChip(
                        label = "COUNTRY",
                        value = profile.country
                            ?.takeIf { it.isNotBlank() }
                            ?: "Not set",
                        modifier = Modifier.weight(1f)
                    )

                    InfoChip(
                        label = "MEMBER SINCE",
                        value = profile.joinedAt
                            ?.substringBefore('T')
                            ?: "Unknown",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(ProfileCard)
                    .border(
                        1.dp,
                        ProfileBorder,
                        RoundedCornerShape(24.dp)
                    )
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        SectionLabel("YOUR CURRENT STRENGTH")
                        Spacer(Modifier.height(7.dp))
                        Text(
                            text = profile.rating?.toString() ?: "—",
                            fontSize = 43.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ProfileCyan
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(ProfileCardSecondary)
                            .padding(horizontal = 13.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = if (profile.provisional) {
                                "PROVISIONAL"
                            } else {
                                "ESTABLISHED"
                            },
                            color = if (profile.provisional) {
                                ProfileCyan
                            } else {
                                Color(0xFF79D6A5)
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = profile.skillLevel
                        ?.takeIf { it.isNotBlank() }
                        ?.replaceFirstChar { it.uppercase() }
                        ?: "Skill level not set",
                    color = ProfileWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = if (profile.provisional) {
                        "Your rating is still being established through games. Play rated matches to unlock your competitive tier."
                    } else {
                        "Your rating reflects your current playing strength. Keep playing rated games to improve."
                    },
                    color = ProfileMuted,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )

                if (profile.provisional) {
                    Spacer(Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "RATING CALIBRATION",
                            color = ProfileMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.7.sp
                        )
                        Text(
                            text = "Play rated games",
                            color = ProfileCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(50))
                            .background(ProfileCardSecondary)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.22f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    Brush.horizontalGradient(listOf(ProfileCyan, ProfileTeal))
                                )
                        )
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SectionHeading(
                    title = "RATING BREAKDOWN",
                    subtitle = "Your strength across game modes"
                )

                val modes = GameMode.entries

                modes.chunked(2).forEach { rowModes ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowModes.forEach { mode ->
                            val rating = profile.ratings?.get(mode.ratingKey)

                            RatingModeCard(
                                title = mode.label,
                                rating = rating?.rating,
                                provisional = rating?.provisional ?: false,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (rowModes.size == 1) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SectionHeading(
                    title = "ABOUT YOU",
                    subtitle = "A little about the player"
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(ProfileCard)
                        .border(
                            1.dp,
                            ProfileBorder,
                            RoundedCornerShape(22.dp)
                        )
                        .clickable(enabled = !isGuest) { onEditProfile() }
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SectionLabel("PLAYER BIO")
                        if (!isGuest) {
                            Text(
                                text = if (profile.bio.isNullOrBlank()) "+ Add bio" else "Edit bio",
                                color = ProfileCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = profile.bio
                            ?.takeIf { it.isNotBlank() }
                            ?: "No bio added yet.",
                        color = if (profile.bio.isNullOrBlank()) {
                            ProfileMuted
                        } else {
                            ProfileWhite
                        },
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SectionHeading(
    title: String,
    subtitle: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(
            text = title,
            color = ProfileWhite,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.7.sp
        )
        Text(
            text = subtitle,
            color = ProfileMuted,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = ProfileMuted,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.1.sp
    )
}

@Composable
private fun InfoChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(ProfileCardSecondary)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        SectionLabel(label)
        Text(
            text = value,
            color = ProfileWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun RatingModeCard(
    title: String,
    rating: Int?,
    provisional: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(19.dp))
            .background(ProfileCard)
            .border(
                1.dp,
                ProfileBorder,
                RoundedCornerShape(19.dp)
            )
            .padding(17.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(ProfileCyan)
            )

            Text(
                text = title.uppercase(),
                color = ProfileMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.7.sp
            )
        }

        Text(
            text = rating?.toString() ?: "—",
            color = ProfileWhite,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = when {
                rating == null -> "Not rated"
                provisional -> "Provisional"
                else -> "Rated"
            },
            color = if (provisional && rating != null) {
                ProfileCyan
            } else {
                ProfileMuted
            },
            fontSize = 11.sp
        )
    }
}

@Composable
private fun ProfileInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    singleLine: Boolean = false,
    supportingText: String? = null,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val bringIntoViewRequester = remember {
        BringIntoViewRequester()
    }
    val coroutineScope = rememberCoroutineScope()
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
            .onFocusEvent { focusState ->
                if (focusState.isFocused) {
                    coroutineScope.launch {
                        bringIntoViewRequester.bringIntoView()
                    }
                }
            },
        singleLine = singleLine,
        supportingText = supportingText?.let {
            { Text(it, color = ProfileMuted) }
        },
        keyboardOptions = KeyboardOptions(
            capitalization = capitalization,
            keyboardType = keyboardType,
            imeAction = ImeAction.Done
        ),
        shape = RoundedCornerShape(15.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = ProfileWhite,
            unfocusedTextColor = ProfileWhite,
            focusedBorderColor = ProfileCyan,
            unfocusedBorderColor = ProfileBorder,
            focusedLabelColor = ProfileCyan,
            unfocusedLabelColor = ProfileMuted,
            cursorColor = ProfileCyan,
            focusedContainerColor = ProfileCardSecondary,
            unfocusedContainerColor = ProfileCardSecondary
        )
    )
}

@Composable
private fun MessageCard(
    message: String,
    color: Color
) {
    Text(
        text = message,
        color = color,
        fontSize = 13.sp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(ProfileCard)
            .padding(14.dp)
    )
}

@Composable
private fun GradientActionButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(17.dp))
            .background(
                if (enabled) {
                    Brush.horizontalGradient(
                        listOf(ProfileCyan, ProfileTeal)
                    )
                } else {
                    Brush.horizontalGradient(
                        listOf(ProfileBorder, ProfileBorder)
                    )
                }
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) ProfileBackground else ProfileMuted,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp
        )
    }
}
