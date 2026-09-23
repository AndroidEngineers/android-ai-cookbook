package com.androidengineers.pocketchat.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.automirrored.rounded.CompareArrows
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidengineers.pocketchat.BuildConfig
import com.androidengineers.pocketchat.ChatViewModel
import com.androidengineers.pocketchat.data.*
import kotlinx.coroutines.launch

private val Blue = Color(0xFF315DFF)
private val Light = lightColorScheme(secondaryContainer=Color(0xFFDCE5FF),onSecondaryContainer=Blue,primary=Blue, onPrimary=Color.White, background=Color(0xFFF6F8FF), surface=Color.White, onSurface=Color(0xFF131C3B), onBackground=Color(0xFF131C3B), surfaceVariant=Color(0xFFE9EEFA), onSurfaceVariant=Color(0xFF53617C), primaryContainer=Color(0xFFDCE5FF), onPrimaryContainer=Color(0xFF131C3B))
private val Dark = darkColorScheme(secondaryContainer=Color(0xFF253C70),onSecondaryContainer=Color(0xFFB6C4FF),primary=Color(0xFFB6C4FF), onPrimary=Color(0xFF002A78), background=Color(0xFF10131D), surface=Color(0xFF1B2030), onSurface=Color(0xFFE5E9FA), onBackground=Color(0xFFE5E9FA), surfaceVariant=Color(0xFF252C40), onSurfaceVariant=Color(0xFFB5BED6), primaryContainer=Color(0xFF253C70), onPrimaryContainer=Color(0xFFE5E9FA))

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun PocketChatApp(vm: ChatViewModel) {
    var theme by rememberSaveable { mutableIntStateOf(0) }
    MaterialTheme(colorScheme = if(theme == 2 || theme == 0 && isSystemInDarkTheme()) Dark else Light) {
        val repo = vm.repository
        val library by repo.library.collectAsStateWithLifecycle()
        val ready by repo.ready.collectAsStateWithLifecycle()
        val selected by vm.selected.collectAsStateWithLifecycle()
        val draft by vm.draft.collectAsStateWithLifecycle()
        val active by repo.active.collectAsStateWithLifecycle()
        val configured by repo.configured.collectAsStateWithLifecycle()
        val notice by repo.notice.collectAsStateWithLifecycle()
        var saved by rememberSaveable { mutableStateOf(false) }
        var settings by remember { mutableStateOf(false) }
        var rename by remember { mutableStateOf(false) }
        var delete by remember { mutableStateOf(false) }
        val conversation = library.conversations.find { it.id == selected }
        val snackbar = remember { SnackbarHostState() }
        LaunchedEffect(notice) { notice?.let { snackbar.showSnackbar(it); repo.dismissNotice() } }
        BackHandler(conversation != null) { vm.open(null) }
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbar) },
            topBar = {
                TopAppBar(title = { Text(conversation?.title ?: "PocketChat", maxLines=1, overflow=TextOverflow.Ellipsis, fontWeight=FontWeight.Bold, color=if(conversation == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) },
                    navigationIcon = { if(conversation != null) IconButton(onClick={ vm.open(null) }) { Icon(Icons.AutoMirrored.Rounded.ArrowBack,"Back to chats") } },
                    colors=TopAppBarDefaults.topAppBarColors(containerColor=MaterialTheme.colorScheme.background),
                    actions = {
                        if(conversation != null) {
                            var menu by remember { mutableStateOf(false) }
                            IconButton(onClick={ menu=true }) { Icon(Icons.Rounded.MoreVert,"Conversation options") }
                            DropdownMenu(expanded=menu,onDismissRequest={menu=false}) {
                                DropdownMenuItem(text={Text("Rename")},onClick={menu=false;rename=true})
                                DropdownMenuItem(text={Text("Delete")},onClick={menu=false;delete=true})
                            }
                        }
                        IconButton(onClick={settings=true}) { Icon(Icons.Rounded.Settings,"Settings") }
                    })
            },
            bottomBar = { if(conversation != null) ChatComposer(draft, vm::draft, vm::send, active == conversation.id, repo::stop) else NavigationBar(containerColor=MaterialTheme.colorScheme.surface) {
                NavigationBarItem(selected=!saved,onClick={saved=false},icon={Icon(Icons.AutoMirrored.Rounded.Chat,null)},label={Text("Chats")})
                NavigationBarItem(selected=saved,onClick={saved=true},icon={Icon(Icons.Rounded.BookmarkBorder,null)},label={Text("Saved")})
            } }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) {
                if(!ready) Column(Modifier.align(Alignment.Center).padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally) { CircularProgressIndicator(); Text(notice ?: "Opening your conversations…",Modifier.padding(top=16.dp)) }
                else if(conversation != null) {
                    BoxWithConstraints(Modifier.fillMaxSize()) {
                        if(maxWidth >= 840.dp) Row(Modifier.fillMaxSize()) {
                            Column(Modifier.width(280.dp).fillMaxHeight().padding(16.dp)) {
                                FilledTonalButton(onClick={vm.newChat()},modifier=Modifier.fillMaxWidth()) { Text("New chat") }
                                LazyColumn { items(library.conversations,key={it.id}) { c -> ConversationRow(c,onClick={vm.open(c.id)}) } }
                            }
                            VerticalDivider()
                            ChatScreen(conversation,active==conversation.id,{repo.send(conversation.id,it,true)},{repo.saveAnswer(conversation,it)},configured,{settings=true},Modifier.weight(1f))
                        } else ChatScreen(conversation,active==conversation.id,{repo.send(conversation.id,it,true)},{repo.saveAnswer(conversation,it)},configured,{settings=true})
                    }
                } else if(saved) SavedScreen(library, { vm.open(it) }, repo::removeSaved)
                else HomeScreen(library.conversations,{vm.newChat(it)},{vm.open(it)})
            }
        }
        if(settings) SettingsDialog(configured,theme,{theme=it},{key,model->repo.configure(key,model)},{repo.clear();vm.open(null)},{settings=false})
        if(rename && conversation != null) {
            var title by remember { mutableStateOf(conversation.title) }
            AlertDialog(onDismissRequest={rename=false},title={Text("Rename conversation")},text={OutlinedTextField(title,{title=it.take(80)},label={Text("Title")})},confirmButton={TextButton(onClick={repo.rename(conversation.id,title);rename=false},enabled=title.isNotBlank()){Text("Save")}},dismissButton={TextButton(onClick={rename=false}){Text("Cancel")}})
        }
        if(delete && conversation != null) AlertDialog(onDismissRequest={delete=false},title={Text("Delete this conversation?")},text={Text("Local messages will be removed. Saved answers remain. This does not delete provider-side records.")},confirmButton={TextButton(onClick={repo.delete(conversation.id);vm.open(null);delete=false}){Text("Delete")}},dismissButton={TextButton(onClick={delete=false}){Text("Cancel")}})
    }
}

