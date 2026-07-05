package com.sismico.openconversation.backend.application.service

import com.sismico.openconversation.backend.application.ports.out.persistence.ConversationRepositoryPort
import com.sismico.openconversation.backend.application.ports.out.persistence.TopicRepositoryPort
import com.sismico.openconversation.backend.domain.Conversation
import com.sismico.openconversation.backend.domain.ConversationWithTopic
import com.sismico.openconversation.backend.domain.Topic
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

class GetConversationServiceTest {
    private val conversationRepository: ConversationRepositoryPort = mockk()
    private val topicRepository: TopicRepositoryPort = mockk()
    private val getConversationUseCase = ConversationQueryService(conversationRepository, topicRepository)

    @Test
    fun `getById returns conversation with topic title when it exists`() {
        val id = UUID.randomUUID()
        val topicId = UUID.randomUUID()
        val conversation =
            Conversation(
                id = id,
                topicId = topicId,
                audioStorageRef = "audio://placeholder",
                transcript = "Transcript",
                createdAt = nowUtc(),
                updatedAt = nowUtc(),
            )
        val topic =
            Topic(
                id = topicId,
                title = "Topic Title",
                createdAt = nowUtc(),
                updatedAt = nowUtc(),
            )

        every { conversationRepository.findById(id) } returns conversation
        every { topicRepository.findById(topicId) } returns topic

        val result = getConversationUseCase.getById(id)

        assertEquals(ConversationWithTopic(conversation, "Topic Title"), result)
        verify { conversationRepository.findById(id) }
        verify { topicRepository.findById(topicId) }
    }

    @Test
    fun `getById returns null when conversation does not exist`() {
        val id = UUID.randomUUID()

        every { conversationRepository.findById(id) } returns null

        val result = getConversationUseCase.getById(id)

        assertNull(result)
        verify { conversationRepository.findById(id) }
    }

    private fun nowUtc(): OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC)
}
