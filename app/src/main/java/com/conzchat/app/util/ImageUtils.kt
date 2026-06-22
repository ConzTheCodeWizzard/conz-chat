package com.conzchat.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.InputStream

object ImageUtils {

    fun compressImageToBase64(context: Context, uri: Uri, maxSize: Int = 400, quality: Int = 75): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val original = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val scaled = scaleBitmap(original, maxSize)
            val baos = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, quality, baos)
            val bytes = baos.toByteArray()
            "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }

    fun compressCoverPhotoToBase64(context: Context, uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val original = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Cover photo: 600x220 crop
            val targetW = 600
            val targetH = 220
            val scale = maxOf(targetW.toFloat() / original.width, targetH.toFloat() / original.height)
            val scaledW = (original.width * scale).toInt()
            val scaledH = (original.height * scale).toInt()
            val scaled = Bitmap.createScaledBitmap(original, scaledW, scaledH, true)
            val startX = (scaledW - targetW) / 2
            val startY = (scaledH - targetH) / 2
            val cropped = Bitmap.createBitmap(scaled, startX, startY, targetW, targetH)

            val baos = ByteArrayOutputStream()
            cropped.compress(Bitmap.CompressFormat.JPEG, 65, baos)
            val bytes = baos.toByteArray()
            "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }

    fun compressImageToBase64ForChat(context: Context, uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val original = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val scaled = scaleBitmap(original, 800)
            val baos = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, 70, baos)
            val bytes = baos.toByteArray()
            "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }

    private fun scaleBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        var w = bitmap.width
        var h = bitmap.height
        if (w > h) {
            if (w > maxSize) { h = h * maxSize / w; w = maxSize }
        } else {
            if (h > maxSize) { w = w * maxSize / h; h = maxSize }
        }
        return Bitmap.createScaledBitmap(bitmap, w, h, true)
    }

    fun bitmapToBase64(bitmap: Bitmap, quality: Int = 75): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
        return "data:image/jpeg;base64," + Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
    }
}
