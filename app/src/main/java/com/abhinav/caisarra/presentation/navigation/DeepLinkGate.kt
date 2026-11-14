package com.abhinav.caisarra.presentation.navigation

import android.net.Uri
import androidx.navigation.NavController
import com.abhinav.caisarra.data.repository.AuthRepository

enum class DeepLinkDecision { Wait, Open, NeedsAccount, Drop }

private val unsettledRoutes = setOf(
    AppRoutes.SPLASH,
    AppRoutes.ONBOARDING
)

private val signedOutLandingRoutes = setOf(
    AppRoutes.LOGIN,
    AppRoutes.GUEST_HOME
)

private val webHosts = setOf("caisaara.app", "caisaara.duckdns.org")

private fun Uri.isInviteLink(): Boolean =
    scheme.equals("https", ignoreCase = true) &&
        host?.lowercase() in webHosts &&
        pathSegments.firstOrNull() == "play"

suspend fun decideDeepLink(
    uri: Uri,
    currentRoute: String?,
    preAuthRoutes: Set<String>,
    navController: NavController,
    authRepository: AuthRepository
): DeepLinkDecision {
    if (currentRoute == null || currentRoute in unsettledRoutes) return DeepLinkDecision.Wait
    if (!navController.graph.hasDeepLink(uri)) return DeepLinkDecision.Drop
    if (authRepository.isLoggedIn()) {
        return if (currentRoute in preAuthRoutes) DeepLinkDecision.Wait else DeepLinkDecision.Open
    }
    if (uri.isInviteLink() && currentRoute in signedOutLandingRoutes) return DeepLinkDecision.Open
    return DeepLinkDecision.NeedsAccount
}
