package com.sismico.openconversation.backend.adapters.out.persistence.mapper

import com.sismico.openconversation.backend.domain.Conversation
import com.sismico.openconversation.backend.domain.FeedbackItem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.UUID

class ConversationMapperTest {
    @Test
    fun `round trip maps conversation correctly`() {
        val topicId = UUID.randomUUID()
        val now = nowTruncated()

        val conversation =
            Conversation(
                id = UUID.randomUUID(),
                topicId = topicId,
                audioStorageRef = "audio://placeholder",
                transcript = "Transcript",
                analyzedAt = now,
                feedbackItems =
                    listOf(
                        FeedbackItem(
                            excerpt = "E1",
                            recommendation = "R1",
                            displayOrder = 0,
                            createdAt = now,
                        ),
                        FeedbackItem(
                            excerpt = "E2",
                            recommendation = "R2",
                            displayOrder = 1,
                            createdAt = now,
                        ),
                    ),
                createdAt = now,
                updatedAt = now,
            )

        val entity = ConversationMapper.toEntity(conversation)
        val roundTripped = ConversationMapper.toDomain(entity)

        assertEquals(conversation, roundTripped)
    }

    private fun nowTruncated(): OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MICROS)
}
