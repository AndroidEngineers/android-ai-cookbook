package com.androidengineers.pocketstories

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidengineers.pocketstories.data.*
import java.io.File
import java.time.LocalTime
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class StoriesState(
    val page: String = "shelf",
    val stories: List<Story> = emptyList(),
    val current: Story? = null,
    val genre: String = "Fantasy",
    val title: String = "",
    val action: String = "",
    val photo: String? = null,
    val busy: Boolean = false,
    val stopping: Boolean = false,
    val importing: Boolean = false,
    val progress: Float = 0f,
    val modelReady: Boolean = false,
    val error: String? = null,
    val status: String = "",
    val sensorEnabled: Boolean = false,
)

class StoriesViewModel(
    private val store: StoryPersistence,
    private val model: ModelAccess,
    private val images: SceneImages,
    private val generator: StoryGenerator,
) : ViewModel() {
    private val mutable = MutableStateFlow(StoriesState(modelReady = model.installed()))
    val state = mutable.asStateFlow()
    private var operation: Job? = null

    init {
        viewModelScope.launch {
            store.stories
                .catch { mutable.update { it.copy(error = "Could not read your bookshelf.") } }
                .collect { items -> mutable.update { it.copy(stories = items) } }
        }
    }

    fun page(page: String) {
        if (!state.value.busy && !state.value.importing)
            mutable.update { it.copy(page = page, error = null) }
    }

    fun title(value: String) {
        mutable.update { it.copy(title = value.take(80)) }
    }

    fun genre(value: String) {
        mutable.update { it.copy(genre = value) }
    }

    fun action(value: String) {
        mutable.update { it.copy(action = value.take(500)) }
    }

    fun clearError() {
        mutable.update { it.copy(error = null) }
    }

    fun newStory() {
        mutable.update {
            it.copy(
                page = "new",
                current = null,
                title = "",
                action = "",
                photo = null,
                error = null,
            )
        }
    }

    fun open(story: Story) {
        mutable.update {
            it.copy(
                page = "reader",
                current = story,
                photo = story.draftImage,
                action = "",
                error = null,
            )
        }
    }

    fun begin() {
        val s = state.value
        mutable.update {
            it.copy(
                current =
                    Story(
                        title = s.title.ifBlank { "An ordinary little adventure" },
                        genre = s.genre,
                    ),
                page = "camera",
            )
        }
    }

    fun photo(uri: Uri) {
        if (state.value.busy) return
        mutable.update { it.copy(busy = true, status = "Preparing your scene…") }
        operation = viewModelScope.launch {
            try {
                val file = images.import(uri)
                val story = state.value.current?.copy(draftImage = file.path)
                if (story != null) store.save(story)
                mutable.update {
                    it.copy(current = story, photo = file.path, page = "reader", status = "")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                mutable.update {
                    it.copy(error = "Could not open this image. Try a smaller photo.")
                }
            } finally {
                mutable.update { it.copy(busy = false) }
            }
        }
    }

    fun downloadModel() {
        if (state.value.busy || state.value.importing) return
        mutable.update {
            it.copy(
                importing = true,
                stopping = false,
                error = null,
                progress = 0f,
                status = "Downloading your storyteller…",
            )
        }
        operation = viewModelScope.launch {
            try {
                model.download { progress ->
                    mutable.update {
                        it.copy(
                            progress = progress,
                            status =
                                if (progress >= 1f) "Verifying model…"
                                else "Downloading your storyteller…",
                        )
                    }
                }
                mutable.update {
                    it.copy(modelReady = true, status = "Your offline storyteller is ready.")
                }
            } catch (e: CancellationException) {
                mutable.update { it.copy(status = "Download paused. Tap Download to resume.") }
            } catch (e: Exception) {
                mutable.update {
                    it.copy(
                        error =
                            e.message ?: "Download interrupted. Check your connection and retry.",
                        status = "",
                    )
                }
            } finally {
                mutable.update { it.copy(importing = false, stopping = false) }
            }
        }
    }

    fun importModel(uri: Uri) {
        if (state.value.busy || state.value.importing) return
        mutable.update {
            it.copy(
                importing = true,
                error = null,
                progress = 0f,
                status = "Importing & verifying…",
            )
        }
        operation = viewModelScope.launch {
            try {
                model.import(uri) { value -> mutable.update { it.copy(progress = value) } }
                mutable.update {
                    it.copy(modelReady = true, status = "Your offline storyteller is ready.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                mutable.update { it.copy(error = e.message ?: "Import failed.") }
            } finally {
                mutable.update { it.copy(importing = false, stopping = false) }
            }
        }
    }

    fun removeModel() {
        if (state.value.busy || state.value.importing) return
        mutable.update { it.copy(importing = true) }
        operation = viewModelScope.launch {
            try {
                model.remove()
                mutable.update { it.copy(modelReady = false, status = "") }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                mutable.update { it.copy(error = "Could not remove model.") }
            } finally {
                mutable.update { it.copy(importing = false, stopping = false) }
            }
        }
    }

    fun generate(direction: String? = null) {
        val s = state.value
        val story = s.current ?: return
        if (s.busy || s.importing || story.sample) return
        if (!s.modelReady) {
            page("model")
            return
        }
        val next =
            direction
                ?: s.action.ifBlank {
                    if (story.scenes.isEmpty())
                        "Begin an adventure using the objects in this scene."
                    else "Continue the adventure."
                }
        val draft = story.copy(draft = "", draftAction = next, draftImage = s.photo)
        mutable.update {
            it.copy(
                current = draft,
                busy = true,
                stopping = false,
                error = null,
                status = "Waking your storyteller…",
            )
        }
        operation = viewModelScope.launch {
            val checkpoints = launch {
                while (isActive) {
                    delay(750)
                    state.value.current?.let { current ->
                        try {
                            store.save(current)
                        } catch (e: Exception) {
                            if (e is CancellationException) throw e
                            mutable.update {
                                it.copy(
                                    error =
                                        "Could not checkpoint your draft. Free storage before leaving."
                                )
                            }
                        }
                    }
                }
            }
            try {
                store.save(draft)
                withTimeout(120_000) {
                    generator.generate(
                        storyPrompt(story, next, LocalTime.now().hour !in 6..18),
                        s.photo?.let(::File),
                    ) { chunk ->
                        if (isActive)
                            mutable.update { old ->
                                old.copy(
                                    current =
                                        old.current?.copy(
                                            draft = (old.current.draft + chunk).take(5000)
                                        ),
                                    status = "Writing your next scene…",
                                )
                            }
                    }
                }
                if (state.value.current?.draft.isNullOrBlank())
                    error("No story was returned. Try another scene.")
            } catch (e: TimeoutCancellationException) {
                mutable.update {
                    it.copy(
                        error =
                            "Generation took too long. Your partial scene is kept; try a shorter direction."
                    )
                }
            } catch (e: CancellationException) {
                mutable.update { it.copy(status = "Stopped. Review or discard your draft.") }
            } catch (e: Exception) {
                mutable.update {
                    it.copy(
                        error =
                            "Could not generate on this device. ${e.message?.take(180)?:"Check model setup and available memory."}"
                    )
                }
            } catch (e: LinkageError) {
                mutable.update {
                    it.copy(
                        error = "This device cannot load the local runtime. Your stories are safe."
                    )
                }
            } finally {
                withContext(NonCancellable) {
                    checkpoints.cancelAndJoin()
                    runCatching { state.value.current?.let { store.save(it) } }
                        .onFailure {
                            mutable.update {
                                it.copy(
                                    error =
                                        "Could not save this scene. Keep the app open and free storage."
                                )
                            }
                        }
                }
                mutable.update {
                    it.copy(
                        busy = false,
                        stopping = false,
                        status = if (it.stopping) "Stopped. Your draft is kept." else "",
                    )
                }
            }
        }
    }

    fun stop() {
        mutable.update { it.copy(stopping = true, status = "Stopping safely…") }
        operation?.cancel()
    }

    fun background() {
        if (state.value.busy && !state.value.importing) stop()
    }

    fun accept() {
        val story = state.value.current ?: return
        if (state.value.busy || story.draft.isBlank()) return
        persist(keepDraft(story))
        mutable.update { it.copy(photo = null, action = "") }
    }

    fun discard() {
        val s = state.value.current ?: return
        if (!state.value.busy) persist(s.copy(draft = "", draftAction = "", draftImage = null))
    }

    private fun persist(story: Story) {
        viewModelScope.launch {
            runCatching { store.save(story) }
                .onSuccess { mutable.update { it.copy(current = story) } }
                .onFailure {
                    mutable.update {
                        it.copy(error = "Could not save. Free storage and try again.")
                    }
                }
        }
    }

    fun delete() {
        val story = state.value.current ?: return
        if (state.value.busy || story.sample) return
        viewModelScope.launch {
            runCatching {
                store.delete(story.id)
                images.remove(
                    story.scenes.mapNotNull { it.image } +
                        listOfNotNull(story.draftImage, state.value.photo)
                )
            }
                .onSuccess { mutable.update { it.copy(page = "shelf", current = null) } }
                .onFailure { mutable.update { it.copy(error = "Could not delete story.") } }
        }
    }

    fun sensors(enabled: Boolean) {
        mutable.update { it.copy(sensorEnabled = enabled) }
    }

    fun shake() {
        val s = state.value
        if (
            s.page == "reader" &&
                s.sensorEnabled &&
                !s.busy &&
                s.current?.sample == false &&
                s.current.draft.isBlank()
        )
            action("A gentle shake awakens a mysterious spark. What happens next?")
    }
}
