package com.androidengineers.pocketcards.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable
import com.androidengineers.pocketcards.data.*

@Serializable data object Library : NavKey
@Serializable data object Create : NavKey
@Serializable data object Editor : NavKey
@Serializable data class Study(val deckId: String) : NavKey

@Composable fun PocketCardsApp(vm: PocketCardsViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val stack = rememberNavBackStack(Library)
    var discard by remember { mutableStateOf(false) }
    var delete by remember { mutableStateOf<Deck?>(null) }
    val home: () -> Unit = { vm.cancel(); stack.clear(); stack.add(Library) }
    val back: () -> Unit = {
        if (stack.lastOrNull() == Editor && state.draft.isNotEmpty()) discard = true
        else { vm.cancel(); if (stack.size > 1) stack.removeAt(stack.lastIndex) }
    }
    LaunchedEffect(state.revision) {
        if (stack.lastOrNull() == Create && state.draft.isNotEmpty()) stack.add(Editor)
        else if (stack.lastOrNull() == Editor && state.draft.isEmpty() && state.revision > 0) home()
    }
    if (discard) AlertDialog(onDismissRequest = { discard = false }, title = { Text("Leave without saving?") },
        text = { Text("Your unsaved card edits will be discarded.") },
        confirmButton = { TextButton(onClick = { discard = false; vm.startNew(); home() }) { Text("Discard edits") } },
        dismissButton = { TextButton(onClick = { discard = false }) { Text("Keep editing") } })
    delete?.let { deck -> AlertDialog(onDismissRequest = { delete = null }, title = { Text("Delete this deck?") },
        text = { Text("“${deck.title}” will be removed from this device.") },
        confirmButton = { TextButton(onClick = { vm.delete(deck.id); delete = null }) { Text("Delete deck") } },
        dismissButton = { TextButton(onClick = { delete = null }) { Text("Keep deck") } }) }
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        NavDisplay(backStack = stack, onBack = { if (!state.saving) back() }, entryProvider = entryProvider {
            entry<Library> { LibraryScreen(state, onCreate = { vm.startNew(); stack.add(Create) },
                onStudy = { stack.add(Study(it.id)) }, onEdit = { vm.edit(it); stack.add(Editor) },
                onDelete = { delete = it }, onRetry = vm::reload) }
            entry<Create> { CreateScreen(state, vm.aiAvailable, vm::notes, vm::generate,
                onManual = { vm.manual(); stack.add(Editor) }, onCancel = vm::cancel, onBack = back) }
            entry<Editor> { EditorScreen(state, vm::title, vm::updateCard, vm::addCard, vm::removeCard, vm::save, back) }
            entry<Study> { key ->
                val deck = (state.decks + sampleDeck).find { it.id == key.deckId }
                if (deck != null) StudyScreen(deck, onBack = back)
                else Frame("Your deck", onBack = home) { item { Text(if (state.loading) "Loading your deck…" else "This deck is no longer available.") } }
            }
        })
    }
}

@Composable private fun Brand() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.AutoAwesome, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(21.dp))
        }
        Text("PocketCards", style = MaterialTheme.typography.titleLarge)
    }
}
@Composable private fun Tag(text: String, icon: ImageVector? = null) {
    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            if (icon != null) Icon(icon, null, Modifier.size(14.dp))
            Text(text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}
@Composable private fun ErrorNote(error: String?) {
    if (error != null) Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(16.dp)) {
        Text(error, Modifier.padding(16.dp).semantics { liveRegion = LiveRegionMode.Polite }, color = MaterialTheme.colorScheme.onErrorContainer)
    }
}
@Composable private fun DeckArt() {
    val scale = LocalDensity.current.fontScale.coerceAtLeast(1f)
    val cardWidth = (176 * scale).coerceAtMost(280f).dp
    val cardHeight = (110 * scale).dp
    Box(Modifier.height((148 * scale).dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(Modifier.size(cardWidth, cardHeight).rotate(-10f).background(Color(0xFFBDA2F0), RoundedCornerShape(18.dp)))
        Box(Modifier.size(cardWidth, cardHeight).rotate(6f).background(Color(0xFFD8C7FB), RoundedCornerShape(18.dp)))
        Surface(Modifier.size(cardWidth, cardHeight).rotate(-2f), shape = RoundedCornerShape(18.dp), color = Color(0xFFFFFDFC), shadowElevation = 3.dp) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
                Icon(Icons.Rounded.AutoAwesome, null, tint = Violet, modifier = Modifier.size(22.dp))
                Text("Small cards.\nBig discoveries.", color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 20.sp)
            }
        }
    }
}
@Composable private fun LibraryScreen(state: PocketState, onCreate: () -> Unit, onStudy: (Deck) -> Unit,
    onEdit: (Deck) -> Unit, onDelete: (Deck) -> Unit, onRetry: () -> Unit) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background, contentWindowInsets = WindowInsets.safeDrawing,
        floatingActionButton = { ExtendedFloatingActionButton(onClick = onCreate, modifier = Modifier.semantics { contentDescription = "Create deck" }, icon = { Icon(Icons.Rounded.Add, null) }, text = { Text("Create deck") }, containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary) }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding), contentAlignment = Alignment.TopCenter) {
            LazyColumn(Modifier.widthIn(max = 760.dp).fillMaxSize(), contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 108.dp), verticalArrangement = Arrangement.spacedBy(22.dp)) {
                item { Brand() }
                item {
                    Column {
                        Text("A little learning,", style = MaterialTheme.typography.displaySmall)
                        Text("always with you.", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(10.dp))
                        Text("Turn your notes into knowledge.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                item {
                    Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                        Column(Modifier.padding(20.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("YOUR NEXT SMALL STEP", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Icon(Icons.Rounded.School, null, tint = MaterialTheme.colorScheme.primary)
                            }
                            DeckArt()
                            Text("Meet your first five cards", style = MaterialTheme.typography.titleLarge)
                            Text("A quick introduction to the language of AI.", Modifier.padding(top = 5.dp, bottom = 14.dp), style = MaterialTheme.typography.bodyMedium)
                            Button(onClick = { onStudy(sampleDeck) }, modifier = Modifier.fillMaxWidth()) {
                                Text("Try a sample deck"); Spacer(Modifier.width(8.dp)); Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, Modifier.size(18.dp))
                            }
                            Text("Written by us · no account needed", Modifier.fillMaxWidth().padding(top = 8.dp), style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
                        }
                    }
                }
                item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Your library", style = MaterialTheme.typography.titleLarge); Tag("${state.decks.size} decks", Icons.Rounded.Layers)
                } }
                if (state.error != null) item { Column { ErrorNote(state.error); TextButton(onClick = onRetry) { Text("Retry loading") } } }
                if (state.loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
                else if (state.decks.isEmpty()) item {
                    Column(Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = .3f), RoundedCornerShape(20.dp)).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.Bookmarks, null, tint = MaterialTheme.colorScheme.primary)
                        Text("Make room for a new idea", Modifier.padding(top = 12.dp), style = MaterialTheme.typography.titleMedium)
                        Text("Your saved decks will live here.\nStart with notes, or write your own cards.", Modifier.padding(top = 6.dp), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                items(state.decks, key = { it.id }) { deck ->
                    Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface) {
                        Column(Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Style, null, Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
                                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                    Text(deck.title, style = MaterialTheme.typography.titleMedium)
                                    Text("${deck.cards.size} cards · saved on this device", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = { onDelete(deck) }) { Icon(Icons.Rounded.DeleteOutline, "Delete ${deck.title}") }
                            }
                            Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(onClick = { onStudy(deck) }, modifier = Modifier.weight(1f)) { Text("Study deck") }
                                OutlinedButton(onClick = { onEdit(deck) }) { Text("Edit") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun Frame(title: String, onBack: () -> Unit, busy: Boolean = false, content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background, contentWindowInsets = WindowInsets.safeDrawing) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding), contentAlignment = Alignment.TopCenter) {
            LazyColumn(Modifier.widthIn(max = 760.dp).fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                item { Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, enabled = !busy) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
                    Text(title, Modifier.padding(start = 8.dp), style = MaterialTheme.typography.titleLarge)
                } }
                content()
            }
        }
    }
}
@Composable private fun CreateScreen(state: PocketState, available: Boolean, onNotes: (String) -> Unit, onGenerate: () -> Unit,
    onManual: () -> Unit, onCancel: () -> Unit, onBack: () -> Unit) {
    Frame("A new deck", onBack) {
        item { Column { Tag("01 / CAPTURE AN IDEA", Icons.Rounded.AutoAwesome); Text("What are we\nlearning today?", Modifier.padding(top = 16.dp), style = MaterialTheme.typography.headlineLarge); Text("Paste your notes. We'll help you turn them into bite-sized questions.", Modifier.padding(top = 10.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        item { OutlinedTextField(value = state.notes, onValueChange = onNotes, enabled = !state.generating, label = { Text("Your study notes") }, placeholder = { Text("A prompt is the input you give an AI model…") }, minLines = 7, maxLines = 12, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), supportingText = { Text("${state.notes.length} / ${CardValidation.MAX_NOTES} characters · minimum 40") }) }
        item { Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceVariant) { Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Rounded.PrivacyTip, null, tint = MaterialTheme.colorScheme.primary)
            Text(if (available) "Generating sends these notes to Google's cloud AI through Firebase. Use study material, not private information. Always review the result." else "AI generation isn't connected in this build. You can write cards yourself or explore the sample deck. Setup instructions are in the project README.", style = MaterialTheme.typography.bodyMedium)
        } } }
        item { ErrorNote(state.error) }
        item { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (state.generating) { LinearProgressIndicator(Modifier.fillMaxWidth()); Text("Finding the ideas worth remembering…", Modifier.semantics { liveRegion = LiveRegionMode.Polite }); OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Cancel generation") } }
            else {
                Button(onClick = onGenerate, enabled = available && state.notes.trim().length >= 40, modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp)) { Icon(Icons.Rounded.AutoAwesome, null); Spacer(Modifier.width(10.dp)); Text("Generate flashcards") }
                OutlinedButton(onClick = onManual, modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp)) { Text("Write my own cards") }
            }
        } }
    }
}
@Composable private fun EditorScreen(state: PocketState, onTitle: (String) -> Unit, onCard: (Int, String, String) -> Unit,
    onAdd: () -> Unit, onRemove: (Int) -> Unit, onSave: () -> Unit, onBack: () -> Unit) {
    Frame("Make it yours", onBack, state.saving) {
        item { Column { Tag("02 / REVIEW & REFINE", Icons.Rounded.EditNote); Text("Good cards start\nwith a little care.", Modifier.padding(top = 16.dp), style = MaterialTheme.typography.headlineLarge); Text("Check every answer. Make it sound like you.", Modifier.padding(top = 10.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        item { OutlinedTextField(state.title, onTitle, label = { Text("Deck title") }, singleLine = true, enabled = !state.saving, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) }
        if (state.draft.isEmpty()) item { Text("There are no unsaved cards here. Add a card to start. Unsaved drafts are not kept after the app process closes.") }
        items(state.draft.size) { index ->
            val card = state.draft[index]
            Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Tag("CARD ${index + 1}")
                        IconButton(onClick = { onRemove(index) }, enabled = !state.saving) { Icon(Icons.Rounded.Close, "Remove card ${index + 1}") }
                    }
                    OutlinedTextField(card.question, { onCard(index, it, card.answer) }, label = { Text("Question ${index + 1}") }, enabled = !state.saving, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp))
                    OutlinedTextField(card.answer, { onCard(index, card.question, it) }, label = { Text("Answer ${index + 1}") }, enabled = !state.saving, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), minLines = 2)
                }
            }
        }
        item { OutlinedButton(onClick = onAdd, enabled = state.draft.size < 10 && !state.saving, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Rounded.Add, null); Text("Add a card") } }
        item { ErrorNote(state.error) }
        item { Button(onClick = onSave, enabled = !state.saving && state.draft.isNotEmpty(), modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp)) { Text(if (state.saving) "Saving…" else "Save deck") } }
    }
}
@Composable private fun StudyScreen(deck: Deck, onBack: () -> Unit) {
    var index by rememberSaveable(deck.id) { mutableIntStateOf(0) }
    var revealed by rememberSaveable(deck.id) { mutableStateOf(false) }
    var known by rememberSaveable(deck.id) { mutableIntStateOf(0) }
    val finished = index >= deck.cards.size
    Frame(if (finished) "A little wiser" else "Time to discover", onBack) {
        item { Text(deck.title, style = MaterialTheme.typography.headlineMedium); Text(deck.origin, Modifier.padding(top = 6.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item { LinearProgressIndicator(progress = { (index.toFloat() / deck.cards.size).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape)) }
        if (finished) {
            item { Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    Icon(Icons.Rounded.Celebration, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                    Text("Look at you grow.", style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
                    Text("$known of ${deck.cards.size} cards felt familiar.", textAlign = TextAlign.Center)
                    Text(if (known == deck.cards.size) "A small step, well taken." else "${deck.cards.size - known} could use another look. Learning takes practice.", textAlign = TextAlign.Center)
                }
            } }
            item { Button(onClick = { index = 0; known = 0; revealed = false }, modifier = Modifier.fillMaxWidth()) { Text("Study again") }; TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back to library") } }
        } else {
            val card = deck.cards[index]
            item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Tag("CARD ${index + 1} OF ${deck.cards.size}"); Text(if (revealed) "THE ANSWER" else "A MOMENT TO THINK", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 8.dp)) } }
            item {
                Surface(shape = RoundedCornerShape(28.dp), color = if (revealed) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.fillMaxWidth().animateContentSize()) {
                    Column(Modifier.padding(28.dp).heightIn(min = 250.dp), verticalArrangement = Arrangement.SpaceBetween) {
                        Icon(if (revealed) Icons.Rounded.Lightbulb else Icons.Rounded.AutoAwesome, null, Modifier.size(28.dp))
                        AnimatedContent(targetState = revealed, label = "cardAnswer") { answer -> Text(if (answer) card.answer else card.question, style = if (answer) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(vertical = 28.dp).semantics { liveRegion = LiveRegionMode.Polite }) }
                        Text(if (revealed) "Understanding beats memorizing." else "Take your time. There's no timer here.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            item {
                if (!revealed) Button(onClick = { revealed = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp)) { Icon(Icons.Rounded.Visibility, null); Spacer(Modifier.width(10.dp)); Text("Reveal answer") }
                else Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { index++; revealed = false }, modifier = Modifier.weight(1f).heightIn(min = 54.dp)) { Text("Still learning") }
                    Button(onClick = { known++; index++; revealed = false }, modifier = Modifier.weight(1f).heightIn(min = 54.dp)) { Icon(Icons.Rounded.Check, null, Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("Got it") }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 915)
@Composable private fun LibraryPreview() { PocketTheme { LibraryScreen(PocketState(loading = false), {}, {}, {}, {}, {}) } }
@Preview(showBackground = true, widthDp = 412, heightDp = 915, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable private fun StudyPreview() { PocketTheme { StudyScreen(sampleDeck, {}) } }
