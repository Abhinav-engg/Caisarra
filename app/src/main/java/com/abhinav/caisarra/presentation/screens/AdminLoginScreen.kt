
package com.abhinav.caisarra.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abhinav.caisarra.presentation.viewmodel.AdminLoginViewModel

private val AdminLoginBackground = Color(0xFF080D13)
private val AdminLoginSurface = Color(0xFF101923)
private val AdminLoginCyan = Color(0xFF00D5E8)
private val AdminLoginText = Color.White
private val AdminLoginSecondary = Color(0xFF8D9AAA)

@Composable
fun AdminLoginScreen(
    viewModel: AdminLoginViewModel,
    onLoginSuccess: () -> Unit,
    onBackToUserLogin: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminLoginBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    AdminLoginSurface,
                    RoundedCornerShape(24.dp)
                )
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            Text(
                text = "MOSAIC",
                color = AdminLoginCyan,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "ADMINISTRATION",
                color = AdminLoginSecondary,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Admin Sign In",
                color = AdminLoginText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Sign in to manage and monitor the MOSAIC platform.",
                color = AdminLoginSecondary,
                style = MaterialTheme.typography.bodyMedium
            )

            OutlinedTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Admin Email") },
                placeholder = { Text("Enter admin email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                shape = RoundedCornerShape(14.dp),
                colors = adminLoginFieldColors()
            )

            OutlinedTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Password") },
                placeholder = { Text("Enter password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                shape = RoundedCornerShape(14.dp),
                colors = adminLoginFieldColors()
            )

            state.error?.let {
                Text(
                    text = it,
                    color = Color(0xFFFF5C65),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = {
                    viewModel.login(onLoginSuccess)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = state.canSubmit,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AdminLoginCyan,
                    contentColor = AdminLoginBackground
                )
            ) {
                if (state.loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = AdminLoginBackground,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Sign In as Admin",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            TextButton(
                onClick = onBackToUserLogin,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    "Back to User Login",
                    color = AdminLoginSecondary
                )
            }

            HorizontalDivider(
                color = Color(0xFF293440)
            )

            Text(
                text = "Restricted access • Authorized administrators only",
                color = AdminLoginSecondary,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun adminLoginFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = AdminLoginText,
        unfocusedTextColor = AdminLoginText,
        cursorColor = AdminLoginCyan,
        focusedBorderColor = AdminLoginCyan,
        unfocusedBorderColor = Color(0xFF293440),
        focusedLabelColor = AdminLoginCyan,
        unfocusedLabelColor = AdminLoginSecondary,
        focusedContainerColor = AdminLoginBackground,
        unfocusedContainerColor = AdminLoginBackground
    )
