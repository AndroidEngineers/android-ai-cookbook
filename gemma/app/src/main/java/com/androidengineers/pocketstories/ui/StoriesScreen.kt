package com.androidengineers.pocketstories.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidengineers.pocketstories.*
import com.androidengineers.pocketstories.R
import com.androidengineers.pocketstories.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun StoriesScreen(vm: StoriesViewModel) {
    val s by vm.state.collectAsStateWithLifecycle()
    val picker =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
            it?.let(vm::importModel)
        }
    var deleteModel by remember { mutableStateOf(false) }
    var deleteStory by remember { mutableStateOf(false) }
    BackHandler(s.page != "shelf") {
        if (s.busy || s.importing) vm.stop()
        else vm.page(if (s.page == "camera" && s.current != null) "reader" else "shelf")
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (s.page != "camera")
                Row(
                    Modifier.fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (s.page != "shelf")
                        IconButton(
                            onClick = { vm.page("shelf") },
                            enabled = !s.busy && !s.importing,
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Bookshelf")
                        }
                    else
                        Icon(
                            Icons.AutoMirrored.Outlined.MenuBook,
                            null,
                            Modifier.padding(8.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    Column(Modifier.weight(1f)) {
                        Text("PocketStories", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "BY ANDROID ENGINEERS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = { vm.page("model") }, enabled = !s.busy && !s.importing) {
                        Icon(Icons.Outlined.Tune, "Model setup")
                    }
                }
        },
        bottomBar = {
            if (s.page == "shelf")
                Surface(tonalElevation = 2.dp) {
                    Row(
                        Modifier.navigationBarsPadding().fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        TextButton(onClick = {}) {
                            Icon(Icons.AutoMirrored.Outlined.MenuBook, null)
                            Spacer(Modifier.width(8.dp))
                            Text("My stories")
                        }
                        TextButton(onClick = vm::newStory) {
                            Icon(Icons.Outlined.AutoAwesome, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Create")
                        }
                    }
                }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding()) {
            s.error?.let { message ->
                Surface(color = MaterialTheme.colorScheme.errorContainer) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            message,
                            Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        IconButton(onClick = vm::clearError) {
                            Icon(Icons.Outlined.Close, "Dismiss error")
                        }
                    }
                }
            }
            when (s.page) {
                "shelf" -> Bookshelf(s, vm)
                "new" -> NewStory(s, vm)
                "model" ->
                    ModelSetup(
                        s,
                        { picker.launch(arrayOf("*/*")) },
                        { deleteModel = true },
                        vm::stop,
                        vm::downloadModel,
                        { vm.page(if (s.current != null) "reader" else "new") },
                    )
                "camera" ->
                    CameraScreen(onPhoto = vm::photo, onBack = { vm.page("reader") }, busy = s.busy)
                "reader" -> Reader(s, vm, { deleteStory = true })
            }
        }
    }
    if (deleteModel)
        AlertDialog(
            onDismissRequest = { deleteModel = false },
            title = { Text("Remove offline model?") },
            text = {
                Text(
                    "Your stories stay on this phone. You will need to import the model again to generate new scenes."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteModel = false
                        vm.removeModel()
                    }
                ) {
                    Text("Remove model")
                }
            },
            dismissButton = { TextButton(onClick = { deleteModel = false }) { Text("Keep") } },
        )
    if (deleteStory)
        AlertDialog(
            onDismissRequest = { deleteStory = false },
            title = { Text("Delete this adventure?") },
            text = { Text("This removes the saved scenes from your bookshelf.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteStory = false
                        vm.delete()
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = { TextButton(onClick = { deleteStory = false }) { Text("Keep") } },
        )
}

@Composable
private fun Eyebrow(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.secondary,
    )
}

@Composable
private fun ActionButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
        shape = RoundedCornerShape(18.dp),
        contentPadding = PaddingValues(16.dp),
    ) {
        Text(text, Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Outlined.ArrowForward, null)
    }
}

