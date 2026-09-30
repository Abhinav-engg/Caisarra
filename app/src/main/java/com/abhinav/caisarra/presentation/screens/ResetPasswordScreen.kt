package com.abhinav.caisarra.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.presentation.components.AuthCard
import com.abhinav.caisarra.presentation.components.AuthFooter
import com.abhinav.caisarra.presentation.components.AuthHeader
import com.abhinav.caisarra.presentation.components.GeneralButton
import com.abhinav.caisarra.presentation.components.SimpleTextField
import com.abhinav.caisarra.presentation.viewmodel.ResetPasswordViewModel
import com.abhinav.caisarra.ui.theme.ResetCardHeight
import com.caisaara.ui.theme.EmeraldNormal

@Composable
fun ResetPasswordScreen(
    viewModel: ResetPasswordViewModel,
    onSendCode: (String) -> Unit = {},
    onBackToSignIn: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    AuthCard(cardHeight = ResetCardHeight) {
        AuthHeader(
            "RESET PASSWORD",
            "Enter your registered email and we'll send a recovery code."
        )

        SimpleTextField(
            label = "Email",
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            placeholder = "grandmaster@caisarra.com",
            helperText = state.emailError
        )
        Spacer(modifier = Modifier.height(32.dp))

        GeneralButton(
            text = if (state.isLoading) "Please wait..." else "Send Code",
            onClick = { viewModel.sendCode(onSendCode) },
            enabled = state.canSubmit
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp),
            contentAlignment = Alignment.Center
        ) {
            state.error?.let {
                Text(text = it, color = Color(0xFFFF6B6B), fontSize = 12.sp)
            }
        }

        AuthFooter("Back to", "Sign In", onBackToSignIn)
    }
}