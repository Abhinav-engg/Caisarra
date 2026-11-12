package com.abhinav.caisarra.features.notificationsettings.data

import com.google.firebase.messaging.FirebaseMessaging
import kotlin.coroutines.suspendCoroutine

suspend fun currentFcmToken(): String = suspendCoroutine { continuation ->
    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
        continuation.resumeWith(runCatching { task.result })
    }
}
