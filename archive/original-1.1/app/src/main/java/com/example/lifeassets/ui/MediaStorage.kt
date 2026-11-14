package com.example.lifeassets.ui

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

suspend fun copyImageToInternalStorage(context: Context, source: Uri): String? =
    withContext(Dispatchers.IO) {
        runCatching {
            val directory = File(context.filesDir, "asset-images").apply { mkdirs() }
            val destination = File(directory, "${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(source)?.use { input ->
                destination.outputStream().use { output -> input.copyTo(output) }
            } ?: return@withContext null
            destination.absolutePath
        }.getOrNull()
    }

fun localImageUri(path: String?): Uri? = path?.let { File(it).takeIf(File::exists)?.toUri() }
