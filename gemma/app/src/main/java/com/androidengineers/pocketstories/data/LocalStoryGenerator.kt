package com.androidengineers.pocketstories.data

import com.google.ai.edge.litertlm.*
import java.io.File
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface StoryGenerator {
    suspend fun generate(prompt: String, image: File?, onChunk: (String) -> Unit)
}

class LocalStoryGenerator(private val model: ModelStore, private val cache: File) : StoryGenerator {
    private val ownership = Mutex()

    override suspend fun generate(prompt: String, image: File?, onChunk: (String) -> Unit) =
        ownership.withLock {
            withContext(Dispatchers.IO) {
                check(model.installed()) { "Set up your offline model first." }
                val engine =
                    Engine(
                        EngineConfig(
                            modelPath = model.file.path,
                            backend = Backend.GPU(),
                            visionBackend = Backend.GPU(),
                            maxNumTokens = 4096,
                            maxNumImages = 1,
                            cacheDir = cache.path,
                        )
                    )
                var initialized = false
                try {
                    engine.initialize()
                    initialized = true
                    currentCoroutineContext().ensureActive()
                    val conversation =
                        engine.createConversation(
                            ConversationConfig(
                                samplerConfig =
                                    SamplerConfig(topK = 40, topP = 0.9, temperature = 0.8),
                                extraContext = mapOf("enable_thinking" to false),
                                maxOutputToken = 320,
                            )
                        )
                    try {
                        val finished = CompletableDeferred<Unit>()
                        val content = mutableListOf<Content>()
                        image?.let { content.add(Content.ImageFile(it.path)) }
                        content.add(Content.Text(prompt))
                        conversation.sendMessageAsync(
                            Contents.of(content),
                            object : MessageCallback {
                                override fun onMessage(message: Message) {
                                    onChunk(message.toString())
                                }

                                override fun onDone() {
                                    finished.complete(Unit)
                                }

                                override fun onError(throwable: Throwable) {
                                    finished.completeExceptionally(throwable)
                                }
                            },
                        )
                        try {
                            withTimeout(120_000) { finished.await() }
                        } finally {
                            // Flow cancellation alone does not stop this SDK. Wait for its terminal
                            // callback before closing native handles.
                            if (!finished.isCompleted)
                                withContext(NonCancellable) {
                                    conversation.cancelProcess()
                                    runCatching { finished.await() }
                                }
                        }
                    } finally {
                        conversation.close()
                    }
                } finally {
                    if (initialized) engine.close()
                }
            }
        }
}
