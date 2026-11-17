package com.abhinav.caisarra.features.notificationsettings.presentation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.edit
import com.abhinav.caisarra.app.NotificationHelper
import com.abhinav.caisarra.core.permissions.hasPermission
import com.abhinav.caisarra.data.local.NOTIFICATION_PROMPT_SHOWN_KEY
import com.abhinav.caisarra.data.local.settingsDataStore
import com.abhinav.caisarra.data.repository.AuthRepository
import com.abhinav.caisarra.features.notificationsettings.data.currentFcmToken
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val PROMPT_DELAY_MS = 1_200L

@Composable
fun NotificationPermissionPrompt() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val context = LocalContext.current
    val appContext = context.applicationContext
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            NotificationHelper.ensureChannels(appContext)
            scope.launch(NonCancellable) {
                runCatching { AuthRepository.get(appContext).updateFcmToken(currentFcmToken()) }
            }
        }
    }
    LaunchedEffect(Unit) {
        if (context.hasPermission(Manifest.permission.POST_NOTIFICATIONS)) return@LaunchedEffect
        val store = appContext.settingsDataStore
        if (store.data.first()[NOTIFICATION_PROMPT_SHOWN_KEY] == true) return@LaunchedEffect
        delay(PROMPT_DELAY_MS)
        store.edit { it[NOTIFICATION_PROMPT_SHOWN_KEY] = true }
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
