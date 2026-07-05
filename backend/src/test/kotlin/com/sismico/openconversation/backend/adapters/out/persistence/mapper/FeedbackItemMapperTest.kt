package com.sismico.openconversation.backend.adapters.out.persistence.mapper

import com.sismico.openconversation.backend.adapters.out.persistence.ConversationJpaEntity
import com.sismico.openconversation.backend.domain.FeedbackItem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.UUID

class FeedbackItemMapperTest {
    @Test
    fun `round trip maps feedback item correctly`() {
        val feedbackItem =
            FeedbackItem(
                id = UUID.randomUUID(),
                excerpt = "Excerpt",
                recommendation = "Recommendation",
                displayOrder = 2,
                createdAt = nowTruncated(),
            )

        val conversationEntity =
            ConversationJpaEntity().apply {
                id = UUID.randomUUID()
            }

        val entity = FeedbackItemMapper.toEntity(feedbackItem, conversationEntity)
        val roundTripped = FeedbackItemMapper.toDomain(entity)

        assertEquals(feedbackItem, roundTripped)
        assertEquals(conversationEntity, entity.conversation)
    }

    private fun nowTruncated(): OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MICROS)
}
