package com.sismico.openconversation.backend.application.ports.out.transcription

/**
 * Outbound port for normalizing audio into a format suitable for transcription.
 *
 * Implementations may convert the audio (e.g., WebM/Opus to WAV PCM) or return
 * it unchanged when it is already compatible with the downstream transcription
 * service.
 */
interface AudioConversionPort {
    /**
     * Returns audio bytes ready for transcription.
     *
     * @param audio The uploaded audio blob.
     * @param originalFilename The original filename, if available, for format hints.
     * @return A [ByteArray] containing PCM-compatible audio or the original bytes
     *         when no conversion is necessary.
     * @throws com.sismico.openconversation.backend.domain.exception.AudioConversionException
     *         when the audio cannot be converted.
     */
    fun convert(
        audio: ByteArray,
        originalFilename: String? = null,
    ): ByteArray
}
