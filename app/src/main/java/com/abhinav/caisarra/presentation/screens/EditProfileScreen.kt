package com.abhinav.caisarra.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.abhinav.caisarra.presentation.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput

private val EditBackground = Color(0xFF080D13)
private val EditCard = Color(0xFF151F2E)
private val EditCardSecondary = Color(0xFF0F1925)
private val EditCyan = Color(0xFF08D5E5)
private val EditTeal = Color(0xFF20B9B0)
private val EditWhite = Color(0xFFF3F6FC)
private val EditMuted = Color(0xFF9BAABD)
private val EditBorder = Color(0xFF29384A)
private val EditError = Color(0xFFFF7777)

private enum class AvatarSource(val label: String) {
    GALLERY("Gallery"),
    CAMERA("Camera"),
    URL("Avatar URL")
}

@Composable
fun EditProfileScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var selectedSource by remember {
        mutableStateOf(AvatarSource.URL)
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedSource = AvatarSource.GALLERY
            viewModel.uploadAvatar(context, uri)
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val file = File(context.cacheDir, "profile_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            selectedSource = AvatarSource.CAMERA
            viewModel.uploadAvatar(context, Uri.fromFile(file))
        }
    }

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
        if (state.message == "Profile updated successfully") onSaved()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EditBackground)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            focusManager.clearFocus()
                        }
                    )
                }
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(EditCard)
                        .border(1.dp, EditBorder, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = EditWhite
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Edit Profile",
                        color = EditWhite,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Update your player information",
                        color = EditMuted,
                        fontSize = 13.sp
                    )
                }
            }

            if (state.loading && !initialized) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 70.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = EditCyan)
                }
            } else if (state.profile != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF172536), EditCard)
                            )
                        )
                        .border(1.dp, EditBorder, RoundedCornerShape(22.dp))
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(112.dp)
                            .clip(CircleShape)
                            .background(EditCardSecondary)
                            .border(
                                width = 3.dp,
                                brush = Brush.linearGradient(listOf(EditCyan, EditTeal)),
                                shape = CircleShape
                            )
                            .padding(4.dp)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (avatarUrl.startsWith("https://", ignoreCase = true)) {
                            AsyncImage(
                                model = avatarUrl.trim(),
                                contentDescription = "Profile picture preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Default avatar",
                                tint = EditCyan,
                                modifier = Modifier.size(58.dp)
                            )
                        }
                    }

                    Text(
                        text = when (selectedSource) {
                            AvatarSource.URL -> "SOURCE: AVATAR URL"
                            AvatarSource.GALLERY -> "SOURCE: GALLERY PHOTO"
                            AvatarSource.CAMERA -> "SOURCE: CAMERA PHOTO"
                        },
                        color = EditCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AvatarSourceButton(
                            modifier = Modifier.weight(1f),
                            title = "Gallery",
                            subtitle = "Choose photo",
                            icon = { Icon(Icons.Default.Image, null) },
                            selected = selectedSource == AvatarSource.GALLERY,
                            onClick = { galleryLauncher.launch("image/*") }
                        )
                        AvatarSourceButton(
                            modifier = Modifier.weight(1f),
                            title = "Camera",
                            subtitle = "Take photo",
                            icon = { Icon(Icons.Default.CameraAlt, null) },
                            selected = selectedSource == AvatarSource.CAMERA,
                            onClick = { cameraLauncher.launch(null) }
                        )
                        AvatarSourceButton(
                            modifier = Modifier.weight(1f),
                            title = "Avatar URL",
                            subtitle = "HTTPS link",
                            icon = { Icon(Icons.Default.Link, null) },
                            selected = selectedSource == AvatarSource.URL,
                            onClick = { selectedSource = AvatarSource.URL }
                        )
                    }
                    Text(
                        text = "Choose a photo from your gallery, take a new photo, or keep using an HTTPS Avatar URL. Photo uploads require a registered account.",
                        color = EditMuted,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(EditCard)
                        .border(1.dp, EditBorder, RoundedCornerShape(22.dp))
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "PLAYER INFORMATION",
                            color = EditCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "@${state.profile?.username.orEmpty()}",
                            color = EditMuted,
                            fontSize = 11.sp
                        )
                    }

                    ReadOnlyProfileRow(
                        label = "Username",
                        value = state.profile?.username.orEmpty()
                    )
                    ReadOnlyProfileRow(
                        label = "Email",
                        value = state.profile?.email.orEmpty()
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
                            country = it.filter(Char::isLetter).take(2).uppercase()
                            validationError = null
                        },
                        label = "Country code (e.g. IN)",
                        singleLine = true,
                        capitalization = KeyboardCapitalization.Characters
                    )

                    EditProfileField(
                        value = bio,
                        onValueChange = {
                            bio = it.take(160)
                            validationError = null
                        },
                        label = "Bio",
                        supportingText = "${bio.length}/160",
                        minLines = 3
                    )

                    EditProfileField(
                        value = avatarUrl,
                        onValueChange = { newValue ->
                            avatarUrl = newValue
                            selectedSource = AvatarSource.URL
                            validationError = null
                        },
                        label = "Avatar HTTPS URL",
                        singleLine = true,
                        keyboardType = KeyboardType.Uri,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = "Avatar URL",
                                tint = EditMuted
                            )
                        }
                    )

                    Text(
                        text = "Enter a new HTTPS URL to replace your current profile photo. " +
                                "You can also continue using Gallery or Camera.",
                        color = EditMuted,
                        fontSize = 12.sp
                    )

                    validationError?.let {
                        Text(it, color = EditError, fontSize = 12.sp)
                    }
                    state.error?.let {
                        Text(it, color = EditError, fontSize = 12.sp)
                    }
                    Text(
                        text = "DEFAULT CHESS AVATARS",
                        color = EditCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    state.defaultAvatars.chunked(3).forEach { avatarRow ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            avatarRow.forEach { avatar ->
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(EditCardSecondary)
                                        .clickable(enabled = !state.saving) {
                                            viewModel.selectDefaultAvatar(avatar.id)
                                        }
                                        .padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    AsyncImage(
                                        model = avatar.url,
                                        contentDescription = "${avatar.id} avatar",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                    )

                                    Spacer(Modifier.height(6.dp))

                                    Text(
                                        text = avatar.id,
                                        color = EditWhite,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            repeat(3 - avatarRow.size) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        validationError = when {
                            displayName.length > 30 ->
                                "Display name must be at most 30 characters."
                            displayName.any { it == '\n' || it == '\r' || it == '\t' } ->
                                "Display name must be a single line."
                            bio.length > 160 ->
                                "Bio must be at most 160 characters."
                            country.isNotBlank() && !country.matches(Regex("[A-Z]{2}")) ->
                                "Enter a valid two-letter country code."
                            avatarUrl.isNotBlank() &&
                                    !avatarUrl.trim().startsWith("https://", ignoreCase = true) ->
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
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EditCyan,
                        contentColor = EditBackground,
                        disabledContainerColor = EditCyan.copy(alpha = 0.45f)
                    )
                ) {
                    if (state.saving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(19.dp),
                            color = EditBackground,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.size(10.dp))
                        Text("SAVING...", fontWeight = FontWeight.Bold)
                    } else {
                        Text(
                            "SAVE CHANGES",
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.6.sp
                        )
                    }
                }

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = EditMuted
                    )
                ) {
                    Text("CANCEL", fontWeight = FontWeight.Bold)
                }
            } else {
                Text(
                    state.error ?: "Unable to load your profile.",
                    color = EditError,
                    fontSize = 14.sp
                )
                OutlinedButton(onClick = { viewModel.loadProfile() }) {
                    Text("RETRY", color = EditCyan)
                }
            }

            Spacer(Modifier.height(8.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding()
        )
    }
}

@Composable
private fun AvatarSourceButton(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) EditCyan else EditBorder
    val backgroundColor = if (selected) Color(0xFF10313C) else EditCardSecondary

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.material3.LocalContentColor provides
                        if (selected) EditCyan else EditMuted,
                content = icon
            )
        }
        Text(
            title,
            color = if (selected) EditCyan else EditWhite,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Text(subtitle, color = EditMuted, fontSize = 9.sp)
    }
}

@Composable
private fun ReadOnlyProfileRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(EditCardSecondary)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(label, color = EditMuted, fontSize = 12.sp)
        Text(
            value.ifBlank { "Not available" },
            color = EditWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
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
    keyboardType: KeyboardType = KeyboardType.Text,
    minLines: Int = 1,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = singleLine,
        minLines = minLines,
        supportingText = supportingText?.let { text ->
            { Text(text, color = EditMuted) }
        },
        leadingIcon = leadingIcon,
        keyboardOptions = KeyboardOptions(
            capitalization = capitalization,
            keyboardType = keyboardType,
            imeAction = if (singleLine) ImeAction.Next else ImeAction.Default
        ),
        shape = RoundedCornerShape(15.dp),
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
