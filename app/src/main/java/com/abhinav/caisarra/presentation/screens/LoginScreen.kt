package com.abhinav.caisarra.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.presentation.components.AuthCard
import com.abhinav.caisarra.presentation.components.AuthFooter
import com.abhinav.caisarra.presentation.components.AuthHeader
import com.abhinav.caisarra.presentation.components.GeneralButton
import com.abhinav.caisarra.presentation.components.PasswordTextField
import com.abhinav.caisarra.presentation.components.SimpleTextField
import com.abhinav.caisarra.ui.theme.LoginCardHeight

@Composable
fun LoginScreen(
    onSignInClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    onSignUpClick: () -> Unit = {}
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AuthCard(cardHeight = LoginCardHeight) {
        AuthHeader("WELCOME BACK", "Sign into your Caisarra account")

        SimpleTextField(
            label = "Username/Email",
            value = username,
            onValueChange = { username = it },
            placeholder = "grandmaster_karan"
        )
        Spacer(modifier = Modifier.height(16.dp))
        PasswordTextField(
            label = "Password",
            value = password,
            onValueChange = { password = it },
            placeholder = "Password"
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Forgot password?",
                color = Color(0xFF00D5E9),
                fontSize = 14.sp,
                modifier = Modifier.clickable { onForgotPasswordClick() }
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        GeneralButton(
            text = "Sign In",
            onClick = onSignInClick,
            enabled = username.isNotBlank() && password.isNotBlank()
        )
        Spacer(modifier = Modifier.height(24.dp))

        AuthFooter("Don't have an account?", "Sign Up", onSignUpClick)
    }
}