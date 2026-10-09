package com.abhinav.caisarra.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.abhinav.caisarra.presentation.components.AuthMessage
import com.abhinav.caisarra.presentation.components.GeneralButton
import com.abhinav.caisarra.presentation.components.PasswordTextField
import com.abhinav.caisarra.presentation.components.SimpleTextField
import com.abhinav.caisarra.presentation.components.TermsOfServiceDialog
import com.abhinav.caisarra.presentation.viewmodel.SignUpViewModel
import com.abhinav.caisarra.ui.theme.SignUpCardHeight
import com.caisaara.ui.theme.LinkText
import com.caisaara.ui.theme.SubtleText
import com.abhinav.caisarra.presentation.components.PasswordRequirements

@Composable
fun SignUpScreen(
    viewModel: SignUpViewModel,
    onSignUpSuccess: (String) -> Unit = {},
    onSignInClick: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    var showTerms by remember { mutableStateOf(false) }
    var isPasswordFocused by remember { mutableStateOf(false) }



    Box(modifier = Modifier.fillMaxSize()) {
        AuthCard(cardHeight = SignUpCardHeight) {
            AuthHeader("CREATE ACCOUNT", "")

            SimpleTextField(
                label = "Username",
                value = state.username,
                onValueChange = viewModel::onUsernameChange,
                placeholder = "Enter your username",
                helperText = state.usernameError
            )
            Spacer(modifier = Modifier.height(16.dp))
            SimpleTextField(
                label = "Email",
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                placeholder = "Enter your email",
                helperText = state.emailError
            )
            Spacer(modifier = Modifier.height(16.dp))
            PasswordTextField(
                label = "Password",
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                placeholder = "Enter password",
                helperText = null,
                onFocusChange = { isPasswordFocused = it }
            )

            PasswordRequirements(
                password = state.password,
                isPasswordFocused = isPasswordFocused,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            PasswordTextField(
                label = "Confirm Password",
                value = state.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                placeholder = "Re-enter password",
                helperText = state.confirmPasswordError
            )

            Spacer(modifier = Modifier.height(16.dp))



            AuthMessage(state.error)

            GeneralButton(
                text = if (state.isLoading) "Please wait..." else "Sign Up",
                onClick = { viewModel.signUp(onSignUpSuccess) },
                enabled = state.canSubmit
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
