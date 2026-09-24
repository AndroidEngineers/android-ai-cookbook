package com.androidengineers.pocketstories.data

import android.content.Context
import android.net.Uri
import java.io.File
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

object GemmaArtifact {
    const val name = "gemma-4-E2B-it.litertlm"
    const val bytes = 2588147712L
    const val sha256 = "181938105e0eefd105961417e8da75903eacda102c4fce9ce90f50b97139a63c"
    const val url =
        "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/blob/b3ca0d2f076785a8f4b2219ddbd2bdb99954eae1/gemma-4-E2B-it.litertlm"
}

interface ModelAccess {
    fun installed(): Boolean

    suspend fun download(progress: (Float) -> Unit)

    suspend fun import(uri: Uri, progress: (Float) -> Unit)

    suspend fun remove()
}

class ModelStore(private val context: Context) : ModelAccess {
    val file = File(context.noBackupFilesDir, GemmaArtifact.name)

    override suspend fun download(progress: (Float) -> Unit) {
        withContext(Dispatchers.IO) { removeObsoleteModel() }
        ModelDownloader()
            .download(
                GemmaArtifact.url.replace("/blob/", "/resolve/"),
                file,
                GemmaArtifact.bytes,
                GemmaArtifact.sha256,
                progress = progress,
            )
    }

    // This earlier artifact has no vision encoder. Remove only our known obsolete cache
    // when the user explicitly requests setup, never story data or imported originals.
    private fun removeObsoleteModel() {
        listOf("gemma-4-E2B-it-gpu.litertlm", "gemma-4-E2B-it-gpu.litertlm.download").forEach { name
            ->
            val obsolete = File(context.noBackupFilesDir, name)
            check(!obsolete.exists() || obsolete.delete()) {
                "Could not remove the old text-only model. Try setup again."
            }
        }
    }

    override fun installed() = file.exists() && file.length() == GemmaArtifact.bytes

    override suspend fun import(uri: Uri, progress: (Float) -> Unit) =
        withContext(Dispatchers.IO) {
            removeObsoleteModel()
            require(
                context.noBackupFilesDir.usableSpace > GemmaArtifact.bytes + 512L * 1024 * 1024
            ) {
                "Not enough space. Free at least 3.2 GB in addition to your downloaded model."
            }
            val temp = File(context.noBackupFilesDir, "model.importing")
            try {
                val digest = MessageDigest.getInstance("SHA-256")
                var total = 0L
                context.contentResolver.openInputStream(uri).use { input ->
                    requireNotNull(input) {
                        "Cannot read this file. Choose the downloaded model again."
                    }
                    temp.outputStream().use { out ->
                        val buffer = ByteArray(1024 * 1024)
                        while (true) {
                            currentCoroutineContext().ensureActive()
                            val count = input.read(buffer)
                            if (count < 0) break
                            total += count
                            require(total <= GemmaArtifact.bytes) {
                                "Wrong model file. Use the linked GPU model."
                            }
                            digest.update(buffer, 0, count)
                            out.write(buffer, 0, count)
                            progress(total.toFloat() / GemmaArtifact.bytes)
                        }
                        out.fd.sync()
                    }
                }
                require(
                    total == GemmaArtifact.bytes &&
                        digest.digest().joinToString("") { "%02x".format(it) } ==
                            GemmaArtifact.sha256
                ) {
                    "Model verification failed. Download the exact linked file and try again."
                }
                check(temp.renameTo(file)) { "Could not finish model import." }
                File(file.parentFile, file.name + ".download").delete()
                Unit
            } finally {
                temp.delete()
            }
        }

    override suspend fun remove() =
        withContext(Dispatchers.IO) {
            File(file.parentFile, file.name + ".download").delete()
            check(!file.exists() || file.delete()) { "Could not remove model." }
        }
}
