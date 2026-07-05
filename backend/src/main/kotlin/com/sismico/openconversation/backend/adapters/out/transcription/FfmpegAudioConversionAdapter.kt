package com.sismico.openconversation.backend.adapters.out.transcription

import com.sismico.openconversation.backend.application.ports.out.transcription.AudioConversionPort
import com.sismico.openconversation.backend.domain.exception.AudioConversionException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteExisting
import kotlin.io.path.isRegularFile

private const val SAMPLE_RATE = 16000
private const val CHANNELS = 1

/**
 * [AudioConversionPort] implementation that uses ffmpeg to normalize audio.
 *
 * PCM-compatible WAV files pass through unchanged. All other inputs are
 * converted to WAV PCM 16-bit [SAMPLE_RATE] Hz mono.
 */
class FfmpegAudioConversionAdapter(
    private val commandRunner: FfmpegCommandRunner = ProcessBuilderFfmpegCommandRunner(),
) : AudioConversionPort {
    override fun convert(
        audio: ByteArray,
        originalFilename: String?,
    ): ByteArray {
        if (PcmAudioDetector.isPcmWav(audio)) {
            return audio
        }

        val tempDir = Files.createTempDirectory("audio-conversion-")
        val inputFile = tempDir.resolve("input")
        val outputFile = tempDir.resolve("output.wav")

        try {
            Files.write(inputFile, audio)

            val args =
                listOf(
                    "-y",
                    "-i",
                    inputFile.toString(),
                    "-ar",
                    SAMPLE_RATE.toString(),
                    "-ac",
                    CHANNELS.toString(),
                    "-c:a",
                    "pcm_s16le",
                    "-f",
                    "wav",
                    outputFile.toString(),
                )

            val result = commandRunner.run(args)

            if (result.exitCode != 0) {
                throw AudioConversionException(
                    "Audio conversion failed (exit code ${result.exitCode}): ${result.errorOutput}",
                )
            }

            return Files.readAllBytes(outputFile)
        } finally {
            deleteSilently(inputFile)
            deleteSilently(outputFile)
            Files.deleteIfExists(tempDir)
        }
    }

    private fun deleteSilently(path: Path) {
        try {
            if (path.isRegularFile()) {
                path.deleteExisting()
            }
        } catch (_: Exception) {
            // Best-effort cleanup; do not mask the original failure.
        }
    }
}