@Composable private fun HomeScreen(conversations: List<Conversation>, newChat: (String)->Unit, open: (String)->Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
        item { Text("Explore ideas\ntogether",fontSize=34.sp,lineHeight=38.sp,fontWeight=FontWeight.Bold); Text("Your Android Engineers AI learning companion.",color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.padding(top=8.dp)) }
        item { Hero { newChat("") } }
        item { Row(horizontalArrangement=Arrangement.spacedBy(8.dp),modifier=Modifier.fillMaxWidth()) {
            PromptCard("Explain","Make it simple",Icons.Rounded.Lightbulb,Modifier.weight(1f)) { newChat("Explain Kotlin coroutines in simple terms.") }
            PromptCard("Learn","Find your path",Icons.Rounded.AutoAwesome,Modifier.weight(1f)) { newChat("Help me choose an Android Engineers roadmap or codelab. Ask about my Android experience and what I want to build.") }
            PromptCard("Mentorship","Get guidance",Icons.AutoMirrored.Rounded.CompareArrows,Modifier.weight(1f)) { newChat("What is Android Engineers 1:1 mentorship, and when would it help with my Android career or interviews?") }
        } }
        item { AndroidEngineersResources() }
        item { Text("Recent conversations",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold) }
        if(conversations.isEmpty()) item { EmptyState(Icons.AutoMirrored.Rounded.Chat,"Your next idea starts here","Ask a question. Your conversations will stay on this device.") }
        else {
            item { OutlinedTextField(query,{query=it},modifier=Modifier.fillMaxWidth(),placeholder={Text("Search conversations")},leadingIcon={Icon(Icons.Rounded.Search,null)},singleLine=true,shape=RoundedCornerShape(20.dp)) }
            val filtered = conversations.filter { it.title.contains(query,true) || it.turns.any { t->t.prompt.contains(query,true) || t.answer.contains(query,true) } }
            if(filtered.isEmpty()) item { Text("No conversations match your search.") }
            items(filtered,key={it.id}) { c -> ConversationRow(c){open(c.id)} }
        }
    }
}
@Composable private fun Hero(onClick:()->Unit) {
    val a=MaterialTheme.colorScheme.primaryContainer; val b=MaterialTheme.colorScheme.surfaceVariant
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Brush.linearGradient(listOf(a,b))).padding(24.dp)) {
        Canvas(Modifier.fillMaxWidth().height(96.dp)) {
            drawRoundRect(Blue.copy(alpha=.14f),Offset(size.width*.38f,18f),Size(125f,100f),CornerRadius(35f))
            drawRoundRect(Blue,Offset(size.width*.25f,0f),Size(130f,95f),CornerRadius(35f))
            drawCircle(Color.White,6f,Offset(size.width*.25f+35f,45f)); drawCircle(Color.White,6f,Offset(size.width*.25f+65f,45f)); drawCircle(Color.White,6f,Offset(size.width*.25f+95f,45f))
        }
        Text("Start with a question",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)
        Text("Make room for a new perspective.",modifier=Modifier.padding(top=6.dp,bottom=16.dp),color=MaterialTheme.colorScheme.onSurfaceVariant)
        Button(onClick=onClick) { Text("New chat"); Spacer(Modifier.width(8.dp)); Icon(Icons.Rounded.Add,null) }
    }
}
@Composable private fun PromptCard(title:String,description:String,icon:ImageVector,modifier:Modifier,onClick:()->Unit) {
    Surface(onClick=onClick,shape=RoundedCornerShape(20.dp),color=MaterialTheme.colorScheme.surface,modifier=modifier) {
        Column(Modifier.padding(horizontal=8.dp,vertical=16.dp),horizontalAlignment=Alignment.CenterHorizontally) { Icon(icon,null,tint=MaterialTheme.colorScheme.primary); Text(title,fontWeight=FontWeight.SemiBold,modifier=Modifier.padding(top=8.dp)); Text(description,fontSize=11.sp,color=MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}
@Composable private fun ConversationRow(c:Conversation,onClick:()->Unit) {
    Surface(onClick=onClick,shape=RoundedCornerShape(18.dp),color=MaterialTheme.colorScheme.surface,modifier=Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            Icon(Icons.AutoMirrored.Rounded.Chat,null,tint=MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f)) { Text(c.title,fontWeight=FontWeight.SemiBold,maxLines=1,overflow=TextOverflow.Ellipsis); Text(c.turns.lastOrNull()?.prompt ?: "Ready for your first question",maxLines=1,overflow=TextOverflow.Ellipsis,color=MaterialTheme.colorScheme.onSurfaceVariant,fontSize=13.sp) }
            Icon(Icons.Rounded.ChevronRight,null)
        }
    }
}
@Composable private fun EmptyState(icon:ImageVector,title:String,body:String) {
    Column(Modifier.fillMaxWidth().padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally) { Icon(icon,null,tint=MaterialTheme.colorScheme.primary,modifier=Modifier.size(36.dp)); Text(title,fontWeight=FontWeight.SemiBold,modifier=Modifier.padding(top=16.dp)); Text(body,color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.padding(top=8.dp)) }
}

@Composable private fun ChatScreen(c:Conversation,active:Boolean,retry:(String)->Unit,save:(Turn)->Unit,configured:Boolean,settings:()->Unit,modifier:Modifier=Modifier) {
    val list=rememberLazyListState(); val scope=rememberCoroutineScope()
    val nearBottom by remember { derivedStateOf { !list.canScrollForward } }
    LaunchedEffect(c.id) { if(c.turns.isNotEmpty()) list.scrollToItem(c.turns.lastIndex) }
    LaunchedEffect(c.turns.lastOrNull()?.answer?.length,c.turns.size) { if(nearBottom && c.turns.isNotEmpty()) list.scrollToItem(c.turns.lastIndex,Int.MAX_VALUE) }
    Column(modifier.fillMaxSize()) {
        if(!configured) Surface(color=MaterialTheme.colorScheme.primaryContainer,onClick=settings,modifier=Modifier.fillMaxWidth()) { Text(if(BuildConfig.DEBUG) "Add your Gemini key to start chatting →" else "Cloud chat needs a production backend",Modifier.padding(horizontal=20.dp,vertical=12.dp),style=MaterialTheme.typography.bodySmall) }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            if(c.turns.isEmpty()) Column(Modifier.align(Alignment.Center).widthIn(max=500.dp)) { EmptyState(Icons.Rounded.AutoAwesome,"Hi, I’m your Android Engineers AI guide","Ask about Kotlin, Compose, Android + AI, or your next learning step. I can help you explore our roadmaps, codelabs, courses, and optional 1:1 mentorship. I’m an AI assistant, not a human mentor.") }
            LazyColumn(state=list,modifier=Modifier.fillMaxSize(),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(24.dp)) {
                items(c.turns,key={it.id}) { turn ->
                    Column(Modifier.fillMaxWidth().widthIn(max=760.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
                        Surface(color=MaterialTheme.colorScheme.primaryContainer,shape=RoundedCornerShape(22.dp),modifier=Modifier.align(Alignment.End).widthIn(max=560.dp)) { SelectionContainer { Text(turn.prompt,Modifier.padding(16.dp),lineHeight=24.sp) } }
                        Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Rounded.AutoAwesome,null,tint=MaterialTheme.colorScheme.primary,modifier=Modifier.padding(top=14.dp).size(22.dp))
                            Column(Modifier.weight(1f)) {
                                Surface(shape=RoundedCornerShape(20.dp),color=MaterialTheme.colorScheme.surface) {
                                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                                        if(turn.answer.isNotBlank()) MarkdownAnswer(turn.answer)
                                        if(turn.status == ReplyStatus.COMPLETE) ResourceLinks(AndroidEngineersGuide.mentionedResources(turn.answer))
                                        if(turn.status == ReplyStatus.STREAMING) { Spacer(Modifier.height(8.dp)); LinearProgressIndicator(modifier=Modifier.width(56.dp)); Text(if(turn.answer.isBlank()) "Thinking…" else "Writing…",style=MaterialTheme.typography.labelSmall,modifier=Modifier.padding(top=8.dp)) }
                                        if(turn.status !in listOf(ReplyStatus.COMPLETE,ReplyStatus.STREAMING)) Text(turn.error ?: if(turn.status==ReplyStatus.STOPPED) "Stopped" else "Interrupted when the app closed",color=MaterialTheme.colorScheme.error,modifier=Modifier.padding(top=8.dp))
                                    }
                                }
                                Row {
                                    if(turn.answer.isNotBlank()) CopyButton(turn.answer)
                                    if(turn.status==ReplyStatus.COMPLETE) TextButton(onClick={save(turn)}) { Icon(Icons.Rounded.BookmarkBorder,null,Modifier.size(18.dp)); Text(" Save") }
                                    else if(!active && turn.id == c.turns.lastOrNull()?.id) TextButton(onClick={retry(turn.prompt)}) { Icon(Icons.Rounded.Refresh,null,Modifier.size(18.dp)); Text(" Retry") }
                                }
                            }
                        }
                    }
                }
            }
            if(!nearBottom && c.turns.isNotEmpty()) FilledTonalButton(onClick={scope.launch{list.animateScrollToItem(c.turns.lastIndex,Int.MAX_VALUE)}},modifier=Modifier.align(Alignment.BottomCenter)) { Text("Jump to latest") }
        }
    }
}

