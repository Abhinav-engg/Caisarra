package com.abhinav.caisarra.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono

@Composable
fun CaisaaraTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    navigationIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
    titleColor: Color = Color.White,
    iconTint: Color = Color.White,
    backgroundColor: Color = Color.Transparent,
    titleFontSize: TextUnit = 20.sp,
    height: Dp = 52.dp,
    actions: @Composable (RowScope.() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(backgroundColor)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Bold,
            fontSize = titleFontSize,
            letterSpacing = 2.sp,
            color = titleColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(horizontal = 48.dp)
                .align(Alignment.Center)
        )

        if (onBack != null) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = navigationIcon,
                    contentDescription = "Back",
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (actions != null) {
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                actions()
            }
        }
    }
}
