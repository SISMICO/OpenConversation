package com.sismico.openconversation.backend.adapters.out.transcription

import com.sismico.openconversation.backend.domain.exception.AudioConversionException
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteIfExists

class FfmpegAudioConversionAdapterIntegrationTest {
    private lateinit var adapter: FfmpegAudioConversionAdapter

    @BeforeEach
    fun setUp() {
        assumeTrue(isFfmpegAvailable(), "ffmpeg is not available on PATH")
        adapter = FfmpegAudioConversionAdapter()
    }

    @Test
    fun `passes through PCM WAV unchanged`() {
        val pcmWav = buildPcmWav()

        val result = adapter.convert(pcmWav, "recording.wav")

        assertArrayEquals(pcmWav, result)
    }

    @Test
    fun `converts MP3 to PCM WAV`() {
        val mp3File = Files.createTempFile("input", ".mp3")

        try {
            generateMp3Fixture(mp3File)

            val mp3Bytes = Files.readAllBytes(mp3File)
            val result = adapter.convert(mp3Bytes, "recording.mp3")

            assertTrue(PcmAudioDetector.isPcmWav(result), "converted result should be PCM WAV")
            assertTrue(result.isNotEmpty(), "converted result should not be empty")
        } finally {
            mp3File.deleteIfExists()
        }
    }

    @Test
    fun `throws exception when input is not valid audio`() {
        val invalidAudio = byteArrayOf(0x00, 0x01, 0x02, 0x03)

        assertThrows<AudioConversionException> {
            adapter.convert(invalidAudio, "recording.webm")
        }
    }

    private fun isFfmpegAvailable(): Boolean =
        try {
            ProcessBuilder("ffmpeg", "-version")
                .redirectErrorStream(true)
                .start()
                .waitFor() == 0
        } catch (_: Exception) {
            false
        }

    private fun runFfmpeg(
        vararg args: String,
        input: ByteArray,
    ) {
        val process = ProcessBuilder(listOf("ffmpeg") + args.toList()).start()
        process.outputStream.use { it.write(input) }

        val exitCode = process.waitFor()
        if (exitCode != 0) {
            val error = process.errorStream.bufferedReader().readText()
            throw IllegalStateException("ffmpeg fixture generation failed: $error")
        }
    }

    private fun generateMp3Fixture(output: Path) {
        val process =
            ProcessBuilder(
                "ffmpeg",
                "-y",
                "-f",
                "lavfi",
                "-i",
                "anullsrc=r=16000:cl=mono",
                "-t",
                "1",
                "-c:a",
                "libmp3lame",
                "-b:a",
                "64k",
                output.toString(),
            ).start()

        val exitCode = process.waitFor()
        if (exitCode != 0) {
            val error = process.errorStream.bufferedReader().readText()
            throw IllegalStateException("ffmpeg MP3 fixture generation failed: $error")
        }
    }

    private fun buildPcmWav(): ByteArray {
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
        buffer.putShort(1)
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
