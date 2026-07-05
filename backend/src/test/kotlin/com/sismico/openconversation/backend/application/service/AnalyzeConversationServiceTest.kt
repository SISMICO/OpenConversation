package com.sismico.openconversation.backend.application.service

import com.sismico.openconversation.backend.application.ports.out.persistence.ConversationRepositoryPort
import com.sismico.openconversation.backend.domain.Conversation
import com.sismico.openconversation.backend.domain.ConversationWithTopic
import com.sismico.openconversation.backend.domain.Topic
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

class AnalyzeConversationServiceTest {
    private val topicService: TopicService = mockk()
    private val conversationRepository: ConversationRepositoryPort = mockk()
    private val analyzeConversationService = AnalyzeConversationService(topicService, conversationRepository)

    @Test
    fun `analyze persists conversation`() {
        val topicId = UUID.randomUUID()
        val topic =
            Topic(
                id = topicId,
                title = "job interview about my career",
                createdAt = OffsetDateTime.now(ZoneOffset.UTC),
                updatedAt = OffsetDateTime.now(ZoneOffset.UTC),
            )
        val savedConversation =
            Conversation(
                id = UUID.randomUUID(),
                topicId = topicId,
                audioStorageRef = "audio://placeholder",
                transcript = "Simulated transcription for topic 'job interview about my career'.",
                feedbackItems = emptyList(),
                createdAt = OffsetDateTime.now(ZoneOffset.UTC),
                updatedAt = OffsetDateTime.now(ZoneOffset.UTC),
            )

        every { topicService.ensureTopic("job interview about my career") } returns topic
        every { conversationRepository.save(any()) } returns savedConversation

        val result =
            analyzeConversationService.analyze(
                audioStorageRef = "audio://placeholder",
                topicTitle = "job interview about my career",
            )

        assertEquals(ConversationWithTopic(savedConversation, "job interview about my career"), result)
        val conversationSlot = slot<Conversation>()
        verify { conversationRepository.save(capture(conversationSlot)) }
        assertEquals(topicId, conversationSlot.captured.topicId)
        assertNotNull(conversationSlot.captured.analyzedAt)
    }

    @Test
    fun `analyze passes topic title to topic service`() {
        val topicId = UUID.randomUUID()
        val topic =
            Topic(
                id = topicId,
                title = "job interview",
                createdAt = OffsetDateTime.now(ZoneOffset.UTC),
                updatedAt = OffsetDateTime.now(ZoneOffset.UTC),
            )
        val savedConversation =
            Conversation(
                id = UUID.randomUUID(),
                topicId = topicId,
                audioStorageRef = "audio://ref",
                transcript = "Transcript",
                feedbackItems = emptyList(),
                createdAt = OffsetDateTime.now(ZoneOffset.UTC),
                updatedAt = OffsetDateTime.now(ZoneOffset.UTC),
            )

        every { topicService.ensureTopic("job interview") } returns topic
        every { conversationRepository.save(any()) } returns savedConversation

        val result =
            analyzeConversationService.analyze(
                audioStorageRef = "audio://ref",
                topicTitle = "job interview",
            )

        assertEquals(topic.title, result.topicTitle)
        verify { topicService.ensureTopic("job interview") }
    }
}