@Composable
private fun Bookshelf(s: StoriesState, vm: StoriesViewModel) {
    LazyColumn(
        modifier = Modifier.testTag("story-list"),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        item {
            Column {
                Eyebrow("A LITTLE WONDER, EVERYWHERE")
                Spacer(Modifier.height(10.dp))
                Text(
                    "Ordinary things.\nExtraordinary tales.",
                    style = MaterialTheme.typography.headlineLarge,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "Turn the world around you into an adventure of your own.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            Column(Modifier.clip(RoundedCornerShape(28.dp)).background(Ink)) {
                Box {
                    Image(
                        painterResource(R.drawable.story_cover),
                        "A tiny brass robot discovers a mug-shaped tower in a moonlit garden",
                        Modifier.fillMaxWidth().height(218.dp),
                        contentScale = ContentScale.Crop,
                    )
                    Surface(
                        Modifier.padding(16.dp).align(Alignment.TopStart),
                        color = Paper.copy(alpha = .94f),
                        shape = RoundedCornerShape(50),
                    ) {
                        Text(
                            "✦  YOUR WORLD, REIMAGINED",
                            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            color = Ink,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
                Column(Modifier.padding(22.dp)) {
                    Text(
                        "Every object has a story.",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Paper,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "A mug. A key. A tiny robot.\nWhere will your next adventure begin?",
                        color = Color(0xFFE0D5E3),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(18.dp))
                    Button(
                        onClick = vm::newStory,
                        colors =
                            ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Icon(Icons.Outlined.AutoAwesome, null)
                        Spacer(Modifier.width(10.dp))
                        Text("Start an adventure")
                    }
                }
            }
        }
        item {
            Row(
                Modifier.fillMaxWidth().clickable { vm.page("model") }.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.PhonelinkLock, null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        if (s.modelReady) "Your storyteller is on this phone"
                        else "A storyteller that stays with you",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        if (s.modelReady) "Model installed · no cloud generation"
                        else "Set up Gemma for offline adventures",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(Icons.AutoMirrored.Outlined.ArrowForward, null)
            }
        }
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Your bookshelf", style = MaterialTheme.typography.titleLarge)
                Text(
                    "${s.stories.size} stories",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (s.stories.isEmpty())
            item {
                Text(
                    "Your first adventure is waiting to be written.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        items(s.stories, key = { it.id }) { story -> StoryRow(story) { vm.open(story) } }
        item { StoryRow(sampleStory) { vm.open(sampleStory) } }
        item {
            Text(
                "Made for curious minds. Powered locally by Gemma.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StoryRow(story: Story, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painterResource(R.drawable.story_cover),
                null,
                Modifier.size(72.dp, 88.dp).clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop,
            )
            Column(Modifier.weight(1f).padding(14.dp)) {
                Eyebrow(if (story.sample) "SAMPLE ADVENTURE" else story.genre.uppercase())
                Spacer(Modifier.height(5.dp))
                Text(story.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    if (story.sample) "A peek at the possibilities"
                    else
                        "${story.scenes.size} scenes · ${if(story.draft.isNotBlank()) "draft waiting" else "ready to continue"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(Icons.AutoMirrored.Outlined.ArrowForward, null)
        }
    }
}

@Composable
private fun NewStory(s: StoriesState, vm: StoriesViewModel) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Eyebrow("CHAPTER ZERO")
        Text("What kind of magic?", style = MaterialTheme.typography.headlineLarge)
        Text(
            "Choose a world. Then show us something ordinary.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        val genres =
            listOf(
                "Fantasy" to "Forests, spells & secret doors",
                "Space" to "Small robots, vast galaxies",
                "Mystery" to "Curious clues & hidden things",
                "Comedy" to "A little wonderfully ridiculous",
            )
        genres.forEach { (genre, subtitle) ->
            Surface(
                onClick = { vm.genre(genre) },
                shape = RoundedCornerShape(20.dp),
                color = if (s.genre == genre) Ink else MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        when (genre) {
                            "Space" -> Icons.Outlined.RocketLaunch
                            "Mystery" -> Icons.Outlined.Search
                            "Comedy" -> Icons.Outlined.SentimentSatisfied
                            else -> Icons.Outlined.AutoAwesome
                        },
                        null,
                        tint = if (s.genre == genre) Gold else MaterialTheme.colorScheme.primary,
                    )
                    Column(Modifier.weight(1f).padding(horizontal = 16.dp)) {
                        Text(
                            genre,
                            style = MaterialTheme.typography.titleMedium,
                            color =
                                if (s.genre == genre) Paper
                                else MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color =
                                if (s.genre == genre) Color(0xFFD9CEDD)
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (s.genre == genre) Icon(Icons.Outlined.CheckCircle, null, tint = Gold)
                }
            }
        }
        OutlinedTextField(
            value = s.title,
            onValueChange = vm::title,
            label = { Text("Adventure title (optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        )
        ActionButton("Find your first scene", vm::begin)
        Text(
            "Photos are processed on your phone. We capture only when you tap the shutter.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ModelSetup(
    s: StoriesState,
    onImport: () -> Unit,
    onRemove: () -> Unit,
    onStop: () -> Unit,
    onDownload: () -> Unit,
    onContinue: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Eyebrow("YOUR PRIVATE STORYTELLER")
        Text(
            "A little imagination.\nEntirely on your phone.",
            style = MaterialTheme.typography.headlineLarge,
        )
        Surface(shape = RoundedCornerShape(24.dp), color = Ink) {
            Column(
                Modifier.fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(Icons.Outlined.AutoAwesome, null, tint = Gold, modifier = Modifier.size(32.dp))
                Text(
                    "Gemma 4 · E2B",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Paper,
                )
                Text("Vision + stories  /  Multimodal model", color = Gold)
                Text(
                    "2.59 GB model · Android 12+\nPhysical device with compatible GPU required.",
                    color = Paper,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        Text(
            if (s.modelReady) "Your offline storyteller is ready"
            else "One download. A world of stories.",
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            if (s.modelReady)
                "You're ready to create. Future stories are generated on this phone, even without a connection."
            else
                "Download Gemma with vision, right here. Setup replaces the earlier text-only model automatically. Your saved stories and photos stay intact."
        )
        if (!s.modelReady)
            Text(
                "2.59 GB download · Wi-Fi recommended. Keep at least 3.2 GB free. Keep this app open during setup. Interrupted downloads can resume when you tap Download again.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        if (s.importing) {
            LinearProgressIndicator(progress = { s.progress }, modifier = Modifier.fillMaxWidth())
            Text("${s.status} · ${(s.progress * 100).toInt()}%")
            TextButton(onClick = onStop, enabled = !s.stopping) {
                Text(if (s.stopping) "Stopping…" else "Cancel setup")
            }
        } else {
            if (s.status.isNotBlank()) Text(s.status, style = MaterialTheme.typography.bodyMedium)
            if (s.modelReady) ActionButton("Start creating", onContinue)
            else ActionButton("Download Gemma · 2.59 GB", onDownload)
            var advanced by rememberSaveable { mutableStateOf(false) }
            TextButton(onClick = { advanced = !advanced }) {
                Text(if (advanced) "Hide advanced setup" else "Advanced setup")
            }
            if (advanced) {
                OutlinedButton(onClick = onImport, modifier = Modifier.fillMaxWidth()) {
                    Text("Import an existing model file")
                }
                if (s.modelReady)
                    TextButton(onClick = onRemove) { Text("Remove model from this app") }
            }
        }
        TextButton(onClick = { uriHandler.openUri(GemmaArtifact.url) }) {
            Text("Model details & license")
        }

        HorizontalDivider()
        Text(
            "No account in PocketStories. No API key. No cloud fallback.",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            "Internet is used only to download the model from Hugging Face. Your photos and stories are processed locally and excluded from Android backup. File verification does not guarantee GPU compatibility; the first story checks that on your device.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun Reader(s: StoriesState, vm: StoriesViewModel, onDelete: () -> Unit) {
    val story = s.current ?: return
    LazyColumn(
        modifier = Modifier.testTag("story-list"),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        item {
            Eyebrow(
                if (story.sample) "A HAND-WRITTEN SAMPLE · NOT AI OUTPUT"
                else "${story.genre.uppercase()} · YOUR ADVENTURE"
            )
            Spacer(Modifier.height(10.dp))
            Text(story.title, style = MaterialTheme.typography.headlineLarge)
        }
        if (story.sample)
            item {
                Image(
                    painterResource(R.drawable.story_cover),
                    null,
                    Modifier.fillMaxWidth().height(230.dp).clip(RoundedCornerShape(24.dp)),
                    contentScale = ContentScale.Crop,
                )
            }
        items(story.scenes.withIndex().toList(), key = { it.index }) { (index, scene) ->
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Eyebrow("SCENE ${index+1}")
                Text(scene.text, fontFamily = StorySerif, fontSize = 21.sp, lineHeight = 32.sp)
                HorizontalDivider(Modifier.padding(top = 14.dp))
            }
        }
        if (s.photo != null)
            item {
                ScenePhoto(s.photo)
                Text(
                    "Your captured scene · stays on this phone",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        if (story.sample)
            item {
                Text(
                    "This example shows how a scene could read. Create your own adventure to use Gemma with your camera.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
                ActionButton("Make a story of your own", vm::newStory)
            }
        else {
            if (story.draft.isNotBlank() || s.busy)
                item {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Column(
                            Modifier.fillMaxWidth().padding(22.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Eyebrow(
                                if (s.busy) "IMAGINATION AT WORK" else "YOUR NEXT SCENE · DRAFT"
                            )
                            if (story.draft.isNotBlank())
                                Text(
                                    story.draft,
                                    fontFamily = StorySerif,
                                    fontSize = 21.sp,
                                    lineHeight = 32.sp,
                                )
                            if (s.busy) {
                                LinearProgressIndicator(Modifier.fillMaxWidth())
                                Text(s.status, style = MaterialTheme.typography.bodyMedium)
                                OutlinedButton(onClick = vm::stop, enabled = !s.stopping) {
                                    Icon(Icons.Outlined.Stop, null)
                                    Text(if (s.stopping) "Stopping…" else "Stop writing")
                                }
                            } else {
                                ActionButton("Keep this scene", vm::accept)
                                Row {
                                    TextButton(onClick = { vm.generate(story.draftAction) }) {
                                        Text("Try again")
                                    }
                                    TextButton(onClick = vm::discard) { Text("Discard draft") }
                                }
                            }
                        }
                    }
                }
            if (!s.busy && story.draft.isBlank()) {
                item {
                    Text(
                        if (story.scenes.isEmpty()) "Bring this scene to life"
                        else "What happens next?",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = s.action,
                        onValueChange = vm::action,
                        label = { Text("Your direction (optional)") },
                        placeholder = { Text("The key begins to glow…") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        minLines = 2,
                    )
                    Spacer(Modifier.height(16.dp))
                    ActionButton(
                        if (story.scenes.isEmpty()) "Write the opening"
                        else "Continue the adventure",
                        { vm.generate() },
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { vm.page("camera") },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    ) {
                        Icon(Icons.Outlined.PhotoCamera, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Show something new")
                    }
                }
                if (story.scenes.isNotEmpty())
                    item {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                        ) {
                            Column(Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            "A spark of magic",
                                            style = MaterialTheme.typography.titleMedium,
                                        )
                                        Text(
                                            "Shake gently to suggest a plot twist.",
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                    }
                                    Switch(checked = s.sensorEnabled, onCheckedChange = vm::sensors)
                                }
                                TextButton(
                                    onClick = {
                                        vm.action("A mysterious spark awakens. What happens next?")
                                    }
                                ) {
                                    Text("Add a spark instead")
                                }
                            }
                        }
                    }
            }
            item {
                Text(
                    "Gemma invents fiction. Review each draft before keeping it. Older scenes may fall outside the model’s context.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = onDelete, enabled = !s.busy) { Text("Delete adventure") }
            }
        }
    }
}

@Composable
private fun ScenePhoto(path: String) {
    val bitmap by
        produceState<android.graphics.Bitmap?>(null, path) {
            value = withContext(Dispatchers.IO) { android.graphics.BitmapFactory.decodeFile(path) }
        }
    bitmap?.let {
        Image(
            it.asImageBitmap(),
            "Your story inspiration",
            Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(20.dp)),
            contentScale = ContentScale.Fit,
        )
    }
}
