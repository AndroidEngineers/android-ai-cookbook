package com.androidengineers.pocketstories

import android.net.Uri
import com.androidengineers.pocketstories.data.*
import java.io.File
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StoriesViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun mainDispatcher() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun reset() {
        Dispatchers.resetMain()
    }

    private class MemoryStore : StoryPersistence {
        override val stories = MutableStateFlow<List<Story>>(emptyList())

        override suspend fun save(story: Story) {
            stories.value = stories.value.filterNot { it.id == story.id } + story
        }

        override suspend fun delete(id: String) {
            stories.value = stories.value.filterNot { it.id == id }
        }
    }

    private class Model : ModelAccess {
        override fun installed() = true

        override suspend fun download(progress: (Float) -> Unit) {}

        override suspend fun import(uri: Uri, progress: (Float) -> Unit) {}

        override suspend fun remove() {}
    }

    private class Images : SceneImages {
        override suspend fun import(uri: Uri) = File("unused.jpg")

        override suspend fun remove(paths: List<String>) {}
    }

    private class Streaming : StoryGenerator {
        var calls = 0
        var emit: (String) -> Unit = {}

        override suspend fun generate(prompt: String, image: File?, onChunk: (String) -> Unit) {
            calls++
            emit = onChunk
            onChunk("A robot wakes.")
            awaitCancellation()
        }
    }

    @Test
    fun duplicateGenerateAndLateCallbackCannotCorruptStoppedDraft() =
        runTest(dispatcher) {
            val store = MemoryStore()
            val generator = Streaming()
            val vm = StoriesViewModel(store, Model(), Images(), generator)
            vm.newStory()
            vm.begin()
            vm.generate()
            runCurrent()
            vm.generate()
            runCurrent()
            assertEquals(1, generator.calls)
            vm.stop()
            runCurrent()
            generator.emit(" LATE")
            runCurrent()
            assertFalse(vm.state.value.busy)
            assertEquals("A robot wakes.", vm.state.value.current!!.draft)
            assertEquals("A robot wakes.", store.stories.value.single().draft)
        }

    @Test
    fun acceptedDraftBecomesSingleSavedScene() =
        runTest(dispatcher) {
            val store = MemoryStore()
            val vm =
                StoriesViewModel(
                    store,
                    Model(),
                    Images(),
                    object : StoryGenerator {
                        override suspend fun generate(
                            prompt: String,
                            image: File?,
                            onChunk: (String) -> Unit,
                        ) {
                            onChunk("The key glows.")
                        }
                    },
                )
            vm.newStory()
            vm.begin()
            vm.generate()
            runCurrent()
            vm.accept()
            runCurrent()
            assertEquals(1, store.stories.value.single().scenes.size)
            assertEquals("", store.stories.value.single().draft)
            vm.accept()
            runCurrent()
            assertEquals(1, store.stories.value.single().scenes.size)
        }

    @Test
    fun backgroundStopsInferenceAndPreservesDraft() =
        runTest(dispatcher) {
            val store = MemoryStore()
            val vm = StoriesViewModel(store, Model(), Images(), Streaming())
            vm.newStory()
            vm.begin()
            vm.generate()
            runCurrent()
            vm.background()
            runCurrent()
            assertFalse(vm.state.value.busy)
            assertEquals("A robot wakes.", store.stories.value.single().draft)
        }

    @Test
    fun draftIsCheckpointedBeforeGenerationCompletes() =
        runTest(dispatcher) {
            val store = MemoryStore()
            val vm = StoriesViewModel(store, Model(), Images(), Streaming())
            vm.newStory()
            vm.begin()
            vm.generate()
            runCurrent()
            advanceTimeBy(800)
            runCurrent()
            assertTrue(vm.state.value.busy)
            assertEquals("A robot wakes.", store.stories.value.single().draft)
            vm.stop()
            runCurrent()
        }
}
