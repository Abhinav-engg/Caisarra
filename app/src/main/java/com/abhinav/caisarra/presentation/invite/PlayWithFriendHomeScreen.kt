package com.abhinav.caisarra.presentation.invite

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.R
import com.abhinav.caisarra.core.ui.components.CaisaaraTopBar
import com.abhinav.caisarra.core.ui.components.GeneralButton
import com.abhinav.caisarra.core.ui.theme.SignUpButtonDefaultCornerRadius
import com.abhinav.caisarra.core.ui.theme.SignUpButtonDefaultMaxHeight
import com.abhinav.caisarra.presentation.game.components.setup.SetupColors
import com.abhinav.caisarra.presentation.game.components.setup.SetupPage

@Composable
fun PlayWithFriendHomeScreen(
    onInviteFriends: () -> Unit,
    onCreateChallengeLink: () -> Unit,
    onJoinByCode: (String) -> Unit = {},
    onBack: () -> Unit
) {
    var enterCode by remember { mutableStateOf("") }
    var showQrScanner by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    if (showQrScanner) {
        QrCodeScannerDialog(
            onDismiss = { showQrScanner = false },
            onQrScanned = { scanned ->
                showQrScanner = false
                val code = InviteCodeParser.extractCode(scanned)
                if (code.isNotBlank()) {
                    onJoinByCode(code)
                }
            }
        )
    }

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
            Icon(
                imageVector = Icons.Default.MeetingRoom,
                contentDescription = null,
                tint = Color(0xFF00CFEA),
                modifier = Modifier.size(28.dp)
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Join with Code or QR",
                color = SetupColors.Text,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Have an invite code, link, or QR code from a friend? Scan or enter it here.",
                color = SetupColors.Secondary,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = enterCode,
                onValueChange = { enterCode = it.trim() },
                placeholder = { Text("Code or link (e.g. 7F89AB12)", color = SetupColors.Secondary) },
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { showQrScanner = true }) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan QR code",
                            tint = Color(0xFF00CFEA)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        val code = InviteCodeParser.extractCode(enterCode)
                        if (code.isNotBlank()) onJoinByCode(code)
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = SetupColors.Text,
                    unfocusedTextColor = SetupColors.Text,
                    focusedBorderColor = Color(0xFF00CFEA),
                    unfocusedBorderColor = Color(0xFF26384D),
                    cursorColor = Color(0xFF00CFEA)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { showQrScanner = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(SignUpButtonDefaultMaxHeight),
                    shape = RoundedCornerShape(SignUpButtonDefaultCornerRadius),
                    border = BorderStroke(1.dp, Color(0xFF00CFEA)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFF00CFEA)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFF00CFEA)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "SCAN QR",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF00CFEA)
                    )
                }

                GeneralButton(
                    text = "JOIN GAME",
                    enabled = enterCode.isNotBlank(),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        focusManager.clearFocus()
                        val code = InviteCodeParser.extractCode(enterCode)
                        if (code.isNotBlank()) onJoinByCode(code)
                    }
                )
            }
        }
    }
}
