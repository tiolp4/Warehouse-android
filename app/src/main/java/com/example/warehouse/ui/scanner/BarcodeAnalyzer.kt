package com.example.warehouse.ui.scanner

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

/**
 * CameraX [ImageAnalysis.Analyzer] that decodes barcodes via ML Kit
 * and invokes [onDetected] for the first non-empty value, then ignores
 * further frames until the host releases the lock.
 */
class BarcodeAnalyzer(
    private val onDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    @Volatile private var paused = false

    private val scanner: BarcodeScanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E,
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_CODE_39,
                Barcode.FORMAT_ITF,
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_DATA_MATRIX
            ).build()
    )

    fun pause()  { paused = true  }
    fun resume() { paused = false }

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        if (paused) { imageProxy.close(); return }
        val mediaImage = imageProxy.image
        if (mediaImage == null) { imageProxy.close(); return }

        val input = InputImage.fromMediaImage(
            mediaImage, imageProxy.imageInfo.rotationDegrees
        )
        scanner.process(input)
            .addOnSuccessListener { codes ->
                if (paused) return@addOnSuccessListener
                val value = codes.firstOrNull { !it.rawValue.isNullOrBlank() }?.rawValue
                if (value != null) {
                    paused = true
                    onDetected(value)
                }
            }
            .addOnCompleteListener { imageProxy.close() }
    }
}
