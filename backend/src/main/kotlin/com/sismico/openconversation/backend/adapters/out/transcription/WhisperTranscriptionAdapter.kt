package com.sismico.openconversation.backend.adapters.out.transcription

import com.sismico.openconversation.backend.application.ports.out.transcription.TranscriptionPort
import com.sismico.openconversation.backend.config.WhisperProperties
import com.sismico.openconversation.backend.domain.Transcription
import com.sismico.openconversation.backend.domain.exception.TranscriptionFailedException
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient

class WhisperTranscriptionAdapter(
    private val restClient: RestClient,
    private val whisperProperties: WhisperProperties,
) : TranscriptionPort {
    override fun transcribe(
        audio: ByteArray,
        language: String?,
    ): Transcription {
        val body = LinkedMultiValueMap<String, Any>()
        body.add("file", NamedByteArrayResource(audio, FILENAME))
        body.add("temperature", "0.0")
        body.add("temperature_inc", "0.2")
        body.add("response_format", "json")

        val response =
            restClient
                .post()
                .uri("${whisperProperties.baseUrl}/inference")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .onStatus({ it.isError }) { _, _ ->
                    throw TranscriptionFailedException("Whisper returned an error status")
                }.body(WhisperResponse::class.java)

        val text = response?.text?.trim()
        if (text.isNullOrBlank()) {
            throw TranscriptionFailedException("Whisper returned an empty or invalid transcription")
        }

        return Transcription(text)
    }

    private class NamedByteArrayResource(
        private val audio: ByteArray,
        private val filename: String,
    ) : ByteArrayResource(audio, filename) {
        override fun getFilename(): String = filename
    }

    companion object {
        private const val FILENAME = "audio.webm"
    }
}
