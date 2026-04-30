package com.nothing.card.ui.screens.vendor

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

@Composable
fun GalleryScannerScreen(
    onBarcodeScanned: (String, String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val image = InputImage.fromFilePath(context, uri)
            val scanner = BarcodeScanning.getClient()
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        val barcode = barcodes[0]
                        onBarcodeScanned(barcode.rawValue ?: "", getBarcodeFormatName(barcode.format))
                    } else {
                        onClose()
                    }
                }
                .addOnFailureListener {
                    onClose()
                }
        } else {
            onClose()
        }
    }

    LaunchedEffect(Unit) {
        galleryLauncher.launch("image/*")
    }
}

private fun getBarcodeFormatName(format: Int): String {
    return when (format) {
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_QR_CODE -> "QR_CODE"
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_13 -> "EAN_13"
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_8 -> "EAN_8"
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_A -> "UPC_A"
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_E -> "UPC_E"
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_128 -> "CODE_128"
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_39 -> "CODE_39"
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_93 -> "CODE_93"
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_ITF -> "ITF"
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_DATA_MATRIX -> "DATA_MATRIX"
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_AZTEC -> "AZTEC"
        com.google.mlkit.vision.barcode.common.Barcode.FORMAT_PDF417 -> "PDF417"
        else -> "UNKNOWN"
    }
}
