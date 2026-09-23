package com.androidengineers.pocketcook
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.androidengineers.pocketcook.audio.AndroidPcmAudio
import com.androidengineers.pocketcook.data.*
import com.androidengineers.pocketcook.ui.*
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {
 private lateinit var model: CookViewModel
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  enableEdgeToEdge()
  model = ViewModelProvider(this, object : ViewModelProvider.Factory {
   @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    return CookViewModel(LocalProgress(applicationContext), GeminiLiveConnection(scope, { AndroidPcmAudio(applicationContext) })) as T
   }
  })[CookViewModel::class.java]
  setContent { PocketCookTheme { PocketCookApp(model) } }
 }
 override fun onStop() {
  if (!isChangingConfigurations && ::model.isInitialized) model.background()
  super.onStop()
 }
}
