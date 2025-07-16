package com.example.clarimind.presentation.screens

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.collection.emptyObjectList
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.clarimind.presentation.viewmodels.EmotionDetectionViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import java.nio.ByteBuffer


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun EmotionCameraScreen(
    onEmotionDetected: (String) -> Unit = {},
    onBackPressed: () -> Unit = {},
    viewModel : EmotionDetectionViewModel = viewModel(),
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    var showInstructions by remember { mutableStateOf(true) }
    var isProcessing by remember { mutableStateOf(false) }
    var detectedEmotion by remember { mutableStateOf<String?>(null) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_FRONT) }

    val previewView = remember { PreviewView(context) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    LaunchedEffect(cameraPermissionState.status.isGranted) {
        if (cameraPermissionState.status.isGranted) {
            showInstructions = false
            setupCamera(context, lifecycleOwner, previewView, lensFacing) { capture ->
                imageCapture = capture
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when {
            !cameraPermissionState.status.isGranted -> {
                CameraPermissionContent(
                    onRequestPermission = { cameraPermissionState.launchPermissionRequest() },
                    shouldShowRationale = cameraPermissionState.status.shouldShowRationale
                )
            }

            showInstructions -> {
                InstructionsOverlay(
                    onStartCamera = { showInstructions = false }
                )
            }

            else -> {
                // Camera Preview
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize()
                )

                // Face Detection Overlay
                FaceDetectionOverlay(
                    modifier = Modifier.fillMaxSize()
                )

                // Top Bar
                TopBar(
                    onBackPressed = onBackPressed,
                    onFlipCamera = {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                            CameraSelector.LENS_FACING_BACK
                        } else {
                            CameraSelector.LENS_FACING_FRONT
                        }
                        // Restart camera with new lens facing
                        setupCamera(context, lifecycleOwner, previewView, lensFacing) { capture ->
                            imageCapture = capture
                        }
                    }
                )

                // Bottom Controls
                BottomControls(
                    isProcessing = isProcessing,
                    detectedEmotion = detectedEmotion,
                    onCapturePhoto = {
                        capturePhoto(
                            imageCapture = imageCapture,
                            context = context,
                            lensFacing = lensFacing,
                            onImageCaptured = { bitmap ->
                                isProcessing = true
                                // Process the real captured bitmap
                                viewModel.processEmotionDetection(bitmap,context) { emotion ->
                                    isProcessing = false
                                    detectedEmotion = emotion
                                }
                            }
                        )
                    },
                    navigateToNext = {
                        onEmotionDetected(it)
                    },
                    onRetry = {
                        detectedEmotion = null
                        isProcessing = false
                    }
                )
            }
        }
    }
}

@Composable
fun CameraPermissionContent(
    onRequestPermission: () -> Unit,
    shouldShowRationale: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Camera Permission Required",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (shouldShowRationale) {
                "ClariMind needs camera access to detect your emotions through facial expressions. This helps provide personalized mental health insights."
            } else {
                "To detect your emotions, we need access to your camera. Your privacy is protected - all processing happens locally on your device."
            },
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRequestPermission,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6C63FF)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Grant Camera Permission",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun InstructionsOverlay(
    onStartCamera: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = Color(0xFF6C63FF),
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Emotion Detection",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "📸 Position your face in the camera frame\n" +
                    "😊 Show your natural expression\n" +
                    "✨ Our AI will detect your current emotion\n" +
                    "🔒 All processing happens on your device",
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onStartCamera,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6C63FF)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Start Camera",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun TopBar(
    onBackPressed: () -> Unit,
    onFlipCamera: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackPressed,
            modifier = Modifier
                .size(48.dp)
                .background(
                    Color.Black.copy(alpha = 0.5f),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        Text(
            text = "ClariMind",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        IconButton(
            onClick = onFlipCamera,
            modifier = Modifier
                .size(48.dp)
                .background(
                    Color.Black.copy(alpha = 0.5f),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.FlipCameraAndroid,
                contentDescription = "Flip Camera",
                tint = Color.White
            )
        }
    }
}

@Composable
fun FaceDetectionOverlay(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = minOf(size.width, size.height) * 0.4f

        // Draw face detection circle
        drawCircle(
            color = Color(0xFF6C63FF),
            radius = radius,
            center = androidx.compose.ui.geometry.Offset(centerX, centerY),
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 4.dp.toPx(),
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                    floatArrayOf(20f, 20f)
                )
            )
        )
    }
}

@Composable
fun BottomControls(
    isProcessing: Boolean,
    detectedEmotion: String?,
    onCapturePhoto: () -> Unit,
    navigateToNext : (String) -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        when {
            isProcessing -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.8f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF6C63FF),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Analyzing your emotion...",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            detectedEmotion != null -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.8f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Detected Emotion",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = detectedEmotion,
                            color = Color(0xFF6C63FF),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = onRetry,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Gray.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Retry")
                            }
                            Button(
                                onClick = { navigateToNext(detectedEmotion) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF6C63FF)
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Continue")
                            }
                        }
                    }
                }
            }

            else -> {
                Text(
                    text = "Position your face in the circle and tap to capture",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }

        // Capture Button
        if (detectedEmotion == null) {
            FloatingActionButton(
                onClick = onCapturePhoto,
                modifier = Modifier
                    .size(72.dp)
                    .border(4.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                containerColor = Color(0xFF6C63FF),
                contentColor = Color.White
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Capture",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

// Helper functions for camera setup and photo capture
private fun setupCamera(
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    previewView: PreviewView,
    lensFacing: Int,
    onImageCaptureReady: (ImageCapture) -> Unit
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()

        val preview = androidx.camera.core.Preview.Builder().build()
        preview.setSurfaceProvider(previewView.surfaceProvider)

        val imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )

            onImageCaptureReady(imageCapture)
        } catch (e: Exception) {
            Log.e("CameraSetup", "Camera binding failed", e)
        }
    }, ContextCompat.getMainExecutor(context))
}

private fun capturePhoto(
    imageCapture: ImageCapture?,
    context: Context,
    lensFacing: Int,
    onImageCaptured: (Bitmap) -> Unit
) {
    imageCapture?.let { capture ->
        // Use in-memory capture with OnImageCapturedCallback
        // This directly provides ImageProxy without saving to file
        capture.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    try {
                        // Convert ImageProxy to Bitmap
                        val bitmap = imageProxyToBitmap(imageProxy, lensFacing)
                        onImageCaptured(bitmap)
                    } catch (e: Exception) {
                        Log.e("PhotoCapture", "Error converting image to bitmap", e)
                    } finally {
                        imageProxy.close()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("PhotoCapture", "Photo capture failed", exception)
                }
            }
        )
    }
}

private fun imageProxyToBitmap(imageProxy: ImageProxy, lensFacing: Int): Bitmap {
    val buffer: ByteBuffer = imageProxy.planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)

    var bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

    // Handle rotation and mirroring
    val rotationDegrees = imageProxy.imageInfo.rotationDegrees
    val matrix = Matrix()

    // Apply rotation
    matrix.postRotate(rotationDegrees.toFloat())

    // Mirror the image if using front camera
    if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
        matrix.postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
    }

    // Apply transformations
    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

    return bitmap
}