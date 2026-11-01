package com.abhinav.caisarra.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.abhinav.caisarra.data.repository.AuthRepository
import com.abhinav.caisarra.presentation.navigation.AppNavigation
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    companion object {
        val pendingIntentFlow = MutableStateFlow<Intent?>(null)
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            NotificationHelper.ensureChannels(applicationContext)
            val authRepository = AuthRepository.get(applicationContext)
            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                lifecycleScope.launch { saveFcmToken(authRepository, token) }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
            splashScreenViewProvider.remove()
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        pendingIntentFlow.value = intent
        requestNotificationPermission()

        val authRepository = AuthRepository.get(applicationContext)
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                lifecycleScope.launch { saveFcmToken(authRepository, token) }
            }
            .addOnFailureListener { error ->
                android.util.Log.e("FCM_DEBUG", "Failed to retrieve FCM token from Google Play Services", error)
            }

        setContent {
            val repo = remember { authRepository }
            AppNavigation(repo)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingIntentFlow.value = intent
    }

    private suspend fun saveFcmToken(authRepository: AuthRepository, token: String) {
        try {
            authRepository.updateFcmToken(token)
        } catch (e: Exception) {
            android.util.Log.e("FCM_DEBUG", "Failed to save FCM token", e)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
