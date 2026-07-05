package com.sismico.openconversation.backend.application.service

import com.sismico.openconversation.backend.application.ports.out.persistence.ConversationRepositoryPort
import com.sismico.openconversation.backend.application.ports.out.persistence.TopicRepositoryPort
import com.sismico.openconversation.backend.domain.Conversation
import com.sismico.openconversation.backend.domain.FeedbackItem
import com.sismico.openconversation.backend.domain.Topic
import com.sismico.openconversation.backend.domain.pagination.Page
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

class GetTopicFeedbackServiceTest {
    private val topicRepository: TopicRepositoryPort = mockk()
    private val conversationRepository: ConversationRepositoryPort = mockk()
    private val getTopicFeedbackUseCase = TopicQueryService(topicRepository, conversationRepository)

    @Test
    fun `getFeedbackByTopicId returns conversations with feedback items for topic`() {
        val topicId = UUID.randomUUID()
        val conversationId = UUID.randomUUID()
        val conversation =
            Conversation(
                id = conversationId,
                topicId = topicId,
                audioStorageRef = "audio://placeholder",
                transcript = "Transcript",
                analyzedAt = nowUtc(),
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
                createdAt = nowUtc(),
                updatedAt = nowUtc(),
            )
        val conversationPage =
            Page(
                data = listOf(conversation),
                page = 0,
                size = 20,
                totalElements = 1,
                totalPages = 1,
            )

        every { topicRepository.findById(topicId) } returns
            Topic(
                id = topicId,
                title = "Topic",
                createdAt = nowUtc(),
                updatedAt = nowUtc(),
            )
        every {
            conversationRepository.findByTopicIdPaginated(
                topicId = topicId,
                page = 0,
                size = 20,
            )
        } returns conversationPage

        val result =
            getTopicFeedbackUseCase.getFeedbackByTopicId(
                topicId = topicId,
                page = 0,
                size = 20,
            )

        assertEquals(1, result.data.size)
        assertEquals(conversationId, result.data.first().conversationId)
        assertEquals(
            1,
            result.data
                .first()
                .feedbackItems.size,
        )
        verify {
            conversationRepository.findByTopicIdPaginated(
                topicId = topicId,
                page = 0,
                size = 20,
            )
        }
    }

    @Test
    fun `getFeedbackByTopicId throws exception when topic is not found`() {
        val topicId = UUID.randomUUID()

        every { topicRepository.findById(topicId) } returns null

        assertThrows<IllegalArgumentException> {
            getTopicFeedbackUseCase.getFeedbackByTopicId(
                topicId = topicId,
                page = 0,
                size = 20,
            )
        }
    }

    @Test
    fun `getFeedbackByTopicId returns empty page when topic has no conversations`() {
        val topicId = UUID.randomUUID()
        val conversationPage =
            Page(
                data = emptyList<Conversation>(),
                page = 0,
                size = 20,
                totalElements = 0,
                totalPages = 0,
            )

        every { topicRepository.findById(topicId) } returns
            Topic(
                id = topicId,
                title = "Topic",
                createdAt = nowUtc(),
                updatedAt = nowUtc(),
            )
        every {
            conversationRepository.findByTopicIdPaginated(
                topicId = topicId,
                page = 0,
                size = 20,
            )
        } returns conversationPage

        val result =
            getTopicFeedbackUseCase.getFeedbackByTopicId(
                topicId = topicId,
                page = 0,
                size = 20,
            )

        assertEquals(0, result.totalElements)
    }

    private fun nowUtc(): OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC)
}
