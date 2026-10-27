package com.abhinav.caisarra.presentation.game.components

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.abhinav.caisarra.R
import com.abhinav.caisarra.core.ui.components.GeneralButton
import com.abhinav.caisarra.core.ui.theme.ResignButtonHeight
import com.abhinav.caisarra.core.ui.theme.ResignButtonRadius
import com.abhinav.caisarra.core.ui.theme.ResignDialogPadding
import com.abhinav.caisarra.core.ui.theme.ResignDialogRadius
import com.abhinav.caisarra.core.ui.theme.ResignDialogSpacing
import com.abhinav.caisarra.core.ui.theme.ResignDialogWidth
import com.abhinav.caisarra.core.ui.theme.ResignIconSize
import com.abhinav.caisarra.presentation.common.singleClick
import com.caisaara.ui.theme.CardBorderNavy
import com.caisaara.ui.theme.CyanLight
import com.caisaara.ui.theme.ResignDialogBg
import com.caisaara.ui.theme.ResignRed
import com.caisaara.ui.theme.ResignSecondaryBg
import com.caisaara.ui.theme.ResignTextWhite
import com.caisaara.ui.theme.SubtleText

enum class GameDecision { OfferDraw, Resign, IncomingDraw }

@Composable
fun GameDecisionDialog(
    decision: GameDecision,
    opponentName: String = "Your opponent",
    gameMode: String = "Blitz",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val isResign = decision == GameDecision.Resign
    val title = when (decision) {
        GameDecision.Resign -> "Resign this game?"
        GameDecision.OfferDraw -> "Offer a Draw?"
        GameDecision.IncomingDraw -> "$opponentName Offers a Draw"
    }
    val description = when (decision) {
        GameDecision.Resign -> "$opponentName will win this rated $gameMode game. This action cannot be undone."
        GameDecision.OfferDraw -> "Would you like to offer a draw to $opponentName?"
        GameDecision.IncomingDraw -> "$opponentName has offered a draw. Do you want to accept?"
    }
    val primaryLabel = when (decision) {
        GameDecision.Resign -> "Keep playing"
        GameDecision.OfferDraw -> "Offer Draw"
        GameDecision.IncomingDraw -> "Accept Draw"
    }
    val secondaryLabel = when (decision) {
        GameDecision.Resign -> "Yes, resign"
        GameDecision.OfferDraw -> "Continue Game"
        GameDecision.IncomingDraw -> "Decline & Continue"
    }
    val primaryAction = if (isResign) onDismiss else onConfirm
    val secondaryAction = if (isResign) onConfirm else onDismiss

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val shape = RoundedCornerShape(ResignDialogRadius)
        Column(
            modifier = Modifier
                .width(ResignDialogWidth)
                .shadow(16.dp, shape, spotColor = CardBorderNavy)
                .clip(shape)
                .background(ResignSecondaryBg)
                .background(ResignDialogBg)
                .border(1.dp, CyanLight, shape)
                .padding(ResignDialogPadding),
            verticalArrangement = Arrangement.spacedBy(ResignDialogSpacing)
        ) {
            if (isResign) {
                Image(
                    painter = painterResource(R.drawable.ic_resign_flag),
                    contentDescription = null,
                    modifier = Modifier.size(ResignIconSize)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Handshake,
                    contentDescription = null,
                    tint = CyanLight,
                    modifier = Modifier.size(ResignIconSize)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = title,
                    color = ResignTextWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 24.sp
                )
                Text(
                    text = description,
                    color = ResignTextWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 16.sp
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                GeneralButton(
                    text = primaryLabel,
                    onClick = primaryAction
                )
                val btnShape = RoundedCornerShape(ResignButtonRadius)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ResignButtonHeight)
                        .clip(btnShape)
                        .background(ResignSecondaryBg)
                        .border(1.dp, CardBorderNavy, btnShape)
                        .clickable(onClick = singleClick(secondaryAction)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = secondaryLabel,
                        color = if (isResign) ResignRed else SubtleText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
