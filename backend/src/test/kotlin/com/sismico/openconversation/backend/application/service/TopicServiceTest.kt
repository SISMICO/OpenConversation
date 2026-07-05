package com.sismico.openconversation.backend.application.service

import com.sismico.openconversation.backend.application.ports.out.persistence.TopicRepositoryPort
import com.sismico.openconversation.backend.domain.Topic
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

class TopicServiceTest {
    private val topicRepositoryPort: TopicRepositoryPort = mockk()
    private val topicService = TopicService(topicRepositoryPort)

    @Test
    fun `ensureTopic returns existing topic when exact match exists`() {
        val title = "Job Interview"
        val existing =
            Topic(
                id = UUID.randomUUID(),
                title = title,
                createdAt = OffsetDateTime.now(ZoneOffset.UTC),
                updatedAt = OffsetDateTime.now(ZoneOffset.UTC),
            )

        every { topicRepositoryPort.searchByTitle(title) } returns listOf(existing)

        val result = topicService.ensureTopic(title)

        assertEquals(existing, result)
        verify(exactly = 0) { topicRepositoryPort.save(any()) }
    }

    @Test
    fun `ensureTopic creates new topic when no exact match exists`() {
        val title = "Job Interview"
        val similar =
            Topic(
                id = UUID.randomUUID(),
                title = "Different Topic",
                createdAt = OffsetDateTime.now(ZoneOffset.UTC),
                updatedAt = OffsetDateTime.now(ZoneOffset.UTC),
            )
        val saved =
            Topic(
                id = UUID.randomUUID(),
                title = title,
                createdAt = OffsetDateTime.now(ZoneOffset.UTC),
                updatedAt = OffsetDateTime.now(ZoneOffset.UTC),
            )

        every { topicRepositoryPort.searchByTitle(title) } returns listOf(similar)
        every { topicRepositoryPort.save(any()) } returns saved

        val result = topicService.ensureTopic(title)

        assertEquals(saved, result)
        verify { topicRepositoryPort.save(match { it.title == title }) }
    }

    @Test
    fun `ensureTopic trims title and matches case-insensitively`() {
        val title = "  Job Interview  "
        val existing =
            Topic(
                id = UUID.randomUUID(),
                title = "job interview",
                createdAt = OffsetDateTime.now(ZoneOffset.UTC),
                updatedAt = OffsetDateTime.now(ZoneOffset.UTC),
            )

        every { topicRepositoryPort.searchByTitle("Job Interview") } returns listOf(existing)

        val result = topicService.ensureTopic(title)

        assertEquals(existing, result)
    }

    @Test
    fun `ensureTopic throws exception when title is blank`() {
        assertThrows<IllegalArgumentException> {
            topicService.ensureTopic("   ")
        }
    }
}
