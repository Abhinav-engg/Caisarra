package com.abhinav.caisarra.presentation.nearby.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.abhinav.caisarra.core.ui.components.MainTabTopBar

@Composable
fun NearbyTopBar(
    modifier: Modifier = Modifier,
    visible: Boolean = false,
    switchEnabled: Boolean = true,
    onBack: () -> Unit = {},
    onVisibleChange: (Boolean) -> Unit = {}
) {
    MainTabTopBar(
        title = "NEARBY",
        icon = Icons.Filled.LocationOn,
        modifier = modifier
    )
}
