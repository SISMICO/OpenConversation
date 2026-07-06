package com.sismico.openconversation.backend.adapters.out.llm

import com.fasterxml.jackson.annotation.JsonProperty
import com.sismico.openconversation.backend.application.ports.out.llm.LlmAnalysisPort
import com.sismico.openconversation.backend.config.LlmProperties
import com.sismico.openconversation.backend.domain.LlmAnalysisResult
import com.sismico.openconversation.backend.domain.exception.LlmAnalysisException
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient
import tools.jackson.databind.ObjectMapper

class OpenAiCompatibleLlmAdapter(
    private val restClient: RestClient,
    private val llmProperties: LlmProperties,
    private val objectMapper: ObjectMapper,
) : LlmAnalysisPort {
    override fun analyze(
        transcript: String,
        topic: String,
        language: String?,
    ): LlmAnalysisResult {
        val request = buildRequest(transcript, topic, language)

        return try {
            val response =
                restClient
                    .post()
                    .uri("${llmProperties.baseUrl}/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .apply {
                        if (llmProperties.apiKey.isNotBlank()) {
                            header("Authorization", "Bearer ${llmProperties.apiKey}")
                        }
                    }.body(request)
                    .retrieve()
                    .onStatus({ it.isError }) { _, _ ->
                        throw LlmAnalysisException("LLM returned an error status")
                    }.body(ChatCompletionResponse::class.java)
                    ?: throw LlmAnalysisException("LLM returned an empty response")

            parseResponse(response)
        } catch (ex: LlmAnalysisException) {
            throw ex
        } catch (ex: Exception) {
            throw LlmAnalysisException("LLM request failed: ${ex.message}", ex)
        }
    }

    private fun buildRequest(
        transcript: String,
        topic: String,
        language: String?,
    ): ChatCompletionRequest {
        val languageLabel = language?.let { " in $it" } ?: ""
        val systemPrompt =
            """
            You are a supportive language tutor. Analyze the user's spoken transcript and provide
            structured feedback. Respond with a JSON object containing a "feedbackItems" array.
            Each item must have "excerpt" (the original text), "correctedExcerpt" (the suggested
            correction), and "explanation" (why the correction helps). You may also include an
            optional "overallComment" string with general fluency feedback.
            """.trimIndent()
        val userPrompt =
            """
            Topic: $topic
            Language$languageLabel
            Transcript:
            $transcript
            """.trimIndent()

        return ChatCompletionRequest(
            model = llmProperties.model,
            messages =
                listOf(
                    MessageRequest(role = "system", content = systemPrompt),
                    MessageRequest(role = "user", content = userPrompt),
                ),
            responseFormat = ResponseFormat(),
        )
    }

    private fun parseResponse(response: ChatCompletionResponse): LlmAnalysisResult {
        val content =
            response.choices
                .firstOrNull()
                ?.message
                ?.content
                ?.trim()
                ?: throw LlmAnalysisException("LLM returned no content")

        return try {
            objectMapper.readValue(content, LlmAnalysisResult::class.java)
        } catch (ex: Exception) {
            throw LlmAnalysisException("Failed to parse LLM response: $content", ex)
        }
    }

    private data class ChatCompletionRequest(
        val model: String,
        val messages: List<MessageRequest>,
        @JsonProperty("response_format")
        val responseFormat: ResponseFormat,
    )

    private data class MessageRequest(
        val role: String,
        val content: String,
    )

    private data class ResponseFormat(
        val type: String = "json_object",
    )

    private data class ChatCompletionResponse(
        val choices: List<ChoiceResponse> = emptyList(),
    )

    private data class ChoiceResponse(
        val message: MessageResponse,
    )

    private data class MessageResponse(
        val content: String,
    )
}
