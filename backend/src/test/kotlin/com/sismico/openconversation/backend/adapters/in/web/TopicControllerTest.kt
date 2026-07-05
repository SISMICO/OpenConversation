package com.sismico.openconversation.backend.adapters.`in`.web

import com.sismico.openconversation.backend.application.ports.`in`.GetTopicFeedbackUseCase
import com.sismico.openconversation.backend.application.ports.`in`.SearchTopicsUseCase
import com.sismico.openconversation.backend.domain.ConversationFeedbackGroup
import com.sismico.openconversation.backend.domain.FeedbackItem
import com.sismico.openconversation.backend.domain.Topic
import com.sismico.openconversation.backend.domain.pagination.Page
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@WebMvcTest(TopicController::class)
@Import(TopicControllerTest.TestConfig::class)
class TopicControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var searchTopicsUseCase: SearchTopicsUseCase

    @Autowired
    lateinit var getTopicFeedbackUseCase: GetTopicFeedbackUseCase

    class TestConfig {
        @Bean
        fun searchTopicsUseCase(): SearchTopicsUseCase = mockk(relaxUnitFun = true)

        @Bean
        fun getTopicFeedbackUseCase(): GetTopicFeedbackUseCase = mockk(relaxUnitFun = true)
    }

    @Test
    fun `GET topics returns paginated search results`() {
        val topic =
            Topic(
                id = UUID.randomUUID(),
                title = "Job Interview",
                createdAt = nowUtc(),
                updatedAt = nowUtc(),
            )
        val page =
            Page(
                data = listOf(topic),
                page = 0,
                size = 20,
                totalElements = 1,
                totalPages = 1,
            )

        every {
            searchTopicsUseCase.search(
                query = "interview",
                page = 0,
                size = 20,
            )
        } returns page

        mockMvc
            .perform(get("/api/v1/topics").param("q", "interview"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data[0].id").value(topic.id.toString()))
            .andExpect(jsonPath("$.data[0].title").value("Job Interview"))
            .andExpect(jsonPath("$.pagination.page").value(0))
            .andExpect(jsonPath("$.pagination.totalElements").value(1))

        verify {
            searchTopicsUseCase.search(
                query = "interview",
                page = 0,
                size = 20,
            )
        }
    }

    @Test
    fun `GET topics uses null query when q is not provided`() {
        val page =
            Page(
                data = emptyList<Topic>(),
                page = 0,
                size = 20,
                totalElements = 0,
                totalPages = 0,
            )

        every {
            searchTopicsUseCase.search(
                query = null,
                page = 0,
                size = 20,
            )
        } returns page

        mockMvc
            .perform(get("/api/v1/topics"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").isEmpty)

        verify {
            searchTopicsUseCase.search(
                query = null,
                page = 0,
                size = 20,
            )
        }
    }

    @Test
    fun `GET topic feedback returns feedback grouped by conversation`() {
        val topicId = UUID.randomUUID()
        val conversationId = UUID.randomUUID()
        val group =
            ConversationFeedbackGroup(
                conversationId = conversationId,
                createdAt = nowUtc(),
                feedbackItems =
                    listOf(
                        FeedbackItem(
                            id = UUID.randomUUID(),
                            excerpt = "Excerpt",
                            recommendation = "Recommendation",
                            displayOrder = 0,
                            createdAt = nowUtc(),
                        ),
                    ),
            )
        val page =
            Page(
                data = listOf(group),
                page = 0,
                size = 20,
                totalElements = 1,
                totalPages = 1,
            )

        every {
            getTopicFeedbackUseCase.getFeedbackByTopicId(
                topicId = topicId,
                page = 0,
                size = 20,
            )
        } returns page

        mockMvc
            .perform(get("/api/v1/topics/{id}/feedback", topicId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data[0].conversationId").value(conversationId.toString()))
            .andExpect(jsonPath("$.data[0].feedbackItems[0].excerpt").value("Excerpt"))
            .andExpect(jsonPath("$.pagination.totalElements").value(1))

        verify {
            getTopicFeedbackUseCase.getFeedbackByTopicId(
                topicId = topicId,
                page = 0,
                size = 20,
            )
        }
    }

    @Test
    fun `GET topic feedback returns empty page when topic has no conversations`() {
        val topicId = UUID.randomUUID()
        val page =
            Page(
                data = emptyList<ConversationFeedbackGroup>(),
                page = 0,
                size = 20,
                totalElements = 0,
                totalPages = 0,
            )

        every {
            getTopicFeedbackUseCase.getFeedbackByTopicId(
                topicId = topicId,
                page = 0,
                size = 20,
            )
        } returns page

        mockMvc
            .perform(get("/api/v1/topics/{id}/feedback", topicId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").isEmpty)
    }

    private fun nowUtc(): OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC)
}
