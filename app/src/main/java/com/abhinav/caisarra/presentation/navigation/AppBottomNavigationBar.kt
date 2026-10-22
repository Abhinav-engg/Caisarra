package com.abhinav.caisarra.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono

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

private val NavBackground = Color(0xFF0B1017)
private val TopGlowColor = Color(0xFF133E4D)
private val ActiveCyan = Color(0xFF23CDE8)
private val InactiveSlate = Color(0xFF94A4B8)

@Composable
fun AppBottomNavigationBar(
    currentRoute: String?,
    onNavigateTo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(NavBackground)
    ) {
        // Subtle glow line on top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            NavBackground,
                            TopGlowColor,
                            ActiveCyan.copy(alpha = 0.5f),
                            TopGlowColor,
                            NavBackground
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem.entries.forEach { item ->
                val isSelected = when (item) {
                    BottomNavItem.HOME -> currentRoute == AppRoutes.HOME || currentRoute == AppRoutes.GUEST_HOME
                    BottomNavItem.COMMUNITY -> currentRoute == AppRoutes.COMMUNITY || currentRoute == AppRoutes.SOCIAL
                    else -> currentRoute == item.route
                }
                val icon = if (isSelected) item.selectedIcon else item.unselectedIcon
                val tint = if (isSelected) ActiveCyan else InactiveSlate

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onNavigateTo(item.route) }
                        )
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = item.label,
                        tint = tint,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.label,
                        color = tint,
                        fontFamily = JetBrainsMono,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
