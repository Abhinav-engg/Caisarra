package com.abhinav.caisarra.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.presentation.components.AuthCard
import com.abhinav.caisarra.presentation.components.AuthFooter
import com.abhinav.caisarra.presentation.components.AuthHeader
import com.abhinav.caisarra.presentation.components.GeneralButton
import com.abhinav.caisarra.presentation.components.SimpleTextField
import com.abhinav.caisarra.ui.theme.ResetCardHeight
import com.caisaara.ui.theme.EmeraldNormal

@Composable
fun ResetPasswordScreen(
    onSendCode: () -> Unit = {},
    onBackToSignIn: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var codeSent by remember { mutableStateOf(false) }

    AuthCard(cardHeight = ResetCardHeight) {
        AuthHeader(
            "RESET PASSWORD",
            "Enter your registered email and we'll send a recovery code."
        )

        SimpleTextField(
            label = "Email",
            value = email,
            onValueChange = { email = it },
            placeholder = "grandmaster@caisarra.com"
        )
        Spacer(modifier = Modifier.height(32.dp))

        GeneralButton(
            text = "Send Code",
            onClick = {
                codeSent = true
                onSendCode()
            },
            enabled = email.isNotBlank()
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp),
            contentAlignment = Alignment.Center
        ) {
            if (codeSent) {
                Text(text = "Code Sent!", color = EmeraldNormal, fontSize = 12.sp)
            }
        }

        AuthFooter("Back to", "Sign In", onBackToSignIn)
    }
}

