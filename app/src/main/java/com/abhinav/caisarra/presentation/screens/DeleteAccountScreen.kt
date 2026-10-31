package com.abhinav.caisarra.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.components.PasswordTextField
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.abhinav.caisarra.core.ui.theme.StatsCardCornerRadius
import com.abhinav.caisarra.presentation.viewmodel.DeleteAccountViewModel
import com.caisaara.ui.theme.DeleteWarningBg
import com.caisaara.ui.theme.DeleteWarningBorder
import com.caisaara.ui.theme.RedNormal
import com.caisaara.ui.theme.StatsBg
import com.caisaara.ui.theme.StatsCardBg
import com.caisaara.ui.theme.StatsCardBorder
import com.caisaara.ui.theme.StatsTextMuted
import com.caisaara.ui.theme.StatsTextWhite
import com.caisaara.ui.theme.TextSizeBadge
import com.caisaara.ui.theme.TextSizeBody
import com.caisaara.ui.theme.TextSizeCaption
import com.caisaara.ui.theme.TextSizeHeading
import com.caisaara.ui.theme.TextSizeTitle
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import com.abhinav.caisarra.core.ui.components.CaisaaraTopBar

@Composable
fun DeleteAccountScreen(
    viewModel: DeleteAccountViewModel,
    onBack: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StatsBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        CaisaaraTopBar(
            title = "Delete Account",
            onBack = {
                if (!state.isLoading) {
                    onBack()
                }
            },
            backgroundColor = StatsBg,
            titleColor = RedNormal,
            iconTint = StatsTextWhite,
            titleFontSize = TextSizeHeading
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(DeleteWarningBg)
                    .border(1.dp, DeleteWarningBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = RedNormal,
                    modifier = Modifier.size(36.dp)
                )
            }

            Text(
                text = "Permanently Delete Account?",
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Bold,
                fontSize = TextSizeTitle,
                color = StatsTextWhite,
                textAlign = TextAlign.Center
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(StatsCardCornerRadius))
                    .background(StatsCardBg)
                    .border(1.dp, StatsCardBorder, RoundedCornerShape(StatsCardCornerRadius))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "This action is permanent and cannot be reversed:",
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = TextSizeBadge,
                        color = StatsTextWhite
                    )

                    BulletPoint(text = "Your profile, username, and bio will be erased.")
                    BulletPoint(text = "All game records, match histories, and analysis will be removed.")
                    BulletPoint(text = "Your ratings (Rapid, Blitz, Bullet, Puzzles) will be reset.")
                    BulletPoint(text = "Friend lists, club memberships, and messages will be lost.")
                    BulletPoint(text = "Active tournaments will be cancelled or withdrawn.")
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "CONFIRM WITH PASSWORD",
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = TextSizeCaption,
                    color = StatsTextMuted
                )

                PasswordTextField(
                    label = "",
                    value = state.password,
                    onValueChange = viewModel::onPasswordChange,
                    placeholder = "Enter your current password",
                    helperText = if (state.password.length > 72) {
                        "Password cannot exceed 72 characters."
                    } else {
                        null
                    }
                )
            }

            if (state.error != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DeleteWarningBg)
                        .border(1.dp, RedNormal.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = RedNormal,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = state.error ?: "",
                            fontFamily = JetBrainsMono,
                            fontSize = TextSizeCaption,
                            color = StatsTextWhite,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    viewModel.confirmDeleteAccount(onSuccess = onConfirmDelete)
                },
                enabled = state.canSubmit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RedNormal,
                    disabledContainerColor = RedNormal.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "DELETING ACCOUNT...",
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                        fontSize = TextSizeBody,
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "DELETE MY ACCOUNT",
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                        fontSize = TextSizeBody,
                        color = Color.White
                    )
                }
            }

            OutlinedButton(
                onClick = onBack,
                enabled = !state.isLoading,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "CANCEL & KEEP ACCOUNT",
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Medium,
                    fontSize = TextSizeBody,
                    color = StatsTextWhite
                )
            }
        }
    }
}

@Composable
private fun BulletPoint(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            fontFamily = JetBrainsMono,
            color = RedNormal,
            fontSize = TextSizeBody
        )
        Text(
            text = text,
            fontFamily = JetBrainsMono,
            color = StatsTextMuted,
            fontSize = TextSizeCaption,
            lineHeight = TextSizeTitle
        )
    }
}
