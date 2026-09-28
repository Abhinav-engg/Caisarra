package com.abhinav.caisarra.presentation.screens

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.presentation.components.GeneralButton
import com.abhinav.caisarra.presentation.components.LoginBackground
import com.abhinav.caisarra.presentation.components.PasswordTextField
import com.abhinav.caisarra.presentation.components.SimpleTextField
import com.abhinav.caisarra.presentation.components.TermsOfServiceDialog


@Composable
fun SignUpScreen(
    onSignUpClick: () -> Unit = {},
    onSignInClick: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {}
) {
    var username by remember {mutableStateOf("") }
    var email by remember {mutableStateOf("") }

    var password by remember {mutableStateOf("") }

    var showTerms by remember {mutableStateOf(false) }
    Box(modifier = Modifier
            .fillMaxSize()
            .background(
                Color(0xFF071017)
            )
    ) {
        BoxWithConstraints(modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing
                )
        ) {
            val horizontalMargin = 20.dp
            val cardWidth = minOf(
                320.dp,
                maxWidth - horizontalMargin * 2
            )
            val desiredCardHeight = 580.dp
            val availableCardHeight = maxHeight - 24.dp
            val cardHeight = minOf(
                desiredCardHeight,
                availableCardHeight
            )
            val cardLeft = (maxWidth - cardWidth) / 2f

            val cardTop = ((maxHeight - cardHeight) / 2f)
                    .coerceAtLeast(12.dp)

            val cardRight = cardLeft + cardWidth
            val cardBottom = cardTop + cardHeight
            LoginBackground(
                modifier = Modifier.fillMaxSize(),
                cardLeft = cardLeft,
                cardTop = cardTop,
                cardRight = cardRight,
                cardBottom = cardBottom,

                cardWidth = cardWidth,
                cardHeight = cardHeight
            )

            Column(modifier = Modifier
                    .width(cardWidth)
                    .height(cardHeight)
                    .offset(
                        x = cardLeft,
                        y = cardTop
                    )
                    .clip(
                        RoundedCornerShape(20.dp)
                    )

                    .background(Color(0xFF101A21))
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            0.0f to Color(0xFF008C9E),
                            0.20f to Color(0xFF008C9E)
                                .copy(alpha = 0.35f),
                            0.45f to Color(0xFF33414D)
                                .copy(alpha = 0.25f),
                            1.0f to Color.Transparent
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )

                    .padding(horizontal = 16.dp,
                        vertical = 20.dp
                    )
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(text = "CREATE ACCOUNT",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(text = "Join to master.",
                        fontSize = 14.sp,
                        color = Color(0xFF82909E),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))

                Column(modifier = Modifier
                        .fillMaxWidth()
                        .width(287.dp)
                ) {
                    SimpleTextField(label = "Username",
                        value = username,
                        onValueChange = {
                            username = it
                        },
                        placeholder = "e.g. Karan_gamer"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SimpleTextField(
                        label = "Email",
                        value = email,
                        onValueChange = {
                            email = it
                        },
                        placeholder = "yourname@gmail.com"
                    )


                    Spacer(modifier = Modifier.height(12.dp))
                    PasswordTextField(
                        label = "Password",
                        value = password,
                        onValueChange = {
                            password = it
                        },
                        placeholder = "Enter password"
                    )


                    Spacer(modifier = Modifier.height(5.dp))

                    Text(text = "Password must be atleast 8 characters long.",
                        color = Color(0xFF82909E),
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {

                        Row(modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {

                            Text(text = "By creating an account, you agree to ",

                                color = Color(0xFF82909E),
                                fontSize = 12.sp
                            )

                            Text(text = "Terms & Privacy",
                                color = Color(0xFF00D5E9),
                                fontSize = 12.sp,
                                modifier = Modifier.clickable {
                                        showTerms = true
                                        onTermsClick()
                                    }
                            )
                        }

                        Text(text = "Policy.",
                            color = Color(0xFF00D5E9),
                            fontSize = 12.sp,
                            modifier = Modifier.clickable {
                                    showTerms = true

                                    onPrivacyClick()
                                }
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))

                    GeneralButton(text = "Sign Up",
                        onClick = {
                            onSignUpClick()
                        },

                        enabled = username.isNotBlank() &&
                                    email.isNotBlank() &&
                                    password.length >= 8
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier
                        .fillMaxWidth(),

                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {

                    Text(text =
                            "Already have an account?",

                        color = Color(0xFF82909E),
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.width(5.dp))

                    Text(text = "Sign In",

                        color = Color(0xFF00D5E9),

                        fontSize = 14.sp,

                        fontWeight = FontWeight.Bold,

                        modifier = Modifier.clickable {
                                showTerms = false
                                onSignInClick()
                            }
                    )
                }
            }
        }

        if (showTerms) {
            TermsOfServiceDialog(
                onClose = {
                    showTerms = false
                }
            )
        }
    }
}