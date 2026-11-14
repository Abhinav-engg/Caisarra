package com.abhinav.caisarra.core.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.presentation.common.singleClickable

@Composable
fun TopBarIconAction(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit
) {
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = tint,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .singleClickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .size(20.dp)
    )
}
