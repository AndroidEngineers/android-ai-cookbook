package com.androidengineers.pocketcook.audio

/** Collects partial device reads into 100 ms of mono PCM16 at 16 kHz.
 * Owned by the capture loop; clear at a mute boundary so old speech is not replayed.
 */
internal class PcmChunker {
    private val pending = ByteArray(3200)
    private var used = 0

    fun append(input: ByteArray, count: Int, emit: (ByteArray) -> Unit) {
        require(count in 0..input.size && count % 2 == 0)
        var offset = 0
        while (offset < count) {
            val length = minOf(pending.size - used, count - offset)
            input.copyInto(pending, used, offset, offset + length)
            used += length
            offset += length
            if (used == pending.size) {
                val chunk = pending.copyOf()
                used = 0
                emit(chunk)
            }
        }
    }

    fun clear() { pending.fill(0); used = 0 }
}
