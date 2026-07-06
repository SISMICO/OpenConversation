package com.sismico.openconversation.backend.adapters.out.llm

import com.sismico.openconversation.backend.config.LlmProperties
import com.sismico.openconversation.backend.domain.exception.LlmAnalysisException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.mock.http.client.MockClientHttpRequest
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.client.match.MockRestRequestMatchers.header
import org.springframework.test.web.client.match.MockRestRequestMatchers.headerDoesNotExist
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withRawStatus
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient
import tools.jackson.module.kotlin.jacksonObjectMapper

class OpenAiCompatibleLlmAdapterTest {
    private lateinit var restClient: RestClient
    private lateinit var server: MockRestServiceServer
    private val objectMapper = jacksonObjectMapper()
    private lateinit var adapter: OpenAiCompatibleLlmAdapter

    @BeforeEach
    fun setUp() {
        val builder = RestClient.builder()
        server = MockRestServiceServer.bindTo(builder).build()
        restClient = builder.build()
    }

    @AfterEach
    fun tearDown() {
        server.verify()
    }

    @Test
    fun `analyze returns parsed feedback from JSON content`() {
        val properties =
            LlmProperties(
                baseUrl = "http://localhost",
                apiKey = "",
                model = "llama3.2:3b",
                timeoutSeconds = 30,
            )
        adapter = OpenAiCompatibleLlmAdapter(restClient, properties, objectMapper)

        val content =
            """
            {
                "feedbackItems": [
                    {
                        "excerpt": "I go to the store yesterday",
                        "correctedExcerpt": "I went to the store yesterday",
                        "explanation": "Use past tense for completed actions."
                    }
                ],
                "overallComment": "Good effort"
            }
            """.trimIndent()
        val llmResponse = """{"choices":[{"message":{"content":${objectMapper.writeValueAsString(content)}}}]}"""

        server
            .expect(requestTo("http://localhost/chat/completions"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(headerDoesNotExist(HttpHeaders.AUTHORIZATION))
            .andRespond(withSuccess(llmResponse, MediaType.APPLICATION_JSON))

        val result = adapter.analyze("I go to the store yesterday", "daily routine", "English")

        assertEquals(1, result.feedbackItems.size)
        assertEquals("I go to the store yesterday", result.feedbackItems[0].excerpt)
        assertEquals("I went to the store yesterday", result.feedbackItems[0].correctedExcerpt)
        assertEquals("Use past tense for completed actions.", result.feedbackItems[0].explanation)
        assertEquals("Good effort", result.overallComment)
    }

    @Test
    fun `analyze sends configured model and response format`() {
        val properties =
            LlmProperties(
                baseUrl = "http://localhost",
                apiKey = "",
                model = "custom-model",
                timeoutSeconds = 30,
            )
        adapter = OpenAiCompatibleLlmAdapter(restClient, properties, objectMapper)

        server
            .expect(requestTo("http://localhost/chat/completions"))
            .andExpect(method(HttpMethod.POST))
            .andExpect { request ->
                val mockRequest = request as MockClientHttpRequest
                val body = String(mockRequest.bodyAsBytes, Charsets.UTF_8)
                assertEquals("custom-model", objectMapper.readTree(body).path("model").textValue())
                assertEquals(
                    "json_object",
                    objectMapper
                        .readTree(body)
                        .path("response_format")
                        .path("type")
                        .textValue(),
                )
            }.andRespond(withSuccess("{\"choices\":[{\"message\":{\"content\":\"{}\"}}]}", MediaType.APPLICATION_JSON))

        adapter.analyze("hello", "topic", null)
    }

    @Test
    fun `analyze sends Bearer token when api key is provided`() {
        val properties =
            LlmProperties(
                baseUrl = "http://localhost",
                apiKey = "secret-key",
                model = "llama3.2:3b",
                timeoutSeconds = 30,
            )
        adapter = OpenAiCompatibleLlmAdapter(restClient, properties, objectMapper)

        server
            .expect(requestTo("http://localhost/chat/completions"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer secret-key"))
            .andRespond(withSuccess("{\"choices\":[{\"message\":{\"content\":\"{}\"}}]}", MediaType.APPLICATION_JSON))

        adapter.analyze("hello", "topic", null)
    }

    @Test
    fun `analyze throws LlmAnalysisException when LLM returns error status`() {
        val properties =
            LlmProperties(
                baseUrl = "http://localhost",
                apiKey = "",
                model = "llama3.2:3b",
                timeoutSeconds = 30,
            )
        adapter = OpenAiCompatibleLlmAdapter(restClient, properties, objectMapper)

        server
            .expect(requestTo("http://localhost/chat/completions"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withRawStatus(503))

        assertThrows<LlmAnalysisException> {
            adapter.analyze("hello", "topic", null)
        }
    }

    @Test
    fun `analyze throws LlmAnalysisException when content is not valid JSON`() {
        val properties =
            LlmProperties(
                baseUrl = "http://localhost",
                apiKey = "",
                model = "llama3.2:3b",
                timeoutSeconds = 30,
            )
        adapter = OpenAiCompatibleLlmAdapter(restClient, properties, objectMapper)

        server
            .expect(requestTo("http://localhost/chat/completions"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(
                withSuccess("{\"choices\":[{\"message\":{\"content\":\"not json\"}}]}", MediaType.APPLICATION_JSON),
            )

        assertThrows<LlmAnalysisException> {
            adapter.analyze("hello", "topic", null)
        }
    }

    @Test
    fun `analyze throws LlmAnalysisException when choices are empty`() {
        val properties =
            LlmProperties(
                baseUrl = "http://localhost",
                apiKey = "",
                model = "llama3.2:3b",
                timeoutSeconds = 30,
            )
        adapter = OpenAiCompatibleLlmAdapter(restClient, properties, objectMapper)

        server
            .expect(requestTo("http://localhost/chat/completions"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withSuccess("{\"choices\":[]}", MediaType.APPLICATION_JSON))

        assertThrows<LlmAnalysisException> {
            adapter.analyze("hello", "topic", null)
        }
    }
}
