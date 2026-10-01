package com.abhinav.caisarra.presentation.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.presentation.components.AuthCard
import com.abhinav.caisarra.presentation.components.GeneralButton
import com.abhinav.caisarra.ui.theme.GuestOptionsCardHeight

@Composable
fun GuestOptionsScreen(
    onLoginClick: () -> Unit = {},
    onSignUpClick: () -> Unit = {}
) {
    AuthCard(cardHeight = GuestOptionsCardHeight) {
        GeneralButton(
            text = "Log In",
            onClick = onLoginClick
        )
        Spacer(modifier = Modifier.height(16.dp))
        GeneralButton(
            text = "Sign Up",
            onClick = onSignUpClick
        )
    }
}