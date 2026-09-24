package com.androidengineers.pocketstories

import android.content.Context
import android.hardware.*
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.androidengineers.pocketstories.ui.*
import kotlin.math.sqrt

class MainActivity : ComponentActivity(), SensorEventListener {
    private val vm: StoriesViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = application as StoriesApplication
                return StoriesViewModel(app.stories, app.model, app.images, app.generator) as T
            }
        }
    }
    private val sensors by lazy { getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    private var lastShake = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle =
                SystemBarStyle.light(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.TRANSPARENT,
                ),
            navigationBarStyle =
                SystemBarStyle.light(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.TRANSPARENT,
                ),
        )
        setContent { StoriesTheme { StoriesScreen(vm) } }
    }

    override fun onResume() {
        super.onResume()
        sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
            sensors.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        sensors.unregisterListener(this)
        super.onPause()
    }

    override fun onStop() {
        if (!isChangingConfigurations) vm.background()
        super.onStop()
    }

    override fun onSensorChanged(event: SensorEvent) {
        val strength =
            sqrt(event.values.sumOf { (it * it).toDouble() }) / SensorManager.GRAVITY_EARTH
        val now = android.os.SystemClock.elapsedRealtime()
        if (strength > 2.5 && now - lastShake > 2500) {
            lastShake = now
            vm.shake()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
