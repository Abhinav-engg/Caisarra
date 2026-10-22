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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono

private val BackgroundColor = Color(0xFF080D13)
private val CardBg = Color(0xFF0F1722)
private val CardBorderColor = Color(0xFF1B2838)
private val TextWhiteColor = Color(0xFFF1F5F9)
private val TextMutedColor = Color(0xFF82909E)
private val RedAlertColor = Color(0xFFEF4444)

@Composable
fun DeleteAccountScreen(
    onBack: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF101924))
                    .border(
                        width = 1.dp,
                        color = Color(0xFF202E3E),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextWhiteColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = "Delete Account",
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = RedAlertColor
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2E171A))
                    .border(1.dp, Color(0xFF4E2024), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = RedAlertColor,
                    modifier = Modifier.size(36.dp)
                )
            }

            Text(
                text = "Permanently Delete Account?",
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextWhiteColor,
                textAlign = TextAlign.Center
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardBg)
                    .border(1.dp, CardBorderColor, RoundedCornerShape(16.dp))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "This action is permanent and cannot be reversed:",
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = TextWhiteColor
                    )

                    BulletPoint(text = "Your profile, username, and bio will be erased.")
                    BulletPoint(text = "All game records, match histories, and analysis will be removed.")
                    BulletPoint(text = "Your ratings (Rapid, Blitz, Bullet, Puzzles) will be reset.")
                    BulletPoint(text = "Friend lists, club memberships, and messages will be lost.")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onConfirmDelete,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RedAlertColor
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "DELETE MY ACCOUNT",
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }

            OutlinedButton(
                onClick = onBack,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "CANCEL & KEEP ACCOUNT",
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = TextWhiteColor
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
            color = RedAlertColor,
            fontSize = 14.sp
        )
        Text(
            text = text,
            fontFamily = JetBrainsMono,
            color = TextMutedColor,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
    }
}
