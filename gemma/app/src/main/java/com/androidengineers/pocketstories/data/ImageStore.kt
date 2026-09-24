package com.androidengineers.pocketstories.data

import android.content.Context
import android.graphics.ImageDecoder
import android.net.Uri
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface SceneImages {
    suspend fun import(uri: Uri): File

    suspend fun remove(paths: List<String>)
}

class ImageStore(private val context: Context) : SceneImages {
    override suspend fun remove(paths: List<String>) =
        withContext(Dispatchers.IO) {
            paths.distinct().forEach { path ->
                val file = File(path)
                if (file.parentFile?.canonicalFile == folder.canonicalFile) file.delete()
            }
        }

    val folder = File(context.noBackupFilesDir, "scenes").apply { mkdirs() }

    override suspend fun import(uri: Uri): File =
        withContext(Dispatchers.IO) {
            val bitmap =
                ImageDecoder.decodeBitmap(
                    ImageDecoder.createSource(context.contentResolver, uri)
                ) { decoder, info, _ ->
                    val ratio = 768f / maxOf(info.size.width, info.size.height)
                    if (ratio < 1)
                        decoder.setTargetSize(
                            (info.size.width * ratio).toInt().coerceAtLeast(1),
                            (info.size.height * ratio).toInt().coerceAtLeast(1),
                        )
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                }
            val output = File(folder, "${UUID.randomUUID()}.jpg")
            try {
                output.outputStream().use {
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, it)
                }
                output
            } finally {
                bitmap.recycle()
            }
        }
}
