package com.androidengineers.pocketcook.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.net.toUri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidengineers.pocketcook.*
import com.androidengineers.pocketcook.R
import com.androidengineers.pocketcook.data.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun PocketCookApp(model: CookViewModel) {
 val state by model.state.collectAsStateWithLifecycle()
 val voice by model.live.state.collectAsStateWithLifecycle()
 var setup by remember { mutableStateOf(false) }
 var sheet by remember { mutableStateOf<String?>(null) }
 val context = LocalContext.current
 val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
  if (granted) model.startVoice() else model.notice("Microphone access is off. You can still follow the recipe, or enable permission in Android settings.")
 }
 fun start() {
  if (!model.configured && BuildConfig.DEBUG) { setup = true; return }
  if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) model.startVoice()
  else permission.launch(Manifest.permission.RECORD_AUDIO)
 }
 BackHandler(state.screen != Screen.Library) { model.back() }
 Scaffold(containerColor=MaterialTheme.colorScheme.background, topBar={
  if (state.screen != Screen.Library) TopAppBar(title={ Text(if(state.screen == Screen.Cooking) state.recipe.title else "PocketCook", style=MaterialTheme.typography.titleMedium, fontWeight=FontWeight.SemiBold) }, navigationIcon={ IconButton(onClick=model::back) { Icon(Icons.AutoMirrored.Filled.ArrowBack,"Back") } }, actions={
   if(state.screen == Screen.Cooking) TextButton(onClick={model.live.end()}, enabled=voice.connection == Connection.Connected || voice.connection == Connection.Connecting) { Text("End voice") }
  })
 }) { padding ->
  Box(Modifier.fillMaxSize().padding(padding)) {
   when(state.screen) {
    Screen.Library -> Library(model::select, {setup=true})
    Screen.Details -> Details(state, model::check, model::cook)
    Screen.Cooking -> Cooking(state, voice, {model.step(-1)}, {model.step(1)}, model::finish, ::start, model.live::mute, {sheet=it}, {setup=true}, {context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:${context.packageName}".toUri()))})
    Screen.Finished -> Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment=Alignment.CenterHorizontally, verticalArrangement=Arrangement.Center) {
     Icon(Icons.Default.CheckCircle,null,Modifier.size(64.dp),tint=MaterialTheme.colorScheme.primary)
     Spacer(Modifier.height(24.dp)); Text("Made with a little help.",style=MaterialTheme.typography.headlineMedium)
     Spacer(Modifier.height(12.dp)); Text("Your ${state.recipe.title.lowercase()} is ready to enjoy.")
     Spacer(Modifier.height(32.dp)); Button(onClick=model::back) { Text("Back to recipes") }
    }
   }
  }
 }
 if(setup) SetupDialog(model.model, {setup=false}, { key, name -> model.configure(key,name); setup=false; model.notice("Connection configured for this foreground session. Tap Talk to PocketCook to start.") })
 if(sheet != null) ModalBottomSheet(onDismissRequest={sheet=null}) {
  Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp).padding(bottom=32.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) {
   Text(sheet!!,style=MaterialTheme.typography.headlineSmall)
   if(sheet=="Ingredients") IngredientList(state,model::check)
   else Text(voice.transcript.ifBlank { "Your conversation will appear here after you start talking. Transcripts are not saved." },style=MaterialTheme.typography.bodyLarge)
  }
 }
}

@Composable private fun Library(select: (String)->Unit, settings: ()->Unit) {
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal=24.dp).widthIn(max=900.dp), verticalArrangement=Arrangement.spacedBy(20.dp)) {
  Spacer(Modifier.height(8.dp))
  Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
   Column(Modifier.weight(1f)) {
    Icon(Icons.Default.Restaurant,null,tint=MaterialTheme.colorScheme.primary,modifier=Modifier.size(28.dp))
    Text("PocketCook",style=MaterialTheme.typography.headlineLarge,fontWeight=FontWeight.Bold)
    Text("A little help in the kitchen.",color=MaterialTheme.colorScheme.onSurfaceVariant)
   }
   IconButton(onClick=settings) { Icon(Icons.Default.Settings,"Connection settings") }
  }
  Text("ON THE MENU",style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.primary,letterSpacing=2.sp)
  Card(onClick={select("pasta")}, shape=RoundedCornerShape(24.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surfaceVariant)) {
   FoodImage("pasta",Modifier.fillMaxWidth().height(215.dp))
   Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
    Text("Tomato pasta",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
    Row(verticalAlignment=Alignment.CenterVertically) {
     Text("25 min · Easy · Serves 2",Modifier.weight(1f),style=MaterialTheme.typography.bodyMedium)
     FilledIconButton(onClick={select("pasta")}) { Icon(Icons.AutoMirrored.Filled.ArrowForward,"Cook tomato pasta") }
    }
   }
  }
  Text("More to make",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.SemiBold)
  Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
   for(recipe in Recipes.all.drop(1)) Card(onClick={select(recipe.id)},modifier=Modifier.weight(1f),shape=RoundedCornerShape(20.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surfaceVariant)) {
    FoodImage(recipe.id,Modifier.fillMaxWidth().height(112.dp))
    Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
     Text(recipe.title,style=MaterialTheme.typography.titleSmall,fontWeight=FontWeight.Bold)
     Text("${recipe.minutes} min · Easy",style=MaterialTheme.typography.bodySmall)
    }
   }
  }
  Text("Recipes work offline. Voice help connects to Gemini when you choose to start.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
  Spacer(Modifier.height(16.dp))
 }
}
@Composable private fun FoodImage(id: String, modifier: Modifier) {
 val resource = when(id) { "bowl" -> R.drawable.chickpea; "stirfry" -> R.drawable.stirfry; else -> R.drawable.pasta }
 Image(painterResource(resource),null,modifier,contentScale=ContentScale.Crop)
}
@Composable private fun IngredientList(state: CookState, check: (Int)->Unit) {
 state.recipe.ingredients.forEachIndexed { index, ingredient ->
  Row(Modifier.fillMaxWidth().clickable{check(index)}.padding(vertical=2.dp),verticalAlignment=Alignment.CenterVertically) {
   Checkbox(index in state.checked,onCheckedChange={check(index)})
   Text(ingredient,style=MaterialTheme.typography.bodyLarge)
  }
 }
}
@Composable private fun Details(state: CookState, check: (Int)->Unit, cook: ()->Unit) {
 Column(Modifier.fillMaxSize()) {
  Column(Modifier.weight(1f).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(20.dp)) {
   FoodImage(state.recipeId,Modifier.fillMaxWidth().height(225.dp))
   Column(Modifier.padding(horizontal=24.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
    Text(state.recipe.title,style=MaterialTheme.typography.headlineLarge,fontWeight=FontWeight.Bold)
    Text("${state.recipe.minutes} min · Easy · Serves 2",color=MaterialTheme.colorScheme.onSurfaceVariant)
    Text(state.recipe.description,style=MaterialTheme.typography.bodyLarge)
    Text("Ingredients",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)
    IngredientList(state,check)
    Text("The steps",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)
    state.recipe.steps.forEachIndexed { i,s -> Text("${i+1}. ${s.title}",style=MaterialTheme.typography.bodyLarge) }
    Spacer(Modifier.height(8.dp))
   }
  }
  Button(onClick=cook,Modifier.fillMaxWidth().padding(20.dp).heightIn(min=56.dp),shape=RoundedCornerShape(18.dp)) { Text(if(state.step>0) "Continue cooking" else "Start cooking"); Spacer(Modifier.width(12.dp)); Icon(Icons.AutoMirrored.Filled.ArrowForward,null) }
 }
}
@Composable private fun Cooking(state: CookState, voice: VoiceState, previous:()->Unit, next:()->Unit, finish:()->Unit, start:()->Unit, mute:()->Unit, sheet:(String)->Unit, settings:()->Unit, permissions:()->Unit) {
 BoxWithConstraints(Modifier.fillMaxSize()) {
  val expanded = maxWidth >= 720.dp
  Row(Modifier.fillMaxSize().padding(horizontal=24.dp),horizontalArrangement=Arrangement.spacedBy(32.dp)) {
   if(expanded) Column(Modifier.weight(.8f).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(16.dp)) {
    FoodImage(state.recipeId,Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(24.dp)))
    Text(state.recipe.title,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
    Text("Ingredients",style=MaterialTheme.typography.titleLarge)
    state.recipe.ingredients.forEach { Text("• $it",style=MaterialTheme.typography.bodyLarge) }
   }
   Column(Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(16.dp)) {
    val connected = voice.connection == Connection.Connected
    Surface(color=MaterialTheme.colorScheme.surfaceVariant,shape=CircleShape) { Text(voice.message,Modifier.padding(horizontal=14.dp,vertical=8.dp),style=MaterialTheme.typography.labelMedium) }
    if(!expanded) FoodImage(state.recipeId,Modifier.size(132.dp,94.dp).clip(RoundedCornerShape(20.dp)))
    Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(8.dp)) {
     Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
      Text("STEP ${state.step+1} OF ${state.recipe.steps.size}",Modifier.weight(1f),style=MaterialTheme.typography.labelMedium,letterSpacing=1.sp)
      TextButton(onClick={sheet("Ingredients")}) { Text("Ingredients") }
     }
     Text(state.recipe.steps[state.step].title,style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold)
     Text(state.recipe.steps[state.step].instruction,style=MaterialTheme.typography.bodyLarge)
    }
    LinearProgressIndicator(progress={(state.step+1f)/state.recipe.steps.size},modifier=Modifier.fillMaxWidth(),color=MaterialTheme.colorScheme.primary,trackColor=MaterialTheme.colorScheme.surfaceVariant)
    Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
     OutlinedButton(onClick=previous,enabled=state.step>0,modifier=Modifier.weight(1f).heightIn(min=52.dp),shape=RoundedCornerShape(16.dp)) { Text("Previous") }
     Button(onClick=if(state.step == state.recipe.steps.lastIndex) finish else next,modifier=Modifier.weight(1f).heightIn(min=52.dp),shape=RoundedCornerShape(16.dp)) { Text(if(state.step==state.recipe.steps.lastIndex) "Finish cooking" else "Next") }
    }
    if(state.notice != null) Surface(color=MaterialTheme.colorScheme.primaryContainer,shape=RoundedCornerShape(16.dp)) {
     Column(Modifier.padding(16.dp)) { Text(state.notice); if(state.notice.contains("permission")) TextButton(onClick=permissions){Text("Open Android settings")} }
    }
    if(voice.connection==Connection.Error) {
     Button(onClick=start) { Text("Retry voice") }
     TextButton(onClick=settings) { Text("Connection settings") }
    }
    Surface(Modifier.fillMaxWidth(),color=MaterialTheme.colorScheme.surfaceVariant,shape=RoundedCornerShape(24.dp)) {
     Column(Modifier.padding(20.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(14.dp)) {
      if(connected) {
       Text(if(voice.muted) "Microphone off" else if(voice.speaking) "Speaking · Mic on" else "Listening · Mic on",style=MaterialTheme.typography.titleMedium)
       LinearProgressIndicator(progress={if(voice.muted) 0f else voice.level},modifier=Modifier.width(140.dp),color=MaterialTheme.colorScheme.primary)
       FilledTonalButton(onClick=mute,modifier=Modifier.heightIn(min=56.dp)) { Icon(if(voice.muted) Icons.Default.Mic else Icons.Default.MicOff,null); Spacer(Modifier.width(8.dp)); Text(if(voice.muted) "Unmute microphone" else "Mute microphone") }
      } else {
       Text("A little help, hands-free.",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.SemiBold)
       Text("Ask a question about this recipe. Audio is sent to Gemini only while voice is on.",style=MaterialTheme.typography.bodyMedium)
       Button(onClick=start,enabled=voice.connection!=Connection.Connecting,modifier=Modifier.fillMaxWidth().heightIn(min=56.dp),shape=RoundedCornerShape(18.dp)) { Icon(Icons.Default.Mic,null); Spacer(Modifier.width(8.dp)); Text(if(voice.connection==Connection.Connecting) "Connecting…" else "Talk to PocketCook") }
      }
      TextButton(onClick={sheet("Transcript")}) { Text("View transcript") }
     }
    }
    Spacer(Modifier.height(8.dp))
   }
  }
 }
}
@Composable private fun SetupDialog(initialModel: String, dismiss:()->Unit, save:(String,String)->Unit) {
 var key by remember { mutableStateOf("") }
 var name by remember { mutableStateOf(initialModel) }
 AlertDialog(onDismissRequest=dismiss,properties=DialogProperties(securePolicy=SecureFlagPolicy.SecureOn),title={Text("Connect your Gemini")},text={
  Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(16.dp)) {
   Text(if(BuildConfig.DEBUG) "Development setup: use your own Google AI Studio API key. It stays in memory and is cleared when you leave the app. Gemini usage may incur charges." else "Voice deployment is not configured in this release. Recipes remain available offline.")
   if(BuildConfig.DEBUG) {
    OutlinedTextField(key,{key=it},label={Text("Gemini API key")},singleLine=true,visualTransformation=PasswordVisualTransformation())
    OutlinedTextField(name,{name=it},label={Text("Live model ID")},singleLine=true)
    Text("Microphone access is requested only when you start voice. Never share your key in screenshots or commits.",style=MaterialTheme.typography.bodySmall)
   }
  }
 },confirmButton={if(BuildConfig.DEBUG) TextButton(onClick={save(key,name)},enabled=key.isNotBlank() && name.matches(Regex("[A-Za-z0-9._-]+"))) {Text("Use for this session")}},dismissButton={TextButton(onClick=dismiss){Text("Close")}})
}
