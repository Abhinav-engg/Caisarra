
package com.abhinav.caisarra.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abhinav.caisarra.presentation.viewmodel.ProfileViewModel
import androidx.compose.foundation.layout.statusBarsPadding

private val EditBackground = Color(0xFF080D13)
private val EditCard = Color(0xFF161F2D)
private val EditCyan = Color(0xFF00D9E9)
private val EditWhite = Color(0xFFF4F7FB)
private val EditMuted = Color(0xFF9CAABD)
private val EditBorder = Color(0xFF273446)
private val EditError = Color(0xFFFF7777)

@Composable
fun EditProfileScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current

    var displayName by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    LaunchedEffect(state.profile) {
        state.profile?.let { profile ->
            displayName = profile.displayName.orEmpty()
            country = profile.country.orEmpty()
            bio = profile.bio.orEmpty()
            avatarUrl = profile.avatarUrl.orEmpty()
            initialized = true
        }
    }

    LaunchedEffect(state.message) {
        if (state.message == "Profile updated successfully") {
            onSaved()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EditBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .pointerInput(Unit) {
                detectTapGestures {
                    focusManager.clearFocus()
                }
            }
            .padding(
                start = 22.dp,
                end = 22.dp,
                top = 8.dp,
                bottom = 16.dp
            ),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(EditCard)
                    .border(
                        1.dp,
                        EditBorder,
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = EditWhite
                    )
                }
            }

            Column {
                Text(
                    text = "Edit Profile",
                    color = EditWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = "Update your player information",
                    color = EditMuted,
                    fontSize = 12.sp
                )
            }
        }

        if (state.loading && !initialized) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = EditCyan)
            }
        } else if (state.profile != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(EditCard)
                    .border(
                        1.dp,
                        EditBorder,
                        RoundedCornerShape(24.dp)
                    )
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "PLAYER INFORMATION",
                    color = EditCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "Username: ${state.profile?.username.orEmpty()}",
                    color = EditMuted,
                    fontSize = 14.sp
                )

                Text(
                    text = "Email: ${state.profile?.email.orEmpty()}",
                    color = EditMuted,
                    fontSize = 14.sp
                )

                EditProfileField(
                    value = displayName,
                    onValueChange = {
                        displayName = it
                        validationError = null
                    },
                    label = "Display name",
                    singleLine = true,
                    capitalization = KeyboardCapitalization.Words
                )

                EditProfileField(
                    value = country,
                    onValueChange = {
                        country = it
                            .filter { char -> char.isLetter() }
                            .take(2)
                            .uppercase()
                        validationError = null
                    },
                    label = "Country code (e.g. IN)",
                    singleLine = true,
                    capitalization = KeyboardCapitalization.Characters
                )

                EditProfileField(
                    value = bio,
                    onValueChange = {
                        bio = it
                        validationError = null
                    },
                    label = "Bio",
                    supportingText = "${bio.length}/160"
                )

                EditProfileField(
                    value = avatarUrl,
                    onValueChange = {
                        avatarUrl = it
                        validationError = null
                    },
                    label = "Avatar HTTPS URL",
                    singleLine = true,
                    keyboardType = KeyboardType.Uri
                )

                validationError?.let { message ->
                    Text(
                        text = message,
                        color = EditError,
                        fontSize = 13.sp
                    )
                }

                state.error?.let { message ->
                    Text(
                        text = message,
                        color = EditError,
                        fontSize = 13.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(EditCyan)
                ) {
                    androidx.compose.material3.Button(
                        onClick = {
                            focusManager.clearFocus()

                            validationError = when {
                                displayName.length > 30 ->
                                    "Display name must be at most 30 characters."

                                displayName.any {
                                    it == '\n' || it == '\r' || it == '\t'
                                } ->
                                    "Display name must be a single line."

                                bio.length > 160 ->
                                    "Bio must be at most 160 characters."

                                country.isNotBlank() &&
                                        !country.matches(Regex("[A-Z]{2}")) ->
                                    "Enter a valid two-letter country code."

                                avatarUrl.isNotBlank() &&
                                        !avatarUrl.trim().startsWith("https://") ->
                                    "Avatar URL must start with https://."

                                avatarUrl.length > 500 ->
                                    "Avatar URL must be at most 500 characters."

                                else -> null
                            }

                            if (validationError == null) {
                                viewModel.updateProfile(
                                    displayName = displayName,
                                    country = country,
                                    bio = bio,
                                    avatarUrl = avatarUrl
                                )
                            }
                        },
                        enabled = !state.saving,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = androidx.compose.material3.ButtonDefaults
                            .buttonColors(
                                containerColor = EditCyan,
                                contentColor = EditBackground
                            )
                    ) {
                        if (state.saving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = EditBackground,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.size(10.dp))
                            Text("SAVING...")
                        } else {
                            Text(
                                text = "SAVE CHANGES",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 5.dp)
                            )
                        }
                    }
                }

                androidx.compose.material3.OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "CANCEL",
                        color = EditMuted,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            Text(
                text = state.error ?: "Unable to load your profile.",
                color = EditError,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun EditProfileField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    singleLine: Boolean = false,
    supportingText: String? = null,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = singleLine,
        supportingText = supportingText?.let { text ->
            { Text(text, color = EditMuted) }
        },
        keyboardOptions = KeyboardOptions(
            capitalization = capitalization,
            keyboardType = keyboardType,
            imeAction = if (singleLine) ImeAction.Next else ImeAction.Default
        ),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = EditWhite,
            unfocusedTextColor = EditWhite,
            focusedBorderColor = EditCyan,
            unfocusedBorderColor = EditBorder,
            focusedLabelColor = EditCyan,
            unfocusedLabelColor = EditMuted,
            cursorColor = EditCyan,
            focusedContainerColor = EditBackground,
            unfocusedContainerColor = EditBackground
        )
    )
}
