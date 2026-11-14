package com.abhinav.caisarra.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.caisaara.ui.theme.RedNormal
import kotlinx.coroutines.delay

@Composable
fun SwipeToDelete(
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    autoDeleteAfterMillis: Long? = null,
    content: @Composable RowScope.() -> Unit
) {
    val state = rememberSwipeToDismissBoxState()
    LaunchedEffect(autoDeleteAfterMillis) {
        autoDeleteAfterMillis?.let {
            delay(it)
            state.dismiss(SwipeToDismissBoxValue.EndToStart)
        }
    }
    SwipeToDismissBox(
        state = state,
        modifier = modifier,
        onDismiss = { onDelete() },
        backgroundContent = {
            Box(
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)).background(RedNormal),
                contentAlignment = if (state.dismissDirection == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = Color.White, modifier = Modifier.padding(horizontal = 20.dp))
            }
        },
        content = content
    )
}
