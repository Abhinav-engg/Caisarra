package com.abhinav.caisarra.presentation.invite

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.R
import com.abhinav.caisarra.core.ui.components.CaisaaraTopBar
import com.abhinav.caisarra.core.ui.components.GeneralButton
import com.abhinav.caisarra.presentation.game.components.setup.SetupColors
import com.abhinav.caisarra.presentation.game.components.setup.SetupPage

@Composable
fun PlayWithFriendHomeScreen(
    onInviteFriends: () -> Unit,
    onCreateChallengeLink: () -> Unit,
    onBack: () -> Unit
) {
    SetupPage {
        CaisaaraTopBar(
            title = "PLAY WITH FRIENDS",
            onBack = onBack
        )

        Spacer(Modifier.height(28.dp))

        Text(
            text = "Better across the board.",
            color = SetupColors.Text,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Challenge a friend, wherever they are.",
            color = SetupColors.Secondary,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    SetupColors.Card,
                    RoundedCornerShape(20.dp)
                )
                .border(
                    1.dp,
                    Brush.linearGradient(
                        listOf(Color(0xFF00CFEA).copy(alpha = 0.35f), Color(0xFF172431))
                    ),
                    RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_invite_friends),
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Invite Friends",
                color = SetupColors.Text,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Find a Mosaic username and add them to your circle.",
                color = SetupColors.Secondary,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(20.dp))

            GeneralButton(
                text = "Find a friend",
                onClick = onInviteFriends
            )
        }

        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    SetupColors.Card,
                    RoundedCornerShape(20.dp)
                )
                .border(
                    1.dp,
                    Brush.linearGradient(
                        listOf(Color(0xFF00CFEA).copy(alpha = 0.35f), Color(0xFF172431))
                    ),
                    RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_match_link),
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Create a match link",
                color = SetupColors.Text,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Set your rules. Share one private invitation.",
                color = SetupColors.Secondary,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(20.dp))

            GeneralButton(
                text = "Create Invite",
                onClick = onCreateChallengeLink
            )
        }
    }
}
