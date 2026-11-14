package com.abhinav.caisarra.features.profilelink.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.abhinav.caisarra.features.profilelink.data.AvatarUrlCache

@Composable
fun rememberAvatarUrl(username: String?, avatarUrl: String?): String? {
    val context = LocalContext.current.applicationContext
    val target = username?.trim()?.takeIf { it.isNotEmpty() && avatarUrl.isNullOrBlank() }
    var fetched by remember(target) { mutableStateOf(target?.let(AvatarUrlCache::peek)) }
    LaunchedEffect(target) {
        if (target != null && fetched == null) fetched = AvatarUrlCache.load(context, target)
    }
    return avatarUrl?.takeIf { it.isNotBlank() } ?: fetched
}
