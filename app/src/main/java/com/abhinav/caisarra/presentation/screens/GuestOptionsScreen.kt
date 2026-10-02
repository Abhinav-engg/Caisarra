package com.abhinav.caisarra.presentation.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abhinav.caisarra.presentation.components.AuthCard
import com.abhinav.caisarra.presentation.components.GeneralButton
import com.abhinav.caisarra.presentation.components.GuestIdHeader
import com.abhinav.caisarra.ui.theme.GuestOptionsCardHeight

@Composable
fun GuestOptionsScreen(
    guestId: String,
    onLoginClick: () -> Unit = {},
    onSignUpClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    AuthCard(cardHeight = GuestOptionsCardHeight) {
        GuestIdHeader(guestId)
        Spacer(modifier = Modifier.height(16.dp))
        GeneralButton(
            text = "Log In",
            onClick = onLoginClick
        )
        Spacer(modifier = Modifier.height(16.dp))
        GeneralButton(
            text = "Sign Up",
            onClick = onSignUpClick
        )
        Spacer(modifier = Modifier.height(16.dp))
        GeneralButton(
            text = "Log Out",
            onClick = onLogoutClick
        )
    }
}