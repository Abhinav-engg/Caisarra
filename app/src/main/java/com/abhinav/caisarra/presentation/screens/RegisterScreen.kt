package com.abhinav.caisarra.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.presentation.components.AuthCard
import com.abhinav.caisarra.presentation.components.AuthFooter
import com.abhinav.caisarra.presentation.components.AuthHeader
import com.abhinav.caisarra.presentation.components.GeneralButton
import com.abhinav.caisarra.presentation.components.PasswordTextField
import com.abhinav.caisarra.presentation.components.SimpleTextField
import com.abhinav.caisarra.presentation.components.TermsOfServiceDialog
import com.abhinav.caisarra.ui.theme.SignUpCardHeight

@Composable
fun SignUpScreen(
    onSignUpClick: () -> Unit = {},
    onSignInClick: () -> Unit = {},
    onTermsClick: () -> Unit = {}
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showTerms by remember { mutableStateOf(false) }

    val termsText = buildAnnotatedString {
        append("By creating an account, you agree to ")
        withStyle(SpanStyle(color = Color(0xFF00D5E9))) {
            append("Terms & Privacy Policy.")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AuthCard(cardHeight = SignUpCardHeight) {
            AuthHeader("CREATE ACCOUNT", "Join to master.")

            SimpleTextField(
                label = "Username",
                value = username,
                onValueChange = { username = it },
                placeholder = "e.g. Karan_gamer"
            )
            Spacer(modifier = Modifier.height(16.dp))
            SimpleTextField(
                label = "Email",
                value = email,
                onValueChange = { email = it },
                placeholder = "yourname@gmail.com"
            )
            Spacer(modifier = Modifier.height(16.dp))
            PasswordTextField(
                label = "Password",
                value = password,
                onValueChange = { password = it },
                placeholder = "Enter password"
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Password must be at least 8 characters long.",
                color = Color(0xFF82909E),
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = termsText,
                color = Color(0xFF82909E),
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        showTerms = true
                        onTermsClick()
                    }
            )
            Spacer(modifier = Modifier.height(20.dp))

            GeneralButton(
                text = "Sign Up",
                onClick = onSignUpClick,
                enabled = username.isNotBlank() &&
                        email.isNotBlank() &&
                        password.length >= 8
            )
            Spacer(modifier = Modifier.height(24.dp))

            AuthFooter("Already have an account?", "Sign In", onSignInClick)
        }

        if (showTerms) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) { detectTapGestures { } }
            ) {
                TermsOfServiceDialog(onClose = { showTerms = false })
            }
        }
    }
}