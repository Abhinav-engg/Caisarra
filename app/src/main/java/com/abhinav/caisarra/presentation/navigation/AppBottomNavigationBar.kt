package com.abhinav.caisarra.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.components.SlidingPillIndicator
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.abhinav.caisarra.core.ui.theme.GlassBorderWidth
import com.abhinav.caisarra.core.ui.theme.NavCornerRadius
import com.abhinav.caisarra.core.ui.theme.NavBarHeight
import com.caisaara.ui.theme.GlassHighlight
import com.caisaara.ui.theme.PuzzleDarkCard
import com.caisaara.ui.theme.StatsCyan
import com.caisaara.ui.theme.TextSizeMicro
import com.caisaara.ui.theme.ThemeSlateAccent

enum class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME(
        route = AppRoutes.HOME,
        label = "HOME",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    PUZZLES(
        route = AppRoutes.PUZZLES,
        label = "PUZZLES",
        selectedIcon = Icons.Filled.Extension,
        unselectedIcon = Icons.Outlined.Extension
    ),
    NEARBY(
        route = AppRoutes.NEARBY,
        label = "NEARBY",
        selectedIcon = Icons.Filled.LocationOn,
        unselectedIcon = Icons.Outlined.LocationOn
    ),
    COMMUNITY(
        route = AppRoutes.COMMUNITY,
        label = "COMMUNITY",
        selectedIcon = Icons.Filled.Groups,
        unselectedIcon = Icons.Outlined.Groups
    ),
    MENU(
        route = AppRoutes.MENU,
        label = "MENU",
        selectedIcon = Icons.Filled.Menu,
        unselectedIcon = Icons.Filled.Menu
    )
}

@Composable
fun AppBottomNavigationBar(
    currentRoute: String?,
    onNavigateTo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedIndex = BottomNavItem.entries.indexOfFirst { item ->
        when (item) {
            BottomNavItem.HOME -> currentRoute in setOf(AppRoutes.HOME, AppRoutes.GUEST_HOME, AppRoutes.GAME_SETUP)
            BottomNavItem.COMMUNITY -> currentRoute == AppRoutes.COMMUNITY || currentRoute == AppRoutes.SOCIAL
            else -> currentRoute == item.route
        }
    }

    val shape = RoundedCornerShape(topStart = NavCornerRadius, topEnd = NavCornerRadius)
    val topBorder = Brush.verticalGradient(
        colors = listOf(GlassHighlight, Color.Transparent),
        endY = with(LocalDensity.current) { NavCornerRadius.toPx() }
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(PuzzleDarkCard, shape)
            .border(GlassBorderWidth, topBorder, shape)
            .navigationBarsPadding()
            .height(NavBarHeight)
    ) {
        if (selectedIndex >= 0) {
            SlidingPillIndicator(
                selectedIndex = selectedIndex,
                itemCount = BottomNavItem.entries.size,
                modifier = Modifier.matchParentSize(),
                inset = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem.entries.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex
                val icon = if (isSelected) item.selectedIcon else item.unselectedIcon
                val tint = if (isSelected) StatsCyan else ThemeSlateAccent

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onNavigateTo(item.route) }
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = item.label,
                        tint = tint,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = item.label,
                        color = tint,
                        fontFamily = JetBrainsMono,
                        fontSize = TextSizeMicro,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
