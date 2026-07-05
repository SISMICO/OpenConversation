package com.sismico.openconversation.backend.adapters.out.transcription

import com.sismico.openconversation.backend.config.WhisperProperties
import com.sismico.openconversation.backend.domain.exception.TranscriptionFailedException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withRawStatus
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient

class WhisperTranscriptionAdapterTest {
    private lateinit var restClient: RestClient
    private lateinit var server: MockRestServiceServer
    private lateinit var adapter: WhisperTranscriptionAdapter

    @BeforeEach
    fun setUp() {
        val builder = RestClient.builder()
        server = MockRestServiceServer.bindTo(builder).build()
        restClient = builder.build()
        adapter = WhisperTranscriptionAdapter(restClient, WhisperProperties("http://localhost"))
    }

    @AfterEach
    fun tearDown() {
        server.verify()
    }

    @Test
    fun `transcribe returns trimmed text from Whisper response`() {
        server
            .expect(requestTo("http://localhost/inference"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(
                withSuccess(
                    "{\"text\":\" Trying to save some audio to test my gratification.\\n\"}",
                    MediaType.APPLICATION_JSON,
                ),
            )

        val result = adapter.transcribe(byteArrayOf(1, 2, 3), null)

        assertEquals("Trying to save some audio to test my gratification.", result.text)
    }

    @Test
    fun `transcribe posts multipart inference request with audio webm file`() {
        server
            .expect(requestTo("http://localhost/inference"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().contentTypeCompatibleWith(MediaType.MULTIPART_FORM_DATA))
            .andExpect { request ->
                val mockRequest = request as org.springframework.mock.http.client.MockClientHttpRequest
                val body = String(mockRequest.bodyAsBytes, Charsets.UTF_8)
                assertTrue(
                    body.contains("Content-Disposition: form-data; name=\"file\"; filename=\"audio.webm\""),
                    "Expected body to contain file part with audio.webm filename",
                )
            }.andRespond(withSuccess("{\"text\":\"hello\"}", MediaType.APPLICATION_JSON))

        adapter.transcribe(byteArrayOf(1, 2, 3), null)
    }

    @Test
    fun `transcribe throws TranscriptionFailedException when Whisper returns error`() {
        server
            .expect(requestTo("http://localhost/inference"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withRawStatus(503))

        assertThrows<TranscriptionFailedException> {
            adapter.transcribe(byteArrayOf(1, 2, 3), null)
        }
    }

    @Test
    fun `transcribe throws TranscriptionFailedException when text is missing`() {
        server
            .expect(requestTo("http://localhost/inference"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON))

        assertThrows<TranscriptionFailedException> {
            adapter.transcribe(byteArrayOf(1, 2, 3), null)
        }
    }

    @Test
    fun `transcribe throws TranscriptionFailedException when text is blank`() {
        val blankResponse = """{"text":"   \n  "}"""
        server
            .expect(requestTo("http://localhost/inference"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess(blankResponse, MediaType.APPLICATION_JSON))

        assertThrows<TranscriptionFailedException> {
            adapter.transcribe(byteArrayOf(1, 2, 3), null)
        }
    }
}
