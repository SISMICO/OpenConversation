package com.sismico.openconversation.backend.application.service

import com.sismico.openconversation.backend.application.ports.out.persistence.ConversationFilters
import com.sismico.openconversation.backend.application.ports.out.persistence.ConversationRepositoryPort
import com.sismico.openconversation.backend.application.ports.out.persistence.TopicRepositoryPort
import com.sismico.openconversation.backend.domain.Conversation
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

class ListConversationsServiceTest {
    private val conversationRepository: ConversationRepositoryPort = mockk()
    private val topicRepository: TopicRepositoryPort = mockk()
    private val listConversationsUseCase = ConversationQueryService(conversationRepository, topicRepository)

    @Test
    fun `list returns paginated summaries without filters`() {
        val topicId = UUID.randomUUID()
        val conversation =
            Conversation(
                id = UUID.randomUUID(),
                topicId = topicId,
                audioStorageRef = "audio://placeholder",
                transcript = "Transcript",
                createdAt = nowUtc(),
                updatedAt = nowUtc(),
            )
        val topic =
            Topic(
                id = topicId,
                title = "Topic",
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

        every {
            conversationRepository.findByFilters(
                filters = ConversationFilters(),
                page = 0,
                size = 20,
            )
        } returns conversationPage
        every { topicRepository.findByIds(listOf(topicId)) } returns listOf(topic)

        val result =
            listConversationsUseCase.list(
                filters = ConversationFilters(),
                page = 0,
                size = 20,
            )

        assertEquals(1, result.data.size)
        assertEquals("Topic", result.data.first().topicTitle)
        assertEquals(1, result.totalElements)
        verify {
            conversationRepository.findByFilters(
                filters = ConversationFilters(),
                page = 0,
                size = 20,
            )
        }
    }

    @Test
    fun `list passes topicId filter to repository`() {
        val topicId = UUID.randomUUID()
        val conversationPage =
            Page(
                data = emptyList<Conversation>(),
                page = 1,
                size = 10,
                totalElements = 0,
                totalPages = 0,
            )

        every {
            conversationRepository.findByFilters(
                filters = ConversationFilters(topicId = topicId),
                page = 1,
                size = 10,
            )
        } returns conversationPage
        every { topicRepository.findByIds(emptyList()) } returns emptyList()

        val result =
            listConversationsUseCase.list(
                filters = ConversationFilters(topicId = topicId),
                page = 1,
                size = 10,
            )

        assertEquals(0, result.totalElements)
        verify {
            conversationRepository.findByFilters(
                filters = ConversationFilters(topicId = topicId),
                page = 1,
                size = 10,
            )
        }
    }

    @Test
    fun `list throws exception for negative page`() {
        assertThrows<IllegalArgumentException> {
            listConversationsUseCase.list(
                filters = ConversationFilters(),
                page = -1,
                size = 20,
            )
        }
    }

    @Test
    fun `list throws exception for invalid size`() {
        assertThrows<IllegalArgumentException> {
            listConversationsUseCase.list(
                filters = ConversationFilters(),
                page = 0,
                size = 0,
            )
        }
    }

    private fun nowUtc(): OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC)
}
