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
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.ui.theme.AuthCardCornerRadius
import com.abhinav.caisarra.ui.theme.AuthCardMaxWidth

private val SubtleText = Color(0xFF82909E)
private val LinkText = Color(0xFF00D5E9)

@Composable
fun AuthCard(
    cardHeight: Dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(AuthCardCornerRadius)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF071017))
            .clearFocusOnTap()
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            val width = minOf(maxWidth * 0.88f, AuthCardMaxWidth)
            val height = minOf(cardHeight, maxHeight - 24.dp)
            val left = (maxWidth - width) / 2f
            val top = (maxHeight - height) / 2f

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
                    .align(Alignment.Center)
                    .width(width)
                    .height(height)
                    .clip(shape)
                    .background(Color(0xFF101A21))
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            0.0f to Color(0xFF008C9E),
                            0.20f to Color(0xFF008C9E).copy(alpha = 0.35f),
                            0.45f to Color(0xFF33414D).copy(alpha = 0.25f),
                            1.0f to Color.Transparent
                        ),
                        shape = shape
                    )
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp),
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
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
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
            fontWeight = FontWeight.Bold,
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

