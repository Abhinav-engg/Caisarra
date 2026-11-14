package com.abhinav.caisarra.features.profilelink.presentation

import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import com.abhinav.caisarra.presentation.common.singleClickable

fun Modifier.openProfileOnClick(username: String?): Modifier = composed {
    val openProfile = LocalOpenProfile.current
    val target = username?.trim()?.takeIf { it.isNotEmpty() }
    if (target == null) this else singleClickable { openProfile(target) }
}
