package com.example.cordiprueba.Inicio.composables

import android.content.Context
import android.net.Uri
import java.io.File

fun uriToFile(context: Context, uri: Uri): File? {
    val inputStream = context.contentResolver.openInputStream(uri) ?: return null
    val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")

    try {
        inputStream.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return file
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}