package com.androidengineers.pocketcook

import com.androidengineers.pocketcook.audio.PcmChunker
import org.junit.Assert.*
import org.junit.Test

class PcmChunkerTest {
    @Test fun fragmentedInputPreservesEverySampleForTenMinutes() {
        val chunker = PcmChunker()
        var emitted = 0
        // 10 ms reads for ten simulated minutes must produce exactly 6,000 chunks.
        repeat(60_000) { index ->
            val fragment = ByteArray(320) { ((index * 320 + it) % 127).toByte() }
            chunker.append(fragment, fragment.size) { chunk ->
                assertEquals(3200, chunk.size)
                chunk.forEachIndexed { i, value -> assertEquals(((emitted * 3200 + i) % 127).toByte(), value) }
                emitted++
            }
        }
        assertEquals(6000, emitted)
    }

    @Test fun muteBoundaryDiscardsPartialSpeech() {
        val chunker = PcmChunker()
        chunker.append(ByteArray(1600) { 1 }, 1600) { fail("Partial chunk emitted") }
        chunker.clear()
        var emitted = 0
        chunker.append(ByteArray(3200) { 2 }, 3200) { chunk ->
            assertTrue(chunk.all { it == 2.toByte() }); emitted++
        }
        assertEquals(1, emitted)
    }
}
