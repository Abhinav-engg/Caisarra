package com.abhinav.caisarra.presentation.screens.components.profile


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val EditBackground = Color(0xFF080D13)
private val EditCard = Color(0xFF151F2E)

private val EditCardSecondary = Color(0xFF0F1925)

private val EditCyan = Color(0xFF08D5E5)

private val EditTeal = Color(0xFF20B9B0)

private val EditWhite = Color(0xFFF3F6FC)

private val EditMuted = Color(0xFF9BAABD)

private val EditBorder = Color(0xFF29384A)

private val EditError = Color(0xFFFF7777)


@Composable
 fun PhotoOptionRow(
    title: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(Modifier.size(14.dp))
        Text(title, color = EditWhite, fontSize = 16.sp)
    }
}
@Composable
fun AvatarSourceButton(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) EditCyan else EditBorder
    val backgroundColor = if (selected) Color(0xFF10313C) else EditCardSecondary

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.material3.LocalContentColor provides
                        if (selected) EditCyan else EditMuted,
                content = icon
            )
        }
        Text(
            title,
            color = if (selected) EditCyan else EditWhite,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Text(subtitle, color = EditMuted, fontSize = 9.sp)
    }
}
