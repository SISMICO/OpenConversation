package com.sismico.openconversation.backend.adapters.out.persistence.mapper

import com.sismico.openconversation.backend.domain.Topic
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.UUID

class TopicMapperTest {
    @Test
    fun `round trip maps topic correctly`() {
        val topic =
            Topic(
                id = UUID.randomUUID(),
                title = "Test Topic",
                createdAt = nowTruncated(),
                updatedAt = nowTruncated(),
            )

        val entity = TopicMapper.toEntity(topic)
        val roundTripped = TopicMapper.toDomain(entity)

        assertEquals(topic, roundTripped)
    }

    private fun nowTruncated(): OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MICROS)
}
