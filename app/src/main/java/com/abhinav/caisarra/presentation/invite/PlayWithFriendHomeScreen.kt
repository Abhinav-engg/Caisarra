
package com.abhinav.caisarra.presentation.invite

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.components.GeneralButton
import com.abhinav.caisarra.presentation.game.components.setup.SetupColors
import com.abhinav.caisarra.presentation.game.components.setup.SetupHeader
import com.abhinav.caisarra.presentation.game.components.setup.SetupPage

@Composable
fun PlayWithFriendHomeScreen(
    onInviteFriends: () -> Unit,
    onCreateChallengeLink: () -> Unit,
    onBack: () -> Unit
) {
    SetupPage {
        SetupHeader(
            title = "Play a Friend",
            onBack = onBack
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "PLAY TOGETHER",
            color = SetupColors.Secondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Choose how you want to play with your friends.",
            color = SetupColors.Secondary,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(24.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    SetupColors.Card,
                    RoundedCornerShape(18.dp)
                )
                .border(
                    1.dp,
                    Color(0xFF26384D),
                    RoundedCornerShape(18.dp)
                )
                .padding(20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.People,
                contentDescription = null,
                tint = SetupColors.Accent
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Invite Friends",
                color = SetupColors.Text,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(16.dp))

            GeneralButton(
                text = "INVITE FRIENDS",
                onClick = onInviteFriends
            )
        }

        Spacer(Modifier.height(16.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    SetupColors.Card,
                    RoundedCornerShape(18.dp)
                )
                .border(
                    1.dp,
                    Color(0xFF26384D),
                    RoundedCornerShape(18.dp)
                )
                .padding(20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Link,
                contentDescription = null,
                tint = SetupColors.Accent
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Create Challenge Link",
                color = SetupColors.Text,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))

            GeneralButton(
                text = "CREATE MATCH LINK",
                onClick = onCreateChallengeLink
            )
        }
    }
}
