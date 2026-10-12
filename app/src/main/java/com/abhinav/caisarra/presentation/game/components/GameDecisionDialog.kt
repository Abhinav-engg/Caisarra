package com.abhinav.caisarra.presentation.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.caisaara.ui.theme.ButtonGradientEnd
import com.caisaara.ui.theme.ButtonGradientMid
import com.caisaara.ui.theme.ButtonGradientStart

enum class GameDecision { OfferDraw, Resign, IncomingDraw }

@Composable
fun GameDecisionDialog(decision: GameDecision, opponentName: String = "Your opponent", onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val isResign = decision == GameDecision.Resign
    val isIncoming = decision == GameDecision.IncomingDraw
    val title = when (decision) {
        GameDecision.OfferDraw -> "Offer a Draw?"
        GameDecision.Resign -> "Resign Game?"
        GameDecision.IncomingDraw -> "$opponentName Offers a Draw"
    }
    val confirmLabel = when (decision) {
        GameDecision.OfferDraw -> "Offer Draw"
        GameDecision.Resign -> "Yes, Resign"
        GameDecision.IncomingDraw -> "Accept Draw"
    }
    val dismissLabel = when (decision) {
        GameDecision.OfferDraw -> "Continue Game"
        GameDecision.Resign -> "Keep Playing"
        GameDecision.IncomingDraw -> "Decline & Continue"
    }
    val icon = if (isResign) Icons.Default.Flag else Icons.Default.Handshake

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xFF18212A))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isResign) Color(0xFFFF6B84) else Color(0xFF42D5F5),
                    modifier = Modifier.size(38.dp).align(Alignment.Center)
                )
                if (isIncoming) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Decline draw",
                        tint = Color(0xFF91A1B5),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .clickable(onClick = onDismiss)
                    )
                }
            }
            Text(title, color = Color(0xFFF3F6FA), fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            GameDecisionButton(
                label = confirmLabel,
                icon = if (isIncoming) Icons.Default.Check else icon,
                color = if (isResign) Color(0xFFE9232B) else null,
                onClick = onConfirm
            )
            GameDecisionButton(
                label = dismissLabel,
                icon = if (isIncoming) Icons.Default.Close else null,
                color = null,
                outlined = true,
                textColor = if (isIncoming) Color(0xFFFF626B) else Color(0xFFD5DCE6),
                onClick = onDismiss
            )
        }
    }
}

@Composable
private fun GameDecisionButton(label: String, icon: ImageVector?, color: Color?, outlined: Boolean = false, textColor: Color = Color(0xFF071017), onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(shape)
            .background(
                brush = if (outlined) Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)) else Brush.horizontalGradient(
                    listOf(color ?: ButtonGradientStart, color ?: ButtonGradientMid, color ?: ButtonGradientEnd)
                )
            )
            .then(
                if (outlined) {
                    Modifier.border(
                        1.dp,
                        if (textColor == Color(0xFFFF626B)) Color(0xFFFF626B) else Color(0xFF44515C),
                        shape
                    )
                } else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            icon?.let { Icon(it, null, tint = textColor, modifier = Modifier.size(24.dp)) }
            Text(label, modifier = Modifier.padding(start = if (icon == null) 0.dp else 10.dp), color = textColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
