package com.androidengineers.pocketstories.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.File

@Composable
fun CameraScreen(onPhoto: (Uri) -> Unit, onBack: () -> Unit, busy: Boolean) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var capturing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val permission =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            granted = it
        }
    val photos =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) {
            it?.let(onPhoto)
        }
    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(LifecycleCameraController.IMAGE_CAPTURE)
        }
    }
    var ready by remember { mutableStateOf(false) }
    val preview = remember {
        PreviewView(context).apply {
            this.controller = controller
            scaleType = PreviewView.ScaleType.FIT_CENTER
        }
    }
    DisposableEffect(owner, preview) {
        val observer =
            androidx.lifecycle.Observer<PreviewView.StreamState> {
                ready = it == PreviewView.StreamState.STREAMING
            }
        preview.previewStreamState.observe(owner, observer)
        onDispose { preview.previewStreamState.removeObserver(observer) }
    }
    DisposableEffect(owner, granted) {
        if (granted)
            runCatching { controller.bindToLifecycle(owner) }
                .onFailure { error = "Camera unavailable. Choose a photo instead." }
        onDispose { controller.unbind() }
    }
    Box(Modifier.fillMaxSize().background(Ink)) {
        if (granted)
            AndroidView(
                factory = { preview },
                modifier = Modifier.fillMaxSize(),
            )
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Surface(shape = RoundedCornerShape(20.dp), color = Ink.copy(alpha = .88f)) {
                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                    Text(
                        "Find a little wonder.",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Paper,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "A toy, a key, a favourite mug.\nYour next adventure starts here.",
                        color = Paper,
                    )
                }
            }
            if (!granted)
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        Icons.Outlined.PhotoCamera,
                        null,
                        tint = Gold,
                        modifier = Modifier.size(56.dp),
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("Let your camera inspire a story.", color = Paper)
                    Button(onClick = { permission.launch(Manifest.permission.CAMERA) }) {
                        Text("Enable camera")
                    }
                    Text("Or choose a photo below.", color = Paper)
                }
            Surface(shape = RoundedCornerShape(24.dp), color = Ink.copy(alpha = .94f)) {
                Column(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    error?.let { Text(it, color = Gold) }
                    if (granted && !ready) Text("Opening camera…", color = Paper)
                    Button(
                        onClick = {
                            capturing = true
                            error = null
                            val file = File(context.cacheDir, "scene-capture.jpg")
                            try {
                                controller.takePicture(
                                    ImageCapture.OutputFileOptions.Builder(file).build(),
                                    ContextCompat.getMainExecutor(context),
                                    object : ImageCapture.OnImageSavedCallback {
                                        override fun onImageSaved(
                                            result: ImageCapture.OutputFileResults
                                        ) {
                                            capturing = false
                                            onPhoto(Uri.fromFile(file))
                                        }

                                        override fun onError(exception: ImageCaptureException) {
                                            capturing = false
                                            error =
                                                "Could not capture. Try again or choose a photo."
                                        }
                                    },
                                )
                            } catch (e: Exception) {
                                capturing = false
                                error = "Camera is still preparing. Try again."
                            }
                        },
                        enabled = granted && ready && !busy && !capturing,
                        colors =
                            ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                    ) {
                        Icon(Icons.Outlined.CameraAlt, null)
                        Spacer(Modifier.width(12.dp))
                        Text(if (capturing || busy) "Preparing scene…" else "Capture this scene")
                    }
                    Row {
                        TextButton(
                            onClick = {
                                photos.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            enabled = !busy && !capturing,
                        ) {
                            Text("Choose photo", color = Paper)
                        }
                        TextButton(onClick = onBack, enabled = !busy && !capturing) {
                            Text("Use words instead", color = Paper)
                        }
                    }
                    Text(
                        "One snapshot. No recording. No cloud.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Paper,
                    )
                }
            }
        }
    }
}
