package com.abhinav.caisarra.presentation.navigation

import android.content.Intent
import androidx.navigation.NavController
import com.abhinav.caisarra.data.remote.dto.NotificationItemDto
import com.abhinav.caisarra.data.remote.dto.getStringData

fun handleNotificationItemClick(
    notification: NotificationItemDto,
    navController: NavController
) {
    val route = resolveNotificationRoute(notification.type) { notification.getStringData(it) }
    if (route == AppRoutes.NOTIFICATIONS && navController.currentDestination?.route == AppRoutes.NOTIFICATIONS) return
    navController.navigateOnce(route)
}

fun handleIntentNotificationNavigation(
    intent: Intent,
    notifType: String?,
    navController: NavController
) {
    val type = notifType?.takeIf { it.isNotBlank() } ?: intent.getStringExtra("type")
    val dataUri = intent.data
    val route = resolveNotificationRoute(type) { key ->
        intent.getStringExtra(key) ?: if (key == "deep_link" || key == "link") intent.dataString else null
    }
    if (route == AppRoutes.NOTIFICATIONS && dataUri != null && navController.graph.hasDeepLink(dataUri)) {
        navController.navigate(dataUri) { launchSingleTop = true }
        return
    }
    navController.navigate(route) { launchSingleTop = true }
}
