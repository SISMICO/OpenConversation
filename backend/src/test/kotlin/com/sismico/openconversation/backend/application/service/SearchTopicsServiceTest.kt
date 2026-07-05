package com.sismico.openconversation.backend.application.service

import com.sismico.openconversation.backend.application.ports.out.persistence.ConversationRepositoryPort
import com.sismico.openconversation.backend.application.ports.out.persistence.TopicRepositoryPort
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

class SearchTopicsServiceTest {
    private val topicRepository: TopicRepositoryPort = mockk()
    private val conversationRepository: ConversationRepositoryPort = mockk()
    private val searchTopicsUseCase = TopicQueryService(topicRepository, conversationRepository)

    @Test
    fun `search returns paginated topics matching query`() {
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
            topicRepository.searchByTitlePaginated(
                query = "interview",
                page = 0,
                size = 20,
            )
        } returns page

        val result =
            searchTopicsUseCase.search(
                query = "interview",
                page = 0,
                size = 20,
            )

        assertEquals(page, result)
        verify {
            topicRepository.searchByTitlePaginated(
                query = "interview",
                page = 0,
                size = 20,
            )
        }
    }

    @Test
    fun `search trims query and converts null when blank`() {
        val page =
            Page(
                data = emptyList<Topic>(),
                page = 0,
                size = 20,
                totalElements = 0,
                totalPages = 0,
            )

        every {
            topicRepository.searchByTitlePaginated(
                query = null,
                page = 0,
                size = 20,
            )
        } returns page

        val result =
            searchTopicsUseCase.search(
                query = "   ",
                page = 0,
                size = 20,
            )

        assertEquals(page, result)
        verify {
            topicRepository.searchByTitlePaginated(
                query = null,
                page = 0,
                size = 20,
            )
        }
    }

    @Test
    fun `search throws exception when query is shorter than minimum length`() {
        val exception =
            assertThrows<IllegalArgumentException> {
                searchTopicsUseCase.search(
                    query = "ab",
                    page = 0,
                    size = 20,
                )
            }

        assertEquals("Query must be at least 3 characters long", exception.message)
    }

    @Test
    fun `search throws exception for negative page`() {
        assertThrows<IllegalArgumentException> {
            searchTopicsUseCase.search(
                query = "interview",
                page = -1,
                size = 20,
            )
        }
    }

    @Test
    fun `search throws exception for invalid size`() {
        assertThrows<IllegalArgumentException> {
            searchTopicsUseCase.search(
                query = "interview",
                page = 0,
                size = 0,
            )
        }
    }

    private fun nowUtc(): OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC)
}
