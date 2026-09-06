package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageUtils {
    fun saveBitmapToInternalStorage(context: Context, bitmap: Bitmap): String {
        val dir = File(context.filesDir, "vendor_photos").apply { mkdirs() }
        val file = File(dir, "vendor_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return file.absolutePath
    }

    fun saveUriToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val dir = File(context.filesDir, "vendor_photos").apply { mkdirs() }
            val file = File(dir, "vendor_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                inputStream?.copyTo(out)
            }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}
