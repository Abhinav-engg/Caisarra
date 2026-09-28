package com.abhinav.caisarra.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.offset

@Composable
fun TermsOfServiceDialog(
    onClose: () -> Unit
) {

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        val dialogWidth = minOf(
            screenWidth * 0.92f,
            380.dp
        )
        val dialogHeight = minOf(
            screenHeight * 0.67f,
            600.dp
        )
        Box(modifier = Modifier
                .fillMaxSize()
                .background(
                    Color.Black.copy(
                        alpha = 0.35f
                    )
                )
        )

        Box(modifier = Modifier
                .fillMaxSize(),

            contentAlignment = Alignment.Center
        ) {
            Column(modifier = Modifier
                    .width(dialogWidth)
                    .height(dialogHeight)
                    .offset(y = (-40).dp)
                    .clip(
                        RoundedCornerShape(16.dp)
                    )
                    .background(Color(0xFF10161D))
                    .border(width = 1.dp,
                        brush = Brush.verticalGradient(
                            0f to Color(0xFF008C9E),
                            0.30f to Color(0xFF008C9E)
                                .copy(alpha = 0.35f),
                            0.70f to Color(0xFF33414D)
                                .copy(alpha = 0.20f),
                            1f to Color.Transparent
                        ),

                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp),

                    contentAlignment = Alignment.BottomCenter
                ) {

                    Box(modifier = Modifier
                            .width(38.dp)
                            .height(4.dp)
                            .clip(
                                RoundedCornerShape(50)
                            )
                            .background(
                                Color(0xFF607080)
                            )
                    )
                }

                Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 20.dp,
                            end = 12.dp,
                            top = 8.dp
                        ),

                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(text = "TERMS OF SERVICE",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight =
                            FontWeight.Bold
                    )

                    Box(modifier = Modifier
                            .width(42.dp)
                            .height(42.dp)
                            .clickable {
                                onClose()
                            },

                        contentAlignment = Alignment.Center
                    ) {

                        Text(text = "×",
                            color = Color(0xFF9BA8B7),
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Light
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Box(modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(
                            horizontal = 14.dp
                        )
                        .background(Color(0xFF0B1016))
                ) {
                    Column(modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 14.dp,
                                vertical = 14.dp
                            )
                    ) {
                        Text(text = "1. ACCEPTANCE",

                            color = Color(0xFFD0D8E0),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "By creating an account or accessing the CAISARRA chess " +
                                        "platform, you agree to be bound by these Terms of Service, " +
                                        "all applicable laws, and regulations. If you do not agree " +
                                        "with any of these terms, you are prohibited from using or " +
                                        "accessing this service.",

                            color = Color(0xFFB5C0CC),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "2. ACCOUNT SECURITY & ACCESS",
                            color = Color(0xFFD0D8E0),
                            fontSize = 12.sp,

                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "You are responsible for maintaining the confidentiality " +
                                        "of your account credentials and for all activities that " +
                                        "occur under your account. CAISARRA reserves the right " +
                                        "to suspend or terminate accounts that exhibit suspicious " +
                                        "activity, credential sharing, or unauthorized automated access.",

                            color = Color(0xFFB5C0CC),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "3. FAIR PLAY & ANTI-CHEATING POLICY",

                            color = Color(0xFFD0D8E0),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "CAISARRA maintains a zero-tolerance policy regarding " +
                                        "engine assistance, external calculation tools, third-party " +
                                        "software analysis during active matches, and rating manipulation " +
                                        "(sandbagging or boosting). All games are continuously monitored " +
                                        "by internal algorithmic detection systems. Violations will " +
                                        "result in immediate account termination and rating forfeiture.",

                            color = Color(0xFFB5C0CC),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(text = "4. INTELLECTUAL PROPERTY & DATA PRIVACY",
                            color = Color(0xFFD0D8E0),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "All content, designs, software, graphics, trademarks, " +
                                        "and other materials provided by CAISARRA are protected " +
                                        "by applicable intellectual property and privacy laws. " +
                                        "Users may not copy, distribute, modify, or reproduce " +
                                        "protected content without authorization.",

                            color = Color(0xFFB5C0CC),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(text = "5. USER RESPONSIBILITIES",

                            color = Color(0xFFD0D8E0),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Text(text = "Users agree to provide accurate information, protect " +
                                        "their account credentials, and use the platform only " +
                                        "for lawful purposes and in accordance with these terms.",
                            color = Color(0xFFB5C0CC),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
                Box(modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 24.dp,
                            vertical = 20.dp
                        )
                ) {

                    Box(modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(
                                RoundedCornerShape(12.dp)
                            )
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF08D5EA),
                                        Color(0xFF0BC78F)
                                    )
                                )
                            )
                            .clickable {onClose()},
                        contentAlignment = Alignment.Center
                    ) {

                        Text(text = "Accept & Close",
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}