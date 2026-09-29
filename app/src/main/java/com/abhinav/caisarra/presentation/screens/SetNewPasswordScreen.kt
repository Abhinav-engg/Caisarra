package com.abhinav.caisarra.presentation.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.presentation.components.AuthCard
import com.abhinav.caisarra.presentation.components.AuthHeader
import com.abhinav.caisarra.presentation.components.AuthMessage
import com.abhinav.caisarra.presentation.components.GeneralButton
import com.abhinav.caisarra.presentation.components.PasswordTextField
import com.abhinav.caisarra.presentation.viewmodel.SetNewPasswordViewModel
import com.abhinav.caisarra.ui.theme.SetNewPasswordCardHeight
import com.caisaara.ui.theme.EmeraldNormal
import com.caisaara.ui.theme.SubtleText

@Composable
fun SetNewPasswordScreen(
    viewModel: SetNewPasswordViewModel,
    onPasswordUpdated: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    AuthCard(cardHeight = SetNewPasswordCardHeight) {
        AuthHeader("NEW PASSWORD", "Set a strong password for your account.")

        PasswordTextField(
            label = "Set New Password",
            value = state.newPassword,
            onValueChange = viewModel::onNewPasswordChange,
            placeholder = "Enter password",
            helperText = state.newPasswordError
        )
        if (state.newPasswordError == null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Password must be at least 8 characters long.",
                color = SubtleText,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        PasswordTextField(
            label = "Confirm Password",
            value = state.confirmPassword,
            onValueChange = viewModel::onConfirmPasswordChange,
            placeholder = "Enter password",
            helperText = state.confirmPasswordError
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (state.updated) {
            AuthMessage("Password updated successfully!", EmeraldNormal)
        } else {
            AuthMessage(state.error)
        }

        GeneralButton(
            text = if (state.isLoading) "Please wait..." else "Reset Password",
            onClick = { viewModel.resetPassword(onPasswordUpdated) },
            enabled = state.canSubmit
        )
    }
}