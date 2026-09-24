package com.androidengineers.pocketcommunity.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.material3.a2ui.A2uiSurface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidengineers.pocketcommunity.data.validEndpoint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(vm: CommunityViewModel, openUrl: (String) -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val surfaces by vm.surfaces.collectAsStateWithLifecycle()
    var prompt by rememberSaveable { mutableStateOf("") }
    var inspect by rememberSaveable { mutableStateOf(false) }
    var settings by rememberSaveable { mutableStateOf(false) }
    var endpoint by rememberSaveable { mutableStateOf("") }
    val scroll = rememberLazyListState()
    LaunchedEffect(state.items.size) {
        if (state.items.isNotEmpty()) scroll.animateScrollToItem(state.items.lastIndex + 1)
    }
    LaunchedEffect(state.pendingUrl) {
        state.pendingUrl?.let {
            openUrl(it)
            vm.consumeUrl()
        }
    }
    Scaffold(
        topBar = {
            Column(
                Modifier.statusBarsPadding()
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "◉",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("PocketCommunity", fontWeight = FontWeight.Bold)
                        Text("Discover with DevEarth", style = MaterialTheme.typography.labelSmall)
                    }
                    TextButton(onClick = { inspect = true }) { Text("Inspect") }
                    TextButton(
                        onClick = {
                            endpoint = state.endpoint
                            settings = true
                        }
                    ) {
                        Text("Setup")
                    }
                }
                if (state.items.isNotEmpty())
                    TextButton(onClick = { vm.reset() }) { Text("New conversation") }
                Text(
                    if (state.endpoint.isBlank()) "CONNECT YOUR AGENT TO BEGIN"
                    else "GEMINI AGENT · DEVEARTH",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        bottomBar = {
            Column(
                Modifier.navigationBarsPadding()
                    .imePadding()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        prompt,
                        { prompt = it.take(2000) },
                        placeholder = { Text("Ask about your next event…") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(28.dp),
                        maxLines = 3,
                        enabled = state.endpoint.isNotBlank(),
                    )
                    FilledTonalButton(
                        onClick = {
                            vm.send(prompt)
                            prompt = ""
                        },
                        enabled = prompt.isNotBlank() && !state.busy && state.endpoint.isNotBlank(),
                        contentPadding = PaddingValues(12.dp),
                    ) {
                        Text("Send")
                    }
                }
            }
        },
    ) { padding ->
        LazyColumn(
            state = scroll,
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                if (state.items.isEmpty())
                    Column(Modifier.padding(top = 32.dp, bottom = 20.dp)) {
                        Text(
                            "Find your people.",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "A conversation that opens doors.",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.height(18.dp))
                        Text(
                            "Explore real developer events, find the venue, and get ready—all in one conversation."
                        )
                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = {
                                if (state.endpoint.isBlank()) {
                                    endpoint = state.endpoint
                                    settings = true
                                } else vm.send("Find Android events in Bengaluru")
                            },
                            enabled = !state.busy,
                        ) {
                            Text(
                                if (state.endpoint.isBlank()) "Connect Gemini agent"
                                else "Find Android events"
                            )
                        }
                        TextButton(
                            onClick = { vm.send("Show all upcoming events") },
                            enabled = !state.busy && state.endpoint.isNotBlank(),
                        ) {
                            Text("Explore all communities")
                        }
                    }
            }
            items(state.items, key = { it.id }) { item ->
                if (item.surfaceId != null) {
                    val surface = surfaces.find { it.id == item.surfaceId }
                    if (surface != null)
                        A2uiSurface(surfaceModel = surface, modifier = Modifier.fillMaxWidth())
                    else LinearProgressIndicator(Modifier.fillMaxWidth())
                } else
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            if (item.user) Arrangement.End else Arrangement.Start,
                    ) {
                        Surface(
                            color =
                                if (item.user) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.widthIn(max = 320.dp),
                        ) {
                            Text(
                                item.text,
                                Modifier.padding(16.dp),
                                color =
                                    if (item.user) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
            }
            if (state.busy)
                item {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                    Text(
                        "Finding the next useful response…",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            state.error?.let { error ->
                item {
                    Card {
                        Column(Modifier.padding(16.dp)) {
                            Text(error, color = MaterialTheme.colorScheme.error)
                            if (state.canRetry)
                                TextButton(onClick = vm::retry, enabled = !state.busy) {
                                    Text("Retry")
                                }
                            TextButton(onClick = vm::dismissError) { Text("Dismiss") }
                        }
                    }
                }
            }
        }
    }
    if (inspect)
        ModalBottomSheet(onDismissRequest = { inspect = false }) {
            Text(
                "A2UI inspector",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(20.dp),
            )
            Text(
                "Gemini-produced UI messages and outgoing actions",
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            SelectionContainer {
                LazyColumn(
                    Modifier.fillMaxWidth().heightIn(max = 500.dp),
                    contentPadding = PaddingValues(20.dp),
                ) {
                    items(state.trace) {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    }
                }
            }
        }
    if (settings)
        AlertDialog(
            onDismissRequest = { settings = false },
            title = { Text("Connect your agent") },
            text = {
                Column {
                    Text(
                        "Connect the companion server to generate UI with Gemini. The key stays on the server. For a phone connected through ADB reverse, use http://127.0.0.1:8787/chat in debug builds. Production endpoints must use HTTPS."
                    )
                    OutlinedTextField(
                        endpoint,
                        { endpoint = it },
                        label = { Text("Agent endpoint") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.configure(endpoint)
                        settings = false
                    },
                    enabled = validEndpoint(endpoint) && !state.busy,
                ) {
                    Text("Connect")
                }
            },
            dismissButton = { TextButton(onClick = { settings = false }) { Text("Cancel") } },
        )
}
