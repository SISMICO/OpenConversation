package com.sismico.openconversation.backend.adapters.out.transcription

import com.sismico.openconversation.backend.domain.exception.AudioConversionException
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files
import java.nio.file.Paths

class FfmpegAudioConversionAdapterTest {
    @Test
    fun `returns PCM WAV unchanged without invoking ffmpeg`() {
        val pcmWav = buildPcmWav()
        val runner = CapturingFfmpegCommandRunner()
        val adapter = FfmpegAudioConversionAdapter(runner)

        val result = adapter.convert(pcmWav, "recording.wav")

        assertArrayEquals(pcmWav, result)
        assertTrue(runner.invocations.isEmpty(), "ffmpeg should not be invoked for PCM WAV")
    }

    @Test
    fun `invokes ffmpeg and returns converted bytes for non-PCM input`() {
        val input = byteArrayOf(0x1A, 0x45, 0xDF.toByte(), 0xA3.toByte())
        val converted = byteArrayOf(9, 8, 7, 6)
        val runner =
            CapturingFfmpegCommandRunner { args ->
                val outputPath = args.last()
                Files.write(Paths.get(outputPath), converted)
                FfmpegResult(0, byteArrayOf(), "")
            }
        val adapter = FfmpegAudioConversionAdapter(runner)

        val result = adapter.convert(input, "recording.webm")

        assertArrayEquals(converted, result)
        assertEquals(1, runner.invocations.size)

        val invocation = runner.invocations.first()
        assertEquals("-y", invocation.first())
        assertTrue(invocation.contains("-ar"))
        assertTrue(invocation.contains("16000"))
        assertTrue(invocation.contains("-ac"))
        assertTrue(invocation.contains("1"))
        assertTrue(invocation.contains("-c:a"))
        assertTrue(invocation.contains("pcm_s16le"))
        assertTrue(invocation.contains("-f"))
        assertTrue(invocation.contains("wav"))
    }

    @Test
    fun `throws AudioConversionException when ffmpeg exits with error`() {
        val input = byteArrayOf(0x00, 0x01, 0x02)
        val runner = CapturingFfmpegCommandRunner { _ -> FfmpegResult(1, byteArrayOf(), "invalid data") }
        val adapter = FfmpegAudioConversionAdapter(runner)

        val exception =
            assertThrows<AudioConversionException> {
                adapter.convert(input, "recording.webm")
            }

        assertTrue(exception.message!!.contains("Audio conversion failed"))
        assertTrue(exception.message!!.contains("invalid data"))
    }

    @Test
    fun `deletes temporary files after successful conversion`() {
        val input = byteArrayOf(0x1A, 0x45, 0xDF.toByte(), 0xA3.toByte())
        val runner =
            CapturingFfmpegCommandRunner { args ->
                val outputPath = args.last()
                Files.write(Paths.get(outputPath), byteArrayOf(1, 2, 3))
                FfmpegResult(0, byteArrayOf(), "")
            }
        val adapter = FfmpegAudioConversionAdapter(runner)

        adapter.convert(input, "recording.webm")

        val invocation = runner.invocations.first()
        val inputPath = invocation[invocation.indexOf("-i") + 1]
        val outputPath = invocation.last()
        assertTrue(Files.notExists(Paths.get(inputPath)), "input temp file should be deleted")
        assertTrue(Files.notExists(Paths.get(outputPath)), "output temp file should be deleted")
    }

    @Test
    fun `deletes temporary files after failed conversion`() {
        val input = byteArrayOf(0x00, 0x01, 0x02)
        val runner = CapturingFfmpegCommandRunner { _ -> FfmpegResult(1, byteArrayOf(), "boom") }
        val adapter = FfmpegAudioConversionAdapter(runner)

        assertThrows<AudioConversionException> {
            adapter.convert(input, "recording.webm")
        }

        val invocation = runner.invocations.first()
        val inputPath = invocation[invocation.indexOf("-i") + 1]
        val outputPath = invocation.last()
        assertTrue(Files.notExists(Paths.get(inputPath)), "input temp file should be deleted")
        assertTrue(Files.notExists(Paths.get(outputPath)), "output temp file should be deleted")
    }

    private fun buildPcmWav(): ByteArray {
        val sampleRate = 16000
        val channels = 1
        val bitsPerSample = 16
        val sampleCount = 100
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

    private class CapturingFfmpegCommandRunner(
        private val handler: (List<String>) -> FfmpegResult = { FfmpegResult(0, byteArrayOf(), "") },
    ) : FfmpegCommandRunner {
        val invocations = mutableListOf<List<String>>()

        override fun run(args: List<String>): FfmpegResult {
            invocations.add(args)
            return handler(args)
        }
    }
}
