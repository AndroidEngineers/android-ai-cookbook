package com.androidengineers.pocketcook

import android.Manifest
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidengineers.pocketcook.audio.AndroidPcmAudio
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicBoolean

@RunWith(AndroidJUnit4::class)
class AudioPlaybackTest {
    @Test fun smallChunksKeepFeedingUntilPlaybackAdvances() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            instrumentation.uiAutomation.grantRuntimePermission(context.packageName, Manifest.permission.RECORD_AUDIO)
        }
        val failed = AtomicBoolean(false)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            val audio = AndroidPcmAudio(context)
            try {
                scenario.onActivity { audio.start({ _, _ -> }, { failed.set(true) }, {}) }
                // 10 ms chunks: the first is smaller than the usual device startup buffer.
                // Silence verifies frame consumption without playing a test tone.
                repeat(20) { audio.play(ByteArray(480)); Thread.sleep(10) }
                val deadline = android.os.SystemClock.elapsedRealtime() + 5000
                while (audio.playedFrames() < 4800 && !failed.get() && android.os.SystemClock.elapsedRealtime() < deadline) {
                    Thread.sleep(20)
                }
                assertFalse("Audio device failed", failed.get())
                assertTrue("Playback must consume all 20 chunks, not stall on the first", audio.playedFrames() >= 4800)
            } finally {
                scenario.onActivity { audio.close() }
            }
        }
    }
    @Test fun sustainedCaptureUsesFullChunksAndSurvivesMuteAndRestart() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            instrumentation.uiAutomation.grantRuntimePermission(context.packageName, Manifest.permission.RECORD_AUDIO)
        }
        val failed = AtomicBoolean(false)
        val badSize = AtomicBoolean(false)
        val captured = java.util.concurrent.atomic.AtomicInteger()
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            repeat(2) {
                val audio = AndroidPcmAudio(context)
                try {
                    scenario.onActivity { audio.start({ bytes, _ ->
                        if (bytes.size != 3200) badSize.set(true)
                        captured.incrementAndGet()
                    }, { failed.set(true) }, {}) }
                    // Fifteen seconds per session. No captured audio is stored or transmitted.
                    repeat(150) { audio.play(ByteArray(4800)); Thread.sleep(100) }
                    scenario.onActivity { audio.mute(true) }
                    Thread.sleep(200)
                    val mutedCount = captured.get()
                    Thread.sleep(300)
                    assertTrue("Muted microphone must stop capture", captured.get() == mutedCount)
                    scenario.onActivity { audio.mute(false) }
                    Thread.sleep(500)
                    assertTrue("Capture must resume", captured.get() > mutedCount)
                } finally { scenario.onActivity { audio.close() } }
            }
        }
        assertFalse("Device audio failure", failed.get())
        assertFalse("All microphone chunks must represent 100 ms", badSize.get())
        assertTrue("Capture must run throughout both sessions", captured.get() >= 250)
    }

}
