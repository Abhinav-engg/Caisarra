package com.abhinav.caisarra.presentation.watch

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.abhinav.caisarra.presentation.watch.components.ActiveGameCard
import com.caisaara.ui.theme.StatsBg
import com.caisaara.ui.theme.StatsCardBg
import com.caisaara.ui.theme.StatsCardBorder
import com.caisaara.ui.theme.StatsCyan
import com.caisaara.ui.theme.StatsGreen
import com.caisaara.ui.theme.StatsTextMuted
import com.caisaara.ui.theme.StatsTextWhite
import com.caisaara.ui.theme.ThemeNeonDeep
import com.caisaara.ui.theme.ThemeNeonSky

@Composable
fun WatchScreen(
    viewModel: WatchViewModel,
    onBack: () -> Unit,
    onNavigateToSpectate: (String) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val codeQuery by viewModel.codeQuery.collectAsState()
    val games by viewModel.filteredGames.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        viewModel.navigateToWatchCode.collect { code ->
            onNavigateToSpectate(code)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StatsBg)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = StatsTextWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "WATCH",
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = StatsTextWhite,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(StatsGreen, CircleShape)
                        )
                    }
                    Text(
                        text = "Active public games",
                        fontFamily = JetBrainsMono,
                        fontSize = 11.sp,
                        color = StatsTextMuted
                    )
                }
            }

            IconButton(
                onClick = viewModel::refresh,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = StatsCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Search player username...",
                        fontFamily = JetBrainsMono,
                        fontSize = 13.sp,
                        color = StatsTextMuted
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = StatsCyan,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = StatsTextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StatsCyan,
                    unfocusedBorderColor = StatsCardBorder,
                    focusedContainerColor = StatsCardBg,
                    unfocusedContainerColor = StatsCardBg,
                    focusedTextColor = StatsTextWhite,
                    unfocusedTextColor = StatsTextWhite
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(Color(0xFF0F1722), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF1B2838), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = codeQuery,
                    onValueChange = viewModel::onCodeQueryChange,
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            text = "Paste watch code or link...",
                            fontFamily = JetBrainsMono,
                            fontSize = 12.sp,
                            color = StatsTextMuted
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ThemeNeonSky,
                        unfocusedBorderColor = Color(0xFF223244),
                        focusedContainerColor = Color(0xFF131D2A),
                        unfocusedContainerColor = Color(0xFF131D2A),
                        focusedTextColor = StatsTextWhite,
                        unfocusedTextColor = StatsTextWhite
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = {
                        focusManager.clearFocus()
                        viewModel.submitCodeOrLink()
                    })
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.submitCodeOrLink()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ThemeNeonDeep),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(48.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Text(
                        text = "JOIN",
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = StatsCyan
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LIVE GAMES (${games.size})",
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                letterSpacing = 0.5.sp,
                color = StatsTextMuted
            )

            if (isLoading) {
                CircularProgressIndicator(
                    color = StatsCyan,
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (games.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(Color(0xFF131F2E), CircleShape)
                            .border(1.dp, Color(0xFF1F3247), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = StatsCyan,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (searchQuery.isNotEmpty()) "NO MATCHING GAMES" else "NO LIVE MATCHES",
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = StatsTextWhite
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (searchQuery.isNotEmpty()) {
                            "No active games found for \"$searchQuery\"."
                        } else {
                            "All matchmade games are public for spectators by default. When players start a game, they will appear here live."
                        },
                        fontFamily = JetBrainsMono,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = StatsTextMuted,
                        lineHeight = 18.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(
                    items = games,
                    key = { it.effectiveGameId.ifBlank { it.effectiveWatchCode ?: it.hashCode().toString() } }
                ) { game ->
                    ActiveGameCard(
                        game = game,
                        onWatchClick = { viewModel.selectGame(it) }
                    )
                }
            }
        }
    }
}
