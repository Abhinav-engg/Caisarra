package com.abhinav.caisarra.presentation.screens

import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.presentation.components.AuthCard
import com.abhinav.caisarra.presentation.components.AuthFooter
import com.abhinav.caisarra.presentation.components.GeneralButton
import com.abhinav.caisarra.presentation.components.SimpleTextField

@Composable
fun GuestScreen(
    onContinueClick: (String) -> Unit = {},
    onSignUpClick: () -> Unit = {}
) {

    var username by remember {
        mutableStateOf("")
    }

    AuthCard(
        cardHeight = 350.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "CONTINUE AS GUEST",
                color = Color.White,
                fontSize = 26.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(text = "Explore as Guest with temporary\nguest access.",
                color = Color(0xFF8D9AAA),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        SimpleTextField(
            label = "Username",
            value = username,
            onValueChange = {
                username = it
            },
            placeholder = "grandmaster_karan"
        )

        Spacer(modifier = Modifier.height(24.dp))

        GeneralButton(text = "Continue",
            onClick = {onContinueClick(username)
            },
            enabled = username.isNotBlank()
        )

        Spacer(modifier = Modifier.height(24.dp))
        AuthFooter(
            "Don't have an account?",
            "Sign Up",
            onSignUpClick
        )
    }
}

