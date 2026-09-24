package com.androidengineers.pocketstories.data

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request

/** Resumable transfer to private staging storage. Only verified bytes become a usable model. */
class ModelDownloader(
    private val client: OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .followSslRedirects(false)
            .build()
) {
    suspend fun download(
        url: String,
        target: File,
        size: Long,
        hash: String,
        reserveBytes: Long = 512L * 1024 * 1024,
        progress: (Float) -> Unit,
    ) =
        withContext(Dispatchers.IO) {
            val partial = File(target.parentFile, target.name + ".download")
            if (partial.length() > size) partial.delete()
            require(target.parentFile!!.usableSpace >= size - partial.length() + reserveBytes) {
                "Not enough storage. Free at least 3.2 GB for the model and working space, then retry."
            }
            if (partial.length() < size) {
                val offset = partial.length()
                val request = Request.Builder().url(url).header("Accept-Encoding", "identity")
                if (offset > 0) request.header("Range", "bytes=$offset-")
                val call = client.newCall(request.build())
                coroutineScope {
                    val cancelNetwork =
                        launch(start = CoroutineStart.UNDISPATCHED) {
                            try {
                                awaitCancellation()
                            } finally {
                                call.cancel()
                            }
                        }
                    try {
                        call.execute().use { response ->
                            if (response.code == 416) {
                                partial.delete()
                                throw IOException(
                                    "The saved download expired. Tap Download to restart."
                                )
                            }
                            if (response.code != 200 && response.code != 206)
                                throw IOException(
                                    "Download unavailable (HTTP ${response.code}). Check your connection and retry."
                                )
                            val append = response.code == 206
                            if (append)
                                require(
                                    response.header("Content-Range") ==
                                        "bytes $offset-${size-1}/$size"
                                ) {
                                    "Unexpected download range. The server did not return the requested model bytes."
                                }
                            var total = if (append) offset else 0L
                            require(
                                target.parentFile!!.usableSpace >= size - total + reserveBytes
                            ) {
                                "More storage is needed to restart this download. Free space and retry."
                            }
                            val body =
                                response.body
                                    ?: throw IOException("Empty download response. Try again.")
                            FileOutputStream(partial, append).use { output ->
                                body.byteStream().use { input ->
                                    val buffer = ByteArray(256 * 1024)
                                    while (true) {
                                        currentCoroutineContext().ensureActive()
                                        val count = input.read(buffer)
                                        if (count < 0) break
                                        total += count
                                        if (total > size) {
                                            partial.delete()
                                            throw IOException(
                                                "Unexpected model size. Download stopped."
                                            )
                                        }
                                        output.write(buffer, 0, count)
                                        progress(total.toFloat() / size)
                                    }
                                }
                                output.fd.sync()
                            }
                            if (total != size)
                                throw IOException("Download interrupted. Tap Download to resume.")
                        }
                    } catch (e: Exception) {
                        currentCoroutineContext().ensureActive()
                        throw e
                    } finally {
                        cancelNetwork.cancel()
                    }
                }
            }
            progress(1f)
            val digest = MessageDigest.getInstance("SHA-256")
            partial.inputStream().use { input ->
                val buffer = ByteArray(1024 * 1024)
                while (true) {
                    currentCoroutineContext().ensureActive()
                    val count = input.read(buffer)
                    if (count < 0) break
                    digest.update(buffer, 0, count)
                }
            }
            if (digest.digest().joinToString("") { "%02x".format(it) } != hash) {
                partial.delete()
                throw IOException("Model verification failed. Tap Download to try a fresh copy.")
            }
            currentCoroutineContext().ensureActive()
            check(partial.renameTo(target)) {
                "Could not install the verified model. Free storage and retry."
            }
        }
}
