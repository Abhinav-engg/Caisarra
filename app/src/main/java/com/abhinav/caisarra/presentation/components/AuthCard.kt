package com.abhinav.caisarra.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.ui.theme.AuthCardCornerRadius
import com.abhinav.caisarra.ui.theme.AuthCardHorizontalPadding
import com.abhinav.caisarra.ui.theme.AuthCardMaxWidth
import com.caisaara.ui.theme.CardBackground
import com.caisaara.ui.theme.CardBorderNavy
import com.caisaara.ui.theme.CyanLight
import com.caisaara.ui.theme.LinkText
import com.caisaara.ui.theme.SubtleText
import com.caisaara.ui.theme.White

@Composable
fun AuthCard(
    cardHeight: Dp,
    verticalBias: Float = 0.5f,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(AuthCardCornerRadius)
    val insets = WindowInsets.safeDrawing.asPaddingValues()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF071017))
            .clearFocusOnTap()
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val topInset = insets.calculateTopPadding()
            val availableHeight = maxHeight - topInset - insets.calculateBottomPadding()
            val width = minOf(maxWidth * 0.88f, AuthCardMaxWidth)
            val height = minOf(cardHeight + 40.dp, availableHeight - 24.dp)
            val left = (maxWidth - width) / 2f
            val top = topInset + (availableHeight - height) * verticalBias

            LoginBackground(
                modifier = Modifier.fillMaxSize(),
                cardLeft = left,
                cardTop = top,
                cardRight = left + width,
                cardBottom = top + height,
                cardWidth = width,
                cardHeight = height
            )

            Column(
                modifier = Modifier
                    .offset(x = left, y = top)
                    .width(width)
                    .height(height)
                    .clip(shape)
                    .background(CardBackground.copy(alpha = 0.60f))
                    .border(
                        width = 1.5.dp,
                        brush = Brush.verticalGradient(
                            0.0f to CyanLight.copy(alpha = 0.60f),
                            0.5f to CardBorderNavy.copy(alpha = 0.20f),
                            1.0f to White.copy(alpha = 0.10f)
                        ),
                        shape = shape
                    )
                    .verticalScroll(rememberScrollState())
                    .padding(
                        top = 20.dp,
                        bottom = 20.dp,
                        start = AuthCardHorizontalPadding,
                        end = AuthCardHorizontalPadding
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                content = content
            )
        }
    }
}

@Composable
fun AuthHeader(title: String, subtitle: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium,
        color = White,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = subtitle,
        fontSize = 14.sp,
        color = SubtleText,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
fun AuthFooter(text: String, action: String, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = text, color = SubtleText, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = action,
            color = LinkText,
            fontSize = 14.sp,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.clickable { onClick() }
        )
    }
}

@Composable
fun AuthMessage(text: String?, color: Color = Color(0xFFFF6B6B)) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        if (text != null) {
            Text(
                text = text,
                color = color,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}