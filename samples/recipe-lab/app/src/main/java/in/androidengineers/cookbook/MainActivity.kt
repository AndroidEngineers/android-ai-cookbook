package `in`.androidengineers.cookbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.*
import java.io.IOException

class LabViewModel : ViewModel() {
    val stream = StreamController(viewModelScope)
}
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { Surface { RecipeLab() } } }
    }
}

@Composable
fun RecipeLab(vm: LabViewModel = viewModel()) {
    var recipe by remember { mutableIntStateOf(0) }
    val names = listOf("1 Streaming", "2 Firebase", "3 Cancellation", "4 Extraction", "5 Recovery", "6 ADK tool")
    Column(Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp)) {
        Text("Android + AI Cookbook", style = MaterialTheme.typography.headlineSmall)
        Text("Fixture lab · no accounts required", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            names.take(3).forEachIndexed { index, name ->
                TextButton(onClick = { vm.stream.cancel(); recipe = index }, modifier = Modifier.weight(1f).testTag("recipe-$index")) { Text(name) }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            names.drop(3).forEachIndexed { index, name ->
                TextButton(onClick = { vm.stream.cancel(); recipe = index + 3 }, modifier = Modifier.weight(1f).testTag("recipe-${index + 3}")) { Text(name) }
            }
        }
        HorizontalDivider()
        key(recipe) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(names[recipe], style = MaterialTheme.typography.titleLarge)
                when (recipe) {
                    0, 1, 2 -> StreamRecipe(recipe, vm.stream)
                    3 -> ExtractionRecipe()
                    4 -> RecoveryRecipe()
                    5 -> AgentRecipe()
                }
            }
        }
    }
}

@Composable
private fun StreamRecipe(recipe: Int, controller: StreamController) {
    val state by controller.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var model by remember { mutableStateOf("") }
    var configError by remember { mutableStateOf("") }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(controller, lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) controller.cancel()
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer); controller.cancel() }
    }
    Text(when (recipe) {
        1 -> "Try the fixture first. Live Firebase requires your project configuration, App Check registration, an enabled model, and may incur charges."
        2 -> "Start request A, then replace it with B or cancel. Leaving this screen stops the request."
        else -> "Watch a deterministic response arrive in chunks. Inject an error to inspect recovery."
    })
    Button(onClick = { controller.start(FixtureSource(), "Request A") }, modifier = Modifier.testTag("stream-start")) { Text("Run fixture A") }
    if (recipe == 2) Button(onClick = { controller.start(FixtureSource(), "Request B") }, modifier = Modifier.testTag("stream-replace")) { Text("Replace with B") }
    Button(onClick = { controller.start(FixtureSource(fail = true), "Failure") }, modifier = Modifier.testTag("stream-error")) { Text("Inject stream error") }
    OutlinedButton(onClick = controller::cancel, modifier = Modifier.testTag("stream-cancel")) { Text("Cancel") }
    if (recipe == 1) {
        OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Enabled Firebase model name") })
        Button(onClick = {
            configError = ""
            if (FirebaseApp.getApps(context).isEmpty()) configError = "Firebase is not configured. Add your google-services.json and rebuild."
            else if (model.isBlank()) configError = "Enter a model enabled for your Firebase project."
            else try { controller.start(FirebaseSource(model.trim()), "Explain Android ViewModel in two sentences.") }
            catch (e: Exception) { configError = "Firebase setup error: ${e.message}" }
        }, modifier = Modifier.testTag("firebase-live")) { Text("Send live request (may cost)") }
        Text(configError, modifier = Modifier.testTag("firebase-error"))
    }
    Text(state.phase, modifier = Modifier.testTag("stream-phase"))
    Text(state.text, modifier = Modifier.testTag("stream-output"))
}

@Composable
private fun ExtractionRecipe() {
    var raw by remember { mutableStateOf("{\"title\":\"Practise Compose\",\"minutes\":25}") }
    var result by remember { mutableStateOf("Not validated") }
    Text("Treat output as untrusted data. Passing validation does not establish factual accuracy.")
    OutlinedTextField(raw, { raw = it }, label = { Text("Synthetic model output") }, modifier = Modifier.testTag("extraction-input"))
    Button(onClick = {
        result = try { "Accepted: ${validateTask(raw)}" } catch (e: Exception) { "Rejected: ${e.message}" }
    }, modifier = Modifier.testTag("validate")) { Text("Validate") }
    OutlinedButton(onClick = { raw = "{\"title\":\"Unbounded\",\"minutes\":999}" }, modifier = Modifier.testTag("invalid-fixture")) { Text("Load invalid fixture") }
    Text(result, modifier = Modifier.testTag("extraction-result"))
}

@Composable
private fun RecoveryRecipe() {
    val scope = rememberCoroutineScope()
    var job by remember { mutableStateOf<Job?>(null) }
    var state by remember { mutableStateOf("Ready") }
    fun run(mode: String) {
        job?.cancel()
        job = scope.launch {
            var attempts = 0
            try {
                val result = boundedRead(timeoutMs = 300, onAttempt = { attempts = it; state = "Attempt $it" }) {
                    delay(if (mode == "timeout") 1000 else 120)
                    if (mode == "offline" || attempts < 3) throw IOException("Injected offline/transient failure")
                    "Recovered on attempt $attempts"
                }
                state = result
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (e: Exception) { state = "Stopped after $attempts attempts: ${e.message}" }
        }
    }
    Text("Retry idempotent reads only, with three attempts and a per-attempt timeout. No real network is used.")
    Button(onClick = { run("transient") }, modifier = Modifier.testTag("retry-recover")) { Text("Fail twice, recover") }
    Button(onClick = { run("offline") }, modifier = Modifier.testTag("retry-offline")) { Text("Always offline") }
    Button(onClick = { run("timeout") }, modifier = Modifier.testTag("retry-timeout")) { Text("Always timeout") }
    OutlinedButton(onClick = { job?.cancel(); state = "Cancelled" }, modifier = Modifier.testTag("retry-cancel")) { Text("Cancel") }
    Text(state, modifier = Modifier.testTag("retry-result"))
}

@Composable
private fun AgentRecipe() {
    val scope = rememberCoroutineScope()
    var result by remember { mutableStateOf("No action recorded") }
    var running by remember { mutableStateOf(false) }
    fun run(approved: Int?, proposed: Int = 25) {
        scope.launch {
            running = true
            result = "Running actual ADK with a scripted model…"
            try {
                val output = runSafeAgent(approved, proposed)
                result = "Writes: ${output.writes}\n${output.transcript}"
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (e: Exception) { result = "Agent stopped: ${e.message}" }
            finally { running = false }
        }
    }
    Text("The model proposes 25 minutes. The app grants one exact approval per run. All writes are in memory; nothing is sent or saved externally.")
    Button(onClick = { run(null) }, enabled = !running, modifier = Modifier.testTag("agent-deny")) { Text("Run without approval") }
    Button(onClick = { run(25) }, enabled = !running, modifier = Modifier.testTag("agent-approve")) { Text("Approve exactly 25 minutes") }
    OutlinedButton(onClick = { run(25, 60) }, enabled = !running, modifier = Modifier.testTag("agent-mismatch")) { Text("Propose 60 after approving 25") }
    Text(result, modifier = Modifier.testTag("agent-result"))
}
