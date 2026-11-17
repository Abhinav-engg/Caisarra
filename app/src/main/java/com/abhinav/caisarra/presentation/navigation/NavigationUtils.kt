package com.abhinav.caisarra.presentation.navigation

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import com.abhinav.caisarra.data.local.OnboardingPreferencesStore
import com.abhinav.caisarra.data.local.PendingInviteStore
import com.abhinav.caisarra.data.local.PendingOtpStore
import com.abhinav.caisarra.data.local.TokenManager
import com.abhinav.caisarra.data.remote.RetrofitInstance
import com.abhinav.caisarra.data.remote.dto.ProfileData
import com.abhinav.caisarra.data.repository.AuthRepository
import com.abhinav.caisarra.data.repository.ProfileRepository
import com.abhinav.caisarra.presentation.viewmodel.VerifyPurpose
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
inline fun <reified VM : ViewModel> screenViewModel(
    key: String? = null,
    crossinline create: () -> VM
): VM {
    return viewModel(
        key = key,
        factory = viewModelFactory {
            initializer {
                create()
            }
        }
    )
}

fun NavController.isResumed(): Boolean {
    return currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED
}

fun NavController.navigateOnce(
    route: String,
    builder: NavOptionsBuilder.() -> Unit = {}
) {
    if (isResumed()) {
        navigate(route, builder)
    }
}

fun NavController.navigateToTab(route: String) {
    if (currentDestination?.route == route) return
    navigate(route) {
        popUpTo(AppRoutes.HOME) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = route != AppRoutes.HOME
    }
}

fun NavController.safeBack(fallbackRoute: String) {
    if (!isResumed()) return
    if (currentDestination?.route == fallbackRoute) return

    val fallbackExists = runCatching { getBackStackEntry(fallbackRoute) }.isSuccess
    when {
        fallbackExists -> popBackStack(fallbackRoute, inclusive = false)
        previousBackStackEntry == null -> replaceStackWith(fallbackRoute)
        else -> popBackStack()
    }
}

fun NavController.safePop(fallbackRoute: String = AppRoutes.HOME) {
    if (!isResumed()) return
    if (previousBackStackEntry == null) {
        if (currentDestination?.route != fallbackRoute) {
            replaceStackWith(fallbackRoute)
        }
    } else {
        popBackStack()
    }
}

private fun NavController.replaceStackWith(route: String) {
    navigate(route) {
        popUpTo(graph.id) {
            inclusive = true
        }
        launchSingleTop = true
    }
}

suspend fun homeRoute(authRepository: AuthRepository): String {
    return if (authRepository.isGuest()) {
        AppRoutes.GUEST_HOME
    } else {
        AppRoutes.HOME
    }
}

fun NavController.goHome() {
    navigate(AppRoutes.HOME) {
        popUpTo(graph.id) {
            inclusive = true
        }
        launchSingleTop = true
    }
}

fun NavController.goToLogin() {
    navigate(AppRoutes.LOGIN) {
        popUpTo(AppRoutes.LOGIN) {
            inclusive = false
        }
        launchSingleTop = true
    }
}

fun NavController.goHomeThenPendingInvite(store: PendingInviteStore) {
    val code = store.consume()
    goHome()
    if (code != null) {
        navigate(AppRoutes.joinInvite(code))
    }
}

fun NavController.requireAuth(
    context: Context,
    authRepository: AuthRepository,
    scope: CoroutineScope,
    featureName: String,
    onAuthorized: suspend () -> Unit
) {
    scope.launch {
        if (authRepository.isGuest() || !authRepository.isLoggedIn()) {
            Toast.makeText(
                context,
                "$featureName requires a registered account. Please sign in.",
                Toast.LENGTH_SHORT
            ).show()
            navigateOnce(AppRoutes.LOGIN)
        } else {
            onAuthorized()
        }
    }
}

private object CurrentUserProfileCache {
    val profileState = mutableStateOf<ProfileData?>(null)
    var activeToken: String? = null
    var loadedToken: String? = null
    var loadingToken: String? = null
}

@Composable
fun rememberCurrentUserProfile(
    tokenManager: TokenManager,
    isGuest: Boolean,
    refreshKey: Any? = null
): State<ProfileData?> {

    val accessToken by tokenManager.accessTokenFlow.collectAsState(initial = null)
    val lifecycleOwner = LocalLifecycleOwner.current
    var resumeTick by remember { mutableIntStateOf(0) }

    DisposableEffect(lifecycleOwner) {
        var seenFirstResume = false
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (seenFirstResume) resumeTick++ else seenFirstResume = true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(accessToken, isGuest, refreshKey, resumeTick) {

        if (isGuest || accessToken.isNullOrBlank()) {
            return@LaunchedEffect
        }

        val token = accessToken ?: return@LaunchedEffect
        if (CurrentUserProfileCache.loadingToken == token) {
            return@LaunchedEffect
        }
        if (CurrentUserProfileCache.loadedToken == token && resumeTick == 0 && refreshKey == null) {
            return@LaunchedEffect
        }
        if (CurrentUserProfileCache.activeToken != token) {
            CurrentUserProfileCache.activeToken = token
            CurrentUserProfileCache.profileState.value = null
            CurrentUserProfileCache.loadedToken = null
        }

        CurrentUserProfileCache.loadingToken = token

        try {
            val profile = ProfileRepository(
                service = RetrofitInstance.createProfileService(tokenManager)
            ).getMyProfile()
            if (CurrentUserProfileCache.activeToken == token) {
                CurrentUserProfileCache.profileState.value = profile
                CurrentUserProfileCache.loadedToken = token
            }

        } catch (e: Exception) {
        } finally {
            if (CurrentUserProfileCache.loadingToken == token) {
                CurrentUserProfileCache.loadingToken = null
            }
        }
    }

    return CurrentUserProfileCache.profileState
}


suspend fun resolveStartDestination(
    authRepository: AuthRepository,
    pendingOtpStore: PendingOtpStore,
    onboardingStore: OnboardingPreferencesStore
): String {

    if (!onboardingStore.hasSeenOnboarding()) {
        return AppRoutes.ONBOARDING
    }

    val pending = pendingOtpStore.getPendingVerification()
    val isGuest = authRepository.isGuest()

    return when {

        pending != null -> {
            val purpose = runCatching {
                VerifyPurpose.valueOf(pending.purpose)
            }.getOrNull()

            if (purpose != null) {
                AppRoutes.verify(pending.email, purpose)
            } else {
                pendingOtpStore.clear()
                AppRoutes.LOGIN
            }
        }

        authRepository.isLoggedIn() -> {
            if (authRepository.isAdmin()) {
                AppRoutes.ADMIN
            } else {
                AppRoutes.HOME
            }
        }

        isGuest -> AppRoutes.GUEST_HOME

        else -> AppRoutes.LOGIN
    }
}

