package com.sismico.openconversation.backend.application.ports.out.transcription

import com.sismico.openconversation.backend.domain.Transcription

interface TranscriptionPort {
    fun transcribe(
        audio: ByteArray,
        language: String?,
    ): Transcription
}
