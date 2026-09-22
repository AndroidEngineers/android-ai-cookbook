package com.androidengineers.pocketcards.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidengineers.pocketcards.data.Deck
import java.time.LocalDate

@Composable internal fun StudyScreen(deck: Deck, onBack: () -> Unit, busy: Boolean = false, error: String? = null,
    onReview: (Int, Boolean?, () -> Unit) -> Unit = { _, _, done -> done() }) {
    var filter by rememberSaveable(deck.id) { mutableStateOf("All") }
    var round by rememberSaveable(deck.id) { mutableIntStateOf(0) }
    val today = LocalDate.now().toEpochDay()
    // A rating must not remove an item from the active session's queue.
    val queue = rememberSaveable(deck.id, filter, round) {
        deck.cards.indices.filter { i -> when (filter) {
            "Due" -> deck.cards[i].dueDay <= today
            "Favorites" -> deck.cards[i].favorite
            else -> true
        } }.toIntArray()
    }
    var position by rememberSaveable(deck.id, filter, round) { mutableIntStateOf(0) }
    var revealed by rememberSaveable(deck.id, filter, round) { mutableStateOf(false) }
    // Capture a safe snapshot outside deferred layout lambdas, including on the last card.
    val cardIndex = queue.getOrNull(position)
    val card = cardIndex?.let { deck.cards.getOrNull(it) }
    val reviewed = deck.cards.count { it.reviewedDay == today }
    val dark = isSystemInDarkTheme()
    val background = if (dark) Color(0xFF1D142B) else Color(0xFFF7F0FF)
    val foreground = if (dark) Color(0xFFF1E8FF) else Color(0xFF402467)
    val purple = if (dark) Color(0xFFC8A8FF) else Color(0xFF7523E8)
    val cardColor = if (dark) Color(0xFF322044) else Color(0xFFF9F3FF)
    val scroll = rememberScrollState()
    LaunchedEffect(position, filter, round) { scroll.scrollTo(0) }
    Surface(color = background, contentColor = foreground) {
        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding(), contentAlignment = Alignment.TopCenter) {
            val cardHeight = (maxHeight * .46f).coerceIn(260.dp, 420.dp)
            Column(Modifier.widthIn(max = 480.dp).fillMaxHeight().verticalScroll(scroll)
                .padding(horizontal = 24.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.AutoAwesome, null, Modifier.size(46.dp), tint = purple)
                    Text("Study smarter\nwith AI", Modifier.weight(1f).padding(start = 14.dp), fontSize = 24.sp,
                        lineHeight = 28.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onBack, enabled = !busy) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back to library") }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("All", "Due", "Favorites").forEach { tab ->
                        val selected = tab == filter
                        Surface(onClick = { filter = tab }, enabled = !busy, shape = RoundedCornerShape(16.dp),
                            color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else foreground,
                            modifier = Modifier.weight(1f).semantics { this.selected = selected; role = Role.Tab }) {
                            Box(Modifier.heightIn(min = 48.dp).padding(8.dp), contentAlignment = Alignment.Center) {
                                Text(tab, fontSize = 14.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
                            }
                        }
                    }
                }
                if (card != null && cardIndex != null) {
                    Box(Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 12.dp), contentAlignment = Alignment.Center) {
                        Box(Modifier.matchParentSize().padding(horizontal = 12.dp).offset(y = (-12).dp).rotate(-7f)
                            .background(Color(0xFFD7B9FF), RoundedCornerShape(28.dp)))
                        Box(Modifier.matchParentSize().padding(horizontal = 5.dp).offset(y = (-3).dp).rotate(-3f)
                            .background(Color(0xFFC699FF), RoundedCornerShape(28.dp)))
                        Surface(shape = RoundedCornerShape(28.dp), color = Color(0xFFB98AF6), shadowElevation = 8.dp) {
                            Column(Modifier.fillMaxWidth().padding(16.dp).background(cardColor, RoundedCornerShape(22.dp))
                                .border(1.dp, Color.White.copy(alpha = .7f), RoundedCornerShape(22.dp))
                                .clickable(enabled = !busy, onClickLabel = if (revealed) "Show question" else "Reveal answer") { revealed = !revealed }
                                .padding(20.dp).heightIn(min = cardHeight), verticalArrangement = Arrangement.SpaceBetween) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(if (revealed) "ANSWER" else "QUESTION", Modifier.weight(1f), fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp, color = foreground.copy(alpha = .65f))
                                    IconButton(onClick = { onReview(cardIndex, null) {} }, enabled = !busy) {
                                        Icon(if (card.favorite) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                            if (card.favorite) "Remove favorite" else "Add favorite", tint = purple)
                                    }
                                }
                                Text(if (revealed) card.answer else card.question, Modifier.padding(vertical = 16.dp)
                                    .semantics { liveRegion = LiveRegionMode.Polite }, fontSize = 24.sp, lineHeight = 33.sp,
                                    fontWeight = FontWeight.Medium, color = foreground)
                                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                    TextButton(onClick = { revealed = !revealed }, enabled = !busy) {
                                        Text(if (revealed) "Show question" else "Reveal answer", color = purple, fontSize = 13.sp)
                                    }
                                    Text("${position + 1} / ${queue.size}", fontWeight = FontWeight.Bold, color = foreground)
                                }
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        val rate: (Boolean) -> Unit = { gotIt ->
                            if (!busy && revealed) onReview(cardIndex, gotIt) {
                                if (queue.getOrNull(position) == cardIndex) {
                                    position = (position + 1).coerceAtMost(queue.size)
                                    revealed = false
                                }
                            }
                        }
                        Button(onClick = { rate(false) }, enabled = revealed && !busy,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFE0E7), contentColor = Color(0xFFA73455)),
                            shape = RoundedCornerShape(22.dp), modifier = Modifier.weight(1f).heightIn(min = 58.dp)) {
                            Icon(Icons.Rounded.Refresh, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text("Again")
                        }
                        Button(onClick = { rate(true) }, enabled = revealed && !busy,
                            colors = ButtonDefaults.buttonColors(containerColor = purple, contentColor = if (dark) Color(0xFF301050) else Color.White),
                            shape = RoundedCornerShape(22.dp), modifier = Modifier.weight(1f).heightIn(min = 58.dp)) {
                            Icon(Icons.Rounded.Check, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text("Got it")
                        }
                    }
                } else {
                    Surface(shape = RoundedCornerShape(28.dp), color = cardColor) {
                        Column(Modifier.fillMaxWidth().heightIn(min = cardHeight).padding(28.dp),
                            verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(if (queue.isEmpty()) Icons.Rounded.Style else Icons.Rounded.CheckCircle, null, Modifier.size(64.dp), tint = purple)
                            Text(if (queue.isEmpty()) "No ${filter.lowercase()} cards" else "Deck complete!", Modifier.padding(vertical = 20.dp), fontSize = 26.sp, fontWeight = FontWeight.Bold)
                            Text(if (filter == "Favorites" && queue.isEmpty()) "Star a card in All to save it here."
                                else "Got it schedules tomorrow. Again keeps a card due today.")
                            Button(onClick = { round++ }, Modifier.padding(top = 20.dp)) { Text("Study again") }
                        }
                    }
                }
                if (error != null) Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Daily progress", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("$reviewed / ${deck.cards.size}", fontSize = 14.sp)
                    }
                    LinearProgressIndicator(progress = { if (deck.cards.isEmpty()) 0f else reviewed.toFloat() / deck.cards.size },
                        modifier = Modifier.fillMaxWidth().height(8.dp), color = purple, trackColor = if (dark) Color(0xFF493459) else Color(0xFFE5D9F1), drawStopIndicator = {})
                    Text(deck.title, color = foreground.copy(alpha = .65f), fontSize = 12.sp)
                }
            }
        }
    }
}
