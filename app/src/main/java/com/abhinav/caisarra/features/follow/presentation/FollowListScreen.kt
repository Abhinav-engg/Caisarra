package com.abhinav.caisarra.features.follow.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.core.ui.components.CaisaaraTopBar
import com.abhinav.caisarra.core.ui.components.SegmentedTabs
import com.abhinav.caisarra.core.ui.components.centeredContentWidth
import com.abhinav.caisarra.core.ui.theme.StatsScreenPadding
import com.abhinav.caisarra.core.ui.theme.StatsSectionSpacing
import com.abhinav.caisarra.features.follow.domain.FollowListTab
import com.abhinav.caisarra.presentation.invite.EmptyNotice
import com.caisaara.ui.theme.FriendsCyan
import com.caisaara.ui.theme.RedNormal
import com.caisaara.ui.theme.StatsBg

@Composable
fun FollowListScreen(
    viewModel: FollowListViewModel,
    title: String,
    currentUsername: String?,
    onBack: () -> Unit,
    onOpenProfile: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StatsBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        CaisaaraTopBar(title = title, onBack = onBack)

        SegmentedTabs(
            titles = FollowListTab.entries.map { stringResource(it.title) },
            selectedIndex = state.tab.ordinal,
            onSelect = { viewModel.selectTab(FollowListTab.entries[it]) },
            modifier = Modifier.centeredContentWidth().padding(horizontal = StatsScreenPadding, vertical = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize().centeredContentWidth(),
            contentPadding = PaddingValues(StatsScreenPadding),
            verticalArrangement = Arrangement.spacedBy(StatsSectionSpacing)
        ) {
            state.error?.let { item { Text(it, color = RedNormal) } }
            if (state.endReached && state.users.isEmpty()) {
                item { EmptyNotice(stringResource(state.tab.empty), "") }
            }
            items(state.users, key = { it.id }) { user ->
                FollowUserRow(
                    user = user,
                    onClick = { onOpenProfile(user.username) },
                    onToggleFollow = if (currentUsername != null && !user.username.equals(currentUsername, true)) {
                        { viewModel.toggleFollow(user) }
                    } else null
                )
            }
            if (!state.endReached) {
                item(key = "follow_list_loader_${state.tab}") {
                    LaunchedEffect(state.users.size, state.tab) { viewModel.loadMore() }
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        if (state.isLoading) CircularProgressIndicator(color = FriendsCyan)
                    }
                }
            }
        }
    }
}
