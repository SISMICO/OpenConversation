package com.sismico.openconversation.backend.adapters.out.transcription

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Detects whether a byte array contains a PCM WAV audio file.
 *
 * Only WAV files with a `fmt` chunk whose audio format tag equals `0x0001`
 * (WAVE_FORMAT_PCM) are considered PCM-compatible. All other inputs, including
 * WebM/Opus, MP3, and WAV files using non-PCM codecs, return `false`.
 */
object PcmAudioDetector {
    private const val PCM_AUDIO_FORMAT = 1.toShort()
    private const val MIN_WAV_HEADER_SIZE = 44

    /**
     * Returns `true` when [audio] is a WAV file using the PCM codec.
     */
    fun isPcmWav(audio: ByteArray): Boolean {
        if (audio.size < MIN_WAV_HEADER_SIZE) {
            return false
        }

        if (!audio.regionMatches(0, "RIFF") || !audio.regionMatches(8, "WAVE")) {
            return false
        }

        return findFmtAudioFormat(audio) == PCM_AUDIO_FORMAT
    }

    private fun findFmtAudioFormat(audio: ByteArray): Short? {
        var offset = 12

        while (offset + 8 <= audio.size) {
            val chunkId = audio.readAscii(offset, 4)
            val chunkSize = audio.readLittleEndianInt(offset + 4)

            if (chunkId == "fmt ") {
                if (offset + 8 + 2 > audio.size) {
                    return null
                }
                return audio.readLittleEndianShort(offset + 8)
            }

            offset += 8 + chunkSize
            if (chunkSize % 2 == 1) {
                offset += 1
            }
        }

        return null
    }

    private fun ByteArray.regionMatches(
        offset: Int,
        value: String,
    ): Boolean =
        this
            .copyOfRange(offset, offset + value.length)
            .contentEquals(value.toByteArray(Charsets.US_ASCII))

    private fun ByteArray.readAscii(
        offset: Int,
        length: Int,
    ): String = String(this, offset, length, Charsets.US_ASCII)

    private fun ByteArray.readLittleEndianInt(offset: Int): Int =
        ByteBuffer
            .wrap(this, offset, 4)
            .order(ByteOrder.LITTLE_ENDIAN)
            .int

    private fun ByteArray.readLittleEndianShort(offset: Int): Short =
        ByteBuffer
            .wrap(this, offset, 2)
            .order(ByteOrder.LITTLE_ENDIAN)
            .short
}
