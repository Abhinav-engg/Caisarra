package com.abhinav.caisarra.presentation.invite

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.abhinav.caisarra.core.ui.theme.JetBrainsMono
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

private val ScannerCyan = Color(0xFF00CFEA)
private val ScannerDarkBg = Color(0xFF080D13)
private val ScannerCardBg = Color(0xFF151F2B)
private val ScannerBorder = Color(0xFF26384D)
private val ScannerMuted = Color(0xFF9CA3AF)

@Composable
fun QrCodeScannerDialog(
    onDismiss: () -> Unit,
    onQrScanned: (String) -> Unit
) {
    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    val barcodeScanner = remember {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        BarcodeScanning.getClient(options)
    }

    var isProcessingGallery by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isProcessingGallery = true
            try {
                val inputImage = InputImage.fromFilePath(context, uri)
                barcodeScanner.process(inputImage)
                    .addOnSuccessListener { barcodes ->
                        val code = barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }?.rawValue
                        if (!code.isNullOrBlank()) {
                            onQrScanned(code)
                        } else {
                            Toast.makeText(context, "No QR code found in selected image", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to decode QR code", Toast.LENGTH_SHORT).show()
                    }
                    .addOnCompleteListener {
                        isProcessingGallery = false
                    }
            } catch (e: Exception) {
                isProcessingGallery = false
                Toast.makeText(context, "Could not open image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            if (hasCameraPermission) {
                CameraScannerView(
                    onQrScanned = onQrScanned,
                    onOpenGallery = { galleryLauncher.launch("image/*") },
                    onDismiss = onDismiss
                )
            } else {
                PermissionRequestView(
                    onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    onOpenGallery = { galleryLauncher.launch("image/*") },
                    onDismiss = onDismiss
                )
            }
        }
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
private fun CameraScannerView(
    onQrScanned: (String) -> Unit,
    onOpenGallery: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var camera by remember { mutableStateOf<Camera?>(null) }
    var isTorchOn by remember { mutableStateOf(false) }
    var hasScanned by remember { mutableStateOf(false) }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val barcodeScanner = remember {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        BarcodeScanning.getClient(options)
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        if (hasScanned) {
                            imageProxy.close()
                            return@setAnalyzer
                        }

                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            val image = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees
                            )
                            barcodeScanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    if (!hasScanned) {
                                        val detected = barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }?.rawValue
                                        if (!detected.isNullOrBlank()) {
                                            hasScanned = true
                                            onQrScanned(detected)
                                        }
                                    }
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        } else {
                            imageProxy.close()
                        }
                    }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                    } catch (_: Exception) {}
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            }
        )

        val boxSize = 260.dp
        ScannerOverlay(boxSize = boxSize)

        // Top Controls Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close scanner",
                    tint = Color.White
                )
            }

            Text(
                text = "SCAN QR TO JOIN",
                color = Color.White,
                fontFamily = JetBrainsMono,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            IconButton(
                onClick = {
                    val nextState = !isTorchOn
                    camera?.cameraControl?.enableTorch(nextState)
                    isTorchOn = nextState
                },
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = "Toggle flashlight",
                    tint = if (isTorchOn) ScannerCyan else Color.White
                )
            }
        }

        // Bottom Controls / Guidance
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Point camera at a game invite QR code",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            OutlinedButton(
                onClick = onOpenGallery,
                modifier = Modifier
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, ScannerCyan.copy(alpha = 0.6f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = ScannerDarkBg.copy(alpha = 0.75f),
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    tint = ScannerCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "CHOOSE FROM PHOTOS",
                    fontFamily = JetBrainsMono,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun ScannerOverlay(boxSize: Dp) {
    val infiniteTransition = rememberInfiniteTransition(label = "scannerLaser")
    val laserPosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserFloat"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val boxPx = boxSize.toPx()
        val left = (width - boxPx) / 2f
        val top = (height - boxPx) / 2f
        val right = left + boxPx
        val bottom = top + boxPx

        val scrimColor = Color.Black.copy(alpha = 0.6f)

        // Draw Scrim (outside the scan box)
        drawRect(color = scrimColor, topLeft = Offset(0f, 0f), size = Size(width, top))
        drawRect(color = scrimColor, topLeft = Offset(0f, bottom), size = Size(width, height - bottom))
        drawRect(color = scrimColor, topLeft = Offset(0f, top), size = Size(left, boxPx))
        drawRect(color = scrimColor, topLeft = Offset(right, top), size = Size(width - right, boxPx))

        // Corner Brackets
        val cornerLength = 28.dp.toPx()
        val cornerStroke = 4.dp.toPx()

        // Top-Left
        drawLine(
            color = ScannerCyan,
            start = Offset(left, top + cornerLength),
            end = Offset(left, top),
            strokeWidth = cornerStroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = ScannerCyan,
            start = Offset(left, top),
            end = Offset(left + cornerLength, top),
            strokeWidth = cornerStroke,
            cap = StrokeCap.Round
        )

        // Top-Right
        drawLine(
            color = ScannerCyan,
            start = Offset(right - cornerLength, top),
            end = Offset(right, top),
            strokeWidth = cornerStroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = ScannerCyan,
            start = Offset(right, top),
            end = Offset(right, top + cornerLength),
            strokeWidth = cornerStroke,
            cap = StrokeCap.Round
        )

        // Bottom-Left
        drawLine(
            color = ScannerCyan,
            start = Offset(left, bottom - cornerLength),
            end = Offset(left, bottom),
            strokeWidth = cornerStroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = ScannerCyan,
            start = Offset(left, bottom),
            end = Offset(left + cornerLength, bottom),
            strokeWidth = cornerStroke,
            cap = StrokeCap.Round
        )

        // Bottom-Right
        drawLine(
            color = ScannerCyan,
            start = Offset(right - cornerLength, bottom),
            end = Offset(right, bottom),
            strokeWidth = cornerStroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = ScannerCyan,
            start = Offset(right, bottom),
            end = Offset(right, bottom - cornerLength),
            strokeWidth = cornerStroke,
            cap = StrokeCap.Round
        )

        // Subtle box border
        drawRect(
            color = ScannerCyan.copy(alpha = 0.2f),
            topLeft = Offset(left, top),
            size = Size(boxPx, boxPx),
            style = Stroke(width = 1.dp.toPx())
        )

        // Animated Laser Line
        val laserY = top + (boxPx * laserPosition)
        drawLine(
            brush = Brush.horizontalGradient(
                listOf(
                    ScannerCyan.copy(alpha = 0f),
                    ScannerCyan,
                    ScannerCyan.copy(alpha = 0f)
                ),
                startX = left,
                endX = right
            ),
            start = Offset(left + 8.dp.toPx(), laserY),
            end = Offset(right - 8.dp.toPx(), laserY),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun PermissionRequestView(
    onRequestPermission: () -> Unit,
    onOpenGallery: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScannerDarkBg)
            .statusBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(44.dp)
                .background(ScannerCardBg, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ScannerCardBg, RoundedCornerShape(20.dp))
                .border(1.dp, ScannerBorder, RoundedCornerShape(20.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(ScannerCyan.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = ScannerCyan,
                    modifier = Modifier.size(36.dp)
                )
            }

            Text(
                text = "CAMERA PERMISSION",
                color = Color.White,
                fontFamily = JetBrainsMono,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Text(
                text = "Camera access is needed to scan game invite QR codes directly from another phone.",
                color = ScannerMuted,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onRequestPermission,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ScannerCyan)
            ) {
                Text(
                    text = "ALLOW CAMERA",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontFamily = JetBrainsMono,
                    fontSize = 13.sp
                )
            }

            OutlinedButton(
                onClick = onOpenGallery,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, ScannerBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    tint = ScannerCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "CHOOSE FROM PHOTOS",
                    fontFamily = JetBrainsMono,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
