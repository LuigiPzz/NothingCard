package com.nothing.card.ui.screens.scanner

import kotlinx.coroutines.launch
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.nothing.card.ui.theme.NothingBlack
import com.nothing.card.ui.theme.NothingWhite
import com.nothing.card.util.BarcodeAnalyzer
import java.util.concurrent.Executors

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera

@Composable
fun ScannerScreen(
    onBarcodeScanned: (String, String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var isFlashOn by remember { mutableStateOf(false) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    var isScanned by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize().background(NothingBlack)) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setTargetResolution(Size(1280, 720))
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also {
                                it.setAnalyzer(cameraExecutor, BarcodeAnalyzer { barcode ->
                                    if (!isScanned) {
                                        val value = barcode.rawValue ?: ""
                                        if (value.isNotEmpty()) {
                                            isScanned = true
                                            scope.launch {
                                                // Success animation then navigate
                                                kotlinx.coroutines.delay(150)
                                                onBarcodeScanned(value, getBarcodeFormatName(barcode.format))
                                            }
                                        }
                                    }
                                })
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
                        } catch (e: Exception) {
                            // Handle errors
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "CAMERA PERMISSION REQUIRED",
                    color = NothingWhite,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        // Success Flash Overlay
        androidx.compose.animation.AnimatedVisibility(
            visible = isScanned,
            enter = androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(100)),
            exit = androidx.compose.animation.fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.3f))
            )
        }

        // Overlay
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = NothingWhite)
                }
                Text(text = "Scan Card", style = MaterialTheme.typography.headlineMedium, color = NothingWhite)
                IconButton(onClick = { 
                    isFlashOn = !isFlashOn
                    camera?.cameraControl?.enableTorch(isFlashOn)
                }) {
                    Icon(
                        imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Toggle Flash",
                        tint = if (isFlashOn) Color.Yellow else NothingWhite
                    )
                }
            }

            // Scanning area indicator
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .background(Color.Transparent)
            ) {
                // Future: add corner borders here
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NothingBlack.copy(alpha = 0.5f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "POINT AT A BARCODE",
                    style = MaterialTheme.typography.labelMedium,
                    color = NothingWhite
                )
            }
        }
    }
}

private fun getBarcodeFormatName(format: Int): String {
    return when (format) {
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_QR_CODE -> "QR_CODE"
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_AZTEC -> "AZTEC"
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODABAR -> "CODABAR"
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_128 -> "CODE_128"
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_39 -> "CODE_39"
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_93 -> "CODE_93"
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_DATA_MATRIX -> "DATA_MATRIX"
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_13 -> "EAN_13"
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_8 -> "EAN_8"
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_ITF -> "ITF"
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_PDF417 -> "PDF417"
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_A -> "UPC_A"
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_E -> "UPC_E"
        else -> "UNKNOWN"
    }
}
