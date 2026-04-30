package com.nothing.card.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix

object BarcodeGenerator {

    fun generateBarcode(content: String, format: BarcodeFormat, width: Int, height: Int): Bitmap? {
        if (content.isBlank()) return null
        
        return try {
            // Normalize content for EAN/UPC if needed
            val normalizedContent = when (format) {
                BarcodeFormat.EAN_13 -> content.padStart(13, '0').takeLast(13)
                BarcodeFormat.EAN_8 -> content.padStart(8, '0').takeLast(8)
                BarcodeFormat.UPC_A -> content.padStart(12, '0').takeLast(12)
                BarcodeFormat.UPC_E -> content.padStart(8, '0').takeLast(8)
                else -> content
            }

            val bitMatrix: BitMatrix = try {
                MultiFormatWriter().encode(normalizedContent, format, width, height)
            } catch (e: Exception) {
                // If specific format fails, try auto-detecting or fallback to CODE_128 which is very versatile
                MultiFormatWriter().encode(normalizedContent, BarcodeFormat.CODE_128, width, height)
            }

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    fun mapToZXingFormat(formatString: String): BarcodeFormat {
        return try {
            BarcodeFormat.valueOf(formatString)
        } catch (e: Exception) {
            BarcodeFormat.QR_CODE
        }
    }
}
