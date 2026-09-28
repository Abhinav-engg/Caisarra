package com.abhinav.caisarra.presentation.screens

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
import com.abhinav.caisarra.presentation.components.AuthHeader
import com.abhinav.caisarra.presentation.components.GeneralButton
import com.abhinav.caisarra.presentation.components.PasswordTextField

@Composable
fun SetNewPasswordScreen(
    onResetAndSignIn: () -> Unit = {}
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val isPasswordValid = newPassword.length >= 8
    val passwordsMatch = newPassword == confirmPassword && confirmPassword.isNotEmpty()

    val isFormValid = isPasswordValid && passwordsMatch

    AuthCard(cardHeight = 390.dp) {
        AuthHeader("NEW PASSWORD",
            "Set a strong password for your account."
        )

        Spacer(modifier = Modifier.height(20.dp))
        PasswordTextField(label = "Set New Password",
            value = newPassword,
            onValueChange = {
                newPassword = it
            },
            placeholder = "Enter password"
        )
        Spacer(modifier = Modifier.height(4.dp))


        Text(text = "Password must be at least 8 characters long.",

            color = Color(0xFF82909E),
            fontSize = 11.sp,
            modifier = Modifier.fillMaxWidth()
        )


        Spacer(modifier = Modifier.height(8.dp))

        PasswordTextField(label = "Confirm Password",
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
            },
            placeholder = "Enter password"
        )


        Spacer(modifier = Modifier.height(24.dp))
        GeneralButton(text = "Reset & Sign In",
            onClick = {
                onResetAndSignIn()
            },
            enabled = isFormValid
        )
    }
}