package com.abhinav.caisarra.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.abhinav.caisarra.presentation.viewmodel.ProfileViewModel
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding

private val AvatarScreenBg = Color(0xFF080D13)
private val AvatarScreenCard = Color(0xFF101923)
private val AvatarScreenBorder = Color(0xFF29384A)
private val AvatarScreenText = Color(0xFFF3F6FC)
private val AvatarScreenMuted = Color(0xFF9BAABD)
private val AvatarScreenAccent = Color(0xFF00D8E8)

@Composable
fun DefaultChessAvatarsScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit,
    onAvatarSelected: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AvatarScreenBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = AvatarScreenText
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "DEFAULT CHESS AVATARS",
                    color = AvatarScreenText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Choose your player identity",
                    color = AvatarScreenMuted,
                    fontSize = 11.sp
                )
            }

            Spacer(Modifier.size(48.dp))
        }

        Text(
            text = "Choose an avatar for your MOSAIC profile.",
            color = AvatarScreenMuted,
            fontSize = 13.sp,
            modifier = Modifier.padding(
                start = 20.dp,
                end = 20.dp,
                top = 8.dp,
                bottom = 18.dp
            )
        )

        when {
            state.loading && state.defaultAvatars.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AvatarScreenAccent)
                }
            }

            state.defaultAvatars.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.error
                            ?: "No default chess avatars are available.",
                        color = AvatarScreenMuted
                    )
                }
            }

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 28.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = state.defaultAvatars,
                        key = { it.id }
                    ) { avatar ->
                        val isSelected =
                            state.profile?.avatarUrl == avatar.url

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(AvatarScreenCard)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) {
                                        AvatarScreenAccent
                                    } else {
                                        AvatarScreenBorder
                                    },
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable(enabled = !state.saving) {
                                    viewModel.selectDefaultAvatar(avatar.id)
                                    onAvatarSelected()
                                }
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AsyncImage(
                                model = avatar.url,
                                contentDescription = "${avatar.id} chess avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AvatarScreenBg)
                            )

                            Spacer(Modifier.height(10.dp))

                            Text(
                                text = avatar.id
                                    .replace('_', ' ')
                                    .replaceFirstChar { it.uppercase() },
                                color = AvatarScreenText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            if (isSelected) {
                                Spacer(Modifier.height(5.dp))

                                Text(
                                    text = "SELECTED",
                                    color = AvatarScreenAccent,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}