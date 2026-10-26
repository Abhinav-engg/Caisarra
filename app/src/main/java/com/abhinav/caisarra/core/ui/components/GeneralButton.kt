package com.abhinav.caisarra.core.ui.components

import android.graphics.RadialGradient as AndroidRadialGradient
import android.graphics.Shader as AndroidShader
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.theme.SignUpButtonDefaultCornerRadius
import com.abhinav.caisarra.core.ui.theme.SignUpButtonDefaultMaxHeight
import com.abhinav.caisarra.presentation.common.singleClick
import com.caisaara.ui.theme.ButtonGradientEnd
import com.caisaara.ui.theme.ButtonGradientMid
import com.caisaara.ui.theme.ButtonGradientStart

@Composable
fun GeneralButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(SignUpButtonDefaultCornerRadius)
    val buttonBrush = remember {
        object : ShaderBrush() {
            override fun createShader(size: Size): Shader {
                val radius = maxOf(size.width, size.height).coerceAtLeast(1f) * 0.75f
                return AndroidRadialGradient(
                    size.width / 2f,
                    size.height / 2f,
                    radius,
                    intArrayOf(
                        ButtonGradientStart.toArgb(),
                        ButtonGradientMid.toArgb(),
                        ButtonGradientEnd.toArgb()
                    ),
                    floatArrayOf(0f, 0.45f, 1f),
                    AndroidShader.TileMode.CLAMP
                )
            }
        }
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(SignUpButtonDefaultMaxHeight)
            .alpha(if (enabled) 1f else 0.5f)
            .clip(shape)
            .background(buttonBrush)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                enabled = enabled,
                onClick = singleClick(onClick)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (leadingIcon != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                leadingIcon()
                Text(
                    text = text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }
        } else {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
        }
    }
}