@Composable private fun ChatComposer(draft:String,onDraft:(String)->Unit,send:()->Unit,active:Boolean,stop:()->Unit) {
    Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).navigationBarsPadding().imePadding()) {
        if(active) OutlinedButton(onClick=stop,modifier=Modifier.align(Alignment.CenterHorizontally)) { Icon(Icons.Rounded.Stop,null,Modifier.size(18.dp)); Text(" Stop generating") }
        Row(Modifier.fillMaxWidth().padding(12.dp),verticalAlignment=Alignment.Bottom,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value=draft,onValueChange=onDraft,placeholder={Text("Ask a follow-up…")},modifier=Modifier.weight(1f),maxLines=5,shape=RoundedCornerShape(24.dp))
            FilledIconButton(onClick=send,enabled=draft.isNotBlank()&&!active,modifier=Modifier.size(52.dp)) { Icon(Icons.AutoMirrored.Rounded.Send,"Send message") }
        }
    }
}

@Composable private fun MarkdownAnswer(text:String) {
    // A deliberately small renderer: fenced code, headings, lists, and bold inline text.
    val segments=text.split("```")
    Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
        segments.forEachIndexed { index,segment ->
            if(index%2==1) {
                val code=segment.substringAfter('\n',segment)
                Surface(color=MaterialTheme.colorScheme.surfaceVariant,shape=RoundedCornerShape(12.dp)) { Column(Modifier.padding(12.dp)) { CopyButton(code); SelectionContainer { Text(code,fontFamily=FontFamily.Monospace,fontSize=13.sp,modifier=Modifier.horizontalScroll(rememberScrollState())) } } }
            } else if(segment.isNotBlank()) SelectionContainer { Column(verticalArrangement=Arrangement.spacedBy(4.dp)) { segment.trim().lines().forEach { line ->
                val heading=line.startsWith("#")
                val value=if(heading) line.trimStart('#',' ') else line.replace(Regex("^[-*] "),"• ")
                Text(value.replace("**",""),lineHeight=24.sp,fontWeight=if(heading || value.startsWith("**")) FontWeight.SemiBold else FontWeight.Normal)
            } } }
        }
    }
}
@Suppress("DEPRECATION")
@Composable private fun CopyButton(text:String) {
    val clipboard=LocalClipboardManager.current
    var copied by remember(text) { mutableStateOf(false) }
    TextButton(onClick={clipboard.setText(AnnotatedString(text));copied=true}) { Icon(Icons.Rounded.ContentCopy,null,Modifier.size(16.dp)); Text(if(copied) " Copied" else " Copy") }
}
@Composable private fun SavedScreen(library:Library,open:(String)->Unit,remove:(String)->Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val notes=library.saved.filter{it.title.contains(query,true)||it.text.contains(query,true)}
    LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        item { Text("Worth keeping",fontSize=32.sp,fontWeight=FontWeight.Bold); Text("Good ideas, ready when you need them.",color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.padding(top=8.dp)) }
        item { OutlinedTextField(query,{query=it},placeholder={Text("Search saved answers")},leadingIcon={Icon(Icons.Rounded.Search,null)},singleLine=true,shape=RoundedCornerShape(20.dp),modifier=Modifier.fillMaxWidth()) }
        if(notes.isEmpty()) item { EmptyState(Icons.Rounded.BookmarkBorder,if(query.isBlank()) "Make a little room for good ideas" else "No matching answers", "Save a completed answer from any conversation to find it here.") }
        items(notes,key={it.id}) { note ->
            Surface(shape=RoundedCornerShape(24.dp),color=MaterialTheme.colorScheme.surface) { Column(Modifier.fillMaxWidth().padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                Text(note.title,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)
                Text("SAVED ANSWER",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.primary)
                SelectionContainer { Text(note.text,lineHeight=24.sp) }
                HorizontalDivider()
                if(library.conversations.any{it.id==note.conversationId}) TextButton(onClick={open(note.conversationId)}) { Text("Open conversation →") } else Text("Source conversation was deleted",style=MaterialTheme.typography.labelMedium)
                Row { CopyButton(note.text); TextButton(onClick={remove(note.id)}) { Text("Remove") } }
            } }
        }
    }
}

@Composable private fun SettingsDialog(configured:Boolean,theme:Int,onTheme:(Int)->Unit,configure:(String,String)->Boolean,clear:()->Unit,dismiss:()->Unit) {
    var key by remember { mutableStateOf("") }; var model by remember { mutableStateOf("gemini-2.5-flash") }; var error by remember { mutableStateOf(false) }; var confirmClear by remember { mutableStateOf(false) }
    // This dialog uses remember, never rememberSaveable, for credentials.
    val owner=androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val observer=androidx.lifecycle.LifecycleEventObserver { _,event -> if(event==androidx.lifecycle.Lifecycle.Event.ON_STOP){key=""} }
        owner.lifecycle.addObserver(observer); onDispose{owner.lifecycle.removeObserver(observer);key=""}
    }
    AlertDialog(onDismissRequest=dismiss,title={Text("Make it yours")},text={Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text("Appearance",fontWeight=FontWeight.Bold)
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) { listOf("System","Light","Dark").forEachIndexed { i,label -> FilterChip(selected=theme==i,onClick={onTheme(i)},label={Text(label)}) } }
        HorizontalDivider()
        Text("Gemini connection",fontWeight=FontWeight.Bold)
        Text("Your messages and selected conversation context are sent to Google Gemini. History is stored on this device. AI answers can be mistaken.")
        if(BuildConfig.DEBUG) {
            Text(if(configured) "Key configured for this session." else "Use your own Google AI Studio API key.",color=MaterialTheme.colorScheme.primary)
            OutlinedTextField(key,{key=it;error=false},label={Text("Gemini API key")},visualTransformation=PasswordVisualTransformation(),singleLine=true,modifier=Modifier.fillMaxWidth())
            OutlinedTextField(model,{model=it;error=false},label={Text("Model")},singleLine=true,modifier=Modifier.fillMaxWidth())
            Text("Debug learning mode. The key stays in memory and clears when you leave the app. It is never saved or bundled.",style=MaterialTheme.typography.bodySmall)
            if(error) Text("Enter a key and a valid model name.",color=MaterialTheme.colorScheme.error)
            Button(onClick={if(configure(key,model)){key="";dismiss()}else error=true}) { Text("Use for this session") }
        } else Text("Cloud chat is unavailable until an authenticated backend is configured. This release contains no direct API-key transport.")
        HorizontalDivider()
        TextButton(onClick={confirmClear=true}) { Text("Delete all local data",color=MaterialTheme.colorScheme.error) }
    }},confirmButton={TextButton(onClick=dismiss){Text("Done")}})
    if(confirmClear) AlertDialog(onDismissRequest={confirmClear=false},title={Text("Delete all local data?")},text={Text("This removes conversations and saved answers from this device. It cannot be undone and does not remove provider-side records.")},confirmButton={TextButton(onClick={clear();confirmClear=false;dismiss()}){Text("Delete all")}},dismissButton={TextButton(onClick={confirmClear=false}){Text("Cancel")}})
}

@Composable private fun AndroidEngineersResources() {
    Surface(shape=RoundedCornerShape(24.dp),color=MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxWidth().padding(20.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Text("Learn with Android Engineers",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)
            Text("Build skills, practice with projects, or find personal guidance.",color=MaterialTheme.colorScheme.onSurfaceVariant)
            ResourceLinks(AndroidEngineersGuide.resources)
        }
    }
}

@Composable private fun ResourceLinks(resources:List<LearningResource>) {
    val uriHandler=LocalUriHandler.current
    var unavailable by remember { mutableStateOf(false) }
    resources.forEach { resource ->
        TextButton(onClick={
            unavailable=runCatching { uriHandler.openUri(resource.url) }.isFailure
        }) {
            Icon(Icons.Rounded.OpenInNew,null,Modifier.size(16.dp))
            Text("  ${resource.title} on Android Engineers")
        }
    }
    if(unavailable) Text("No browser is available to open this link.",color=MaterialTheme.colorScheme.error,style=MaterialTheme.typography.bodySmall)
}
