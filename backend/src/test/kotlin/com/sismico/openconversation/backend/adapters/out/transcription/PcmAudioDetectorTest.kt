package com.sismico.openconversation.backend.adapters.out.transcription

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder

class PcmAudioDetectorTest {
    @Test
    fun `returns true for PCM WAV`() {
        assertTrue(PcmAudioDetector.isPcmWav(buildPcmWav()))
    }

    @Test
    fun `returns false for non-PCM WAV`() {
        assertFalse(PcmAudioDetector.isPcmWav(buildWav(audioFormat = 7)))
    }

    @Test
    fun `returns false for WebM`() {
        assertFalse(PcmAudioDetector.isPcmWav(byteArrayOf(0x1A, 0x45, 0xDF.toByte(), 0xA3.toByte())))
    }

    @Test
    fun `returns false for empty input`() {
        assertFalse(PcmAudioDetector.isPcmWav(byteArrayOf()))
    }

    @Test
    fun `returns false for short input`() {
        assertFalse(PcmAudioDetector.isPcmWav(byteArrayOf(0x52, 0x49, 0x46, 0x46)))
    }

    @Test
    fun `returns false when RIFF marker is missing`() {
        assertFalse(PcmAudioDetector.isPcmWav(buildPcmWav().also { it[0] = 0x00 }))
    }

    @Test
    fun `returns false when WAVE marker is missing`() {
        val wav = buildPcmWav()
        wav[8] = 0x00
        wav[9] = 0x00
        wav[10] = 0x00
        wav[11] = 0x00
        assertFalse(PcmAudioDetector.isPcmWav(wav))
    }

    @Test
    fun `detects PCM when fmt chunk is not immediately after WAVE marker`() {
        val wav = buildPcmWav()
        assertTrue(PcmAudioDetector.isPcmWav(wav))
    }

    private fun buildPcmWav(): ByteArray = buildWav(audioFormat = 1)

    private fun buildWav(audioFormat: Short): ByteArray {
        val sampleRate = 16000
        val channels = 1
        val bitsPerSample = 16
        val sampleCount = sampleRate / 2
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val blockAlign = (channels * bitsPerSample / 8).toShort()
        val dataSize = sampleCount * blockAlign
        val headerSize = 44
        val totalSize = dataSize + headerSize - 8
        val totalSizeBytes = headerSize + dataSize

        val buffer = ByteBuffer.allocate(totalSizeBytes).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put("RIFF".toByteArray(Charsets.US_ASCII))
        buffer.putInt(totalSize)
        buffer.put("WAVE".toByteArray(Charsets.US_ASCII))
        buffer.put("fmt ".toByteArray(Charsets.US_ASCII))
        buffer.putInt(16)
        buffer.putShort(audioFormat)
        buffer.putShort(channels.toShort())
        buffer.putInt(sampleRate)
        buffer.putInt(byteRate)
        buffer.putShort(blockAlign)
        buffer.putShort(bitsPerSample.toShort())
        buffer.put("data".toByteArray(Charsets.US_ASCII))
        buffer.putInt(dataSize)
        repeat(sampleCount) { buffer.putShort(0) }

        return buffer.array()
    }
}
