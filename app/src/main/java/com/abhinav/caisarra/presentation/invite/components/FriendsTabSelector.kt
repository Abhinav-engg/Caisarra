package com.abhinav.caisarra.presentation.invite.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.abhinav.caisarra.core.ui.components.SegmentedTabs
import com.abhinav.caisarra.presentation.invite.FriendsTab

@Composable
fun FriendsTabSelector(
    selectedTab: FriendsTab,
    friendsCount: Int,
    requestsCount: Int,
    onTabSelected: (FriendsTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(FriendsTab.ALL_FRIENDS, FriendsTab.REQUESTS)
    SegmentedTabs(
        titles = listOf("All friends · $friendsCount", "Requests · $requestsCount"),
        selectedIndex = tabs.indexOf(selectedTab),
        onSelect = { onTabSelected(tabs[it]) },
        modifier = modifier
    )
}
