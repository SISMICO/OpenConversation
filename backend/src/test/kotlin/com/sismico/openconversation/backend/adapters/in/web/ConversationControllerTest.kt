package com.sismico.openconversation.backend.adapters.`in`.web

import com.sismico.openconversation.backend.application.ports.`in`.AnalyzeConversationUseCase
import com.sismico.openconversation.backend.application.ports.`in`.GetConversationUseCase
import com.sismico.openconversation.backend.application.ports.`in`.ListConversationsUseCase
import com.sismico.openconversation.backend.application.ports.out.persistence.ConversationFilters
import com.sismico.openconversation.backend.domain.Conversation
import com.sismico.openconversation.backend.domain.ConversationSummary
import com.sismico.openconversation.backend.domain.ConversationWithTopic
import com.sismico.openconversation.backend.domain.FeedbackItem
import com.sismico.openconversation.backend.domain.pagination.Page
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@WebMvcTest(ConversationController::class)
@Import(ConversationControllerTest.TestConfig::class)
class ConversationControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var analyzeConversationUseCase: AnalyzeConversationUseCase

    @Autowired
    lateinit var listConversationsUseCase: ListConversationsUseCase

    @Autowired
    lateinit var getConversationUseCase: GetConversationUseCase

    class TestConfig {
        @Bean
        fun analyzeConversationUseCase(): AnalyzeConversationUseCase = mockk(relaxUnitFun = true)

        @Bean
        fun listConversationsUseCase(): ListConversationsUseCase = mockk(relaxUnitFun = true)

        @Bean
        fun getConversationUseCase(): GetConversationUseCase = mockk(relaxUnitFun = true)
    }

    @Test
    fun `POST returns 201 and conversation body when audio and topic are provided`() {
        val topicId = UUID.randomUUID()
        val conversationId = UUID.randomUUID()
        val audioBytes = "fake-audio".toByteArray()
        val audioFile =
            MockMultipartFile(
                "audio",
                "recording.webm",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                audioBytes,
            )
        val conversation =
            Conversation(
                id = conversationId,
                topicId = topicId,
                audioStorageRef = "audio://placeholder/${UUID.randomUUID()}",
                transcript = "Simulated transcription for topic 'job interview about my career'.",
                analyzedAt = nowUtc(),
                feedbackItems =
                    listOf(
                        FeedbackItem(
                            id = UUID.randomUUID(),
                            excerpt = "Sample excerpt one",
                            recommendation = "Consider expanding your vocabulary.",
                            displayOrder = 0,
                            createdAt = nowUtc(),
                        ),
                    ),
                createdAt = nowUtc(),
                updatedAt = nowUtc(),
            )
        val analyzed = ConversationWithTopic(conversation, "job interview about my career")

        every {
            analyzeConversationUseCase.analyze(
                audio = any<ByteArray>(),
                audioFilename = any(),
                topicTitle = "job interview about my career",
                language = any(),
            )
        } returns analyzed

        mockMvc
            .perform(
                multipart("/api/v1/conversations")
                    .file(audioFile)
                    .param("topicTitle", "job interview about my career"),
            ).andExpect(status().isCreated)
            .andExpect(header().string("Location", "http://localhost/api/v1/conversations/$conversationId"))
            .andExpect(jsonPath("$.id").value(conversationId.toString()))
            .andExpect(jsonPath("$.topicId").value(topicId.toString()))
            .andExpect(jsonPath("$.topicTitle").value("job interview about my career"))
            .andExpect(
                jsonPath("$.transcript").value("Simulated transcription for topic 'job interview about my career'."),
            ).andExpect(jsonPath("$.feedbackItems[0].excerpt").value("Sample excerpt one"))

        verify {
            analyzeConversationUseCase.analyze(
                audio = audioBytes,
                audioFilename = "recording.webm",
                topicTitle = "job interview about my career",
                language = null,
            )
        }
    }

    @Test
    fun `POST forwards language parameter to use case when provided`() {
        val topicId = UUID.randomUUID()
        val conversationId = UUID.randomUUID()
        val audioBytes = "fake-audio".toByteArray()
        val audioFile =
            MockMultipartFile(
                "audio",
                "recording.webm",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                audioBytes,
            )
        val conversation =
            Conversation(
                id = conversationId,
                topicId = topicId,
                audioStorageRef = "audio://placeholder/${UUID.randomUUID()}",
                transcript = "Simulated transcription in Portuguese for topic 'job interview'.",
                analyzedAt = nowUtc(),
                feedbackItems = emptyList(),
                createdAt = nowUtc(),
                updatedAt = nowUtc(),
            )
        val analyzed = ConversationWithTopic(conversation, "job interview")

        every {
            analyzeConversationUseCase.analyze(
                audio = any<ByteArray>(),
                audioFilename = any(),
                topicTitle = "job interview",
                language = "Portuguese",
            )
        } returns analyzed

        mockMvc
            .perform(
                multipart("/api/v1/conversations")
                    .file(audioFile)
                    .param("topicTitle", "job interview")
                    .param("language", "Portuguese"),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(conversationId.toString()))

        verify {
            analyzeConversationUseCase.analyze(
                audio = audioBytes,
                audioFilename = "recording.webm",
                topicTitle = "job interview",
                language = "Portuguese",
            )
        }
    }

    @Test
    fun `POST returns 400 when topicTitle is blank`() {
        mockMvc
            .perform(
                multipart("/api/v1/conversations")
                    .file("audio", "fake-audio".toByteArray())
                    .param("topicTitle", "   "),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error.code").exists())
    }

    @Test
    fun `GET list returns 200 with data and pagination`() {
        val topicId = UUID.randomUUID()
        val summary =
            ConversationSummary(
                id = UUID.randomUUID(),
                topicId = topicId,
                topicTitle = "Topic",
                audioStorageRef = "audio://placeholder",
                createdAt = nowUtc(),
            )
        val page =
            Page(
                data = listOf(summary),
                page = 0,
                size = 20,
                totalElements = 1,
                totalPages = 1,
            )

        every {
            listConversationsUseCase.list(
                filters = ConversationFilters(),
                page = 0,
                size = 20,
            )
        } returns page

        mockMvc
            .perform(get("/api/v1/conversations"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data[0].id").value(summary.id.toString()))
            .andExpect(jsonPath("$.data[0].topicTitle").value("Topic"))
            .andExpect(jsonPath("$.pagination.page").value(0))
            .andExpect(jsonPath("$.pagination.size").value(20))
            .andExpect(jsonPath("$.pagination.totalElements").value(1))
            .andExpect(jsonPath("$.pagination.totalPages").value(1))

        verify {
            listConversationsUseCase.list(
                filters = ConversationFilters(),
                page = 0,
                size = 20,
            )
        }
    }

    @Test
    fun `GET list passes topicId filter to use case`() {
        val topicId = UUID.randomUUID()
        val page =
            Page(
                data = emptyList<ConversationSummary>(),
                page = 0,
                size = 20,
                totalElements = 0,
                totalPages = 0,
            )

        every {
            listConversationsUseCase.list(
                filters = ConversationFilters(topicId = topicId),
                page = 0,
                size = 20,
            )
        } returns page

        mockMvc
            .perform(
                get("/api/v1/conversations")
                    .param("topicId", topicId.toString()),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data").isEmpty)

        verify {
            listConversationsUseCase.list(
                filters = ConversationFilters(topicId = topicId),
                page = 0,
                size = 20,
            )
        }
    }

    @Test
    fun `GET by id returns 200 for existing conversation with topic title`() {
        val conversationId = UUID.randomUUID()
        val topicId = UUID.randomUUID()
        val conversation =
            Conversation(
                id = conversationId,
                topicId = topicId,
                audioStorageRef = "audio://placeholder",
                transcript = "Transcript",
                createdAt = nowUtc(),
                updatedAt = nowUtc(),
            )
        val enriched = ConversationWithTopic(conversation, "Real Topic Title")

        every { getConversationUseCase.getById(conversationId) } returns enriched

        mockMvc
            .perform(get("/api/v1/conversations/{id}", conversationId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(conversationId.toString()))
            .andExpect(jsonPath("$.topicId").value(topicId.toString()))
            .andExpect(jsonPath("$.topicTitle").value("Real Topic Title"))
            .andExpect(jsonPath("$.transcript").value("Transcript"))

        verify { getConversationUseCase.getById(conversationId) }
    }

    @Test
    fun `GET by id returns 404 when conversation is not found`() {
        val conversationId = UUID.randomUUID()

        every { getConversationUseCase.getById(conversationId) } returns null

        mockMvc
            .perform(
                get("/api/v1/conversations/{id}", conversationId)
                    .accept(MediaType.APPLICATION_JSON),
            ).andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error.code").value("NOT_FOUND"))
            .andExpect(jsonPath("$.error.message").exists())

        verify { getConversationUseCase.getById(conversationId) }
    }

    private fun nowUtc(): OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC)
}
