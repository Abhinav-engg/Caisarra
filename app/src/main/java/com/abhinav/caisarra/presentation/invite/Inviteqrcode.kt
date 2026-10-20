package com.abhinav.caisarra.presentation.invite

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono

private val CardBg = Color(0xFF151F2B)
private val BorderColor = Color(0xFF304154)
private val White = Color(0xFFF5F5F5)
private val Secondary = Color(0xFF8C98A5)

@Composable
fun InviteQrCode(
    link: String,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp
) {
    val bitmap = remember(link) { QrCodeGenerator.generate(link) }

    Box(
        modifier = modifier
            .size(size)
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Invite QR code",
                modifier = Modifier.fillMaxSize(),
                filterQuality = FilterQuality.None
            )
        } else {
            Text(
                text = "Couldn't generate QR code",
                color = Color.Black,
                fontFamily = JetBrainsMono,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
fun InviteQrDialog(
    link: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .background(CardBg, RoundedCornerShape(16.dp))
                .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "SCAN TO JOIN",
                color = White,
                fontFamily = JetBrainsMono,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            InviteQrCode(link = link, size = 260.dp)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Point a phone camera at this code to open the match invite.",
                color = Secondary,
                fontFamily = JetBrainsMono,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "CLOSE",
                    color = White,
                    fontFamily = JetBrainsMono,
                    fontSize = 12.sp
                )
            }
        }
    }
}