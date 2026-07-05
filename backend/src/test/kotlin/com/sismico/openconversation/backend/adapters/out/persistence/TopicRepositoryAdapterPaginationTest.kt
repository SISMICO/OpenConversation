package com.sismico.openconversation.backend.adapters.out.persistence

import com.sismico.openconversation.backend.TestcontainersConfiguration
import com.sismico.openconversation.backend.domain.Topic
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import

@Import(TestcontainersConfiguration::class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TopicRepositoryAdapterPaginationTest {
    @Autowired
    lateinit var topicJpaRepository: TopicJpaRepository

    private val adapter by lazy { TopicRepositoryAdapter(topicJpaRepository) }

    @Test
    fun `searchByTitlePaginated returns paginated results`() {
        repeat(5) { index ->
            adapter.save(Topic(title = "Interview Topic $index"))
        }

        val result =
            adapter.searchByTitlePaginated(
                query = "Interview",
                page = 0,
                size = 2,
            )

        assertEquals(2, result.data.size)
        assertEquals(5, result.totalElements)
        assertEquals(3, result.totalPages)
    }

    @Test
    fun `searchByTitlePaginated is case insensitive`() {
        adapter.save(Topic(title = "CaSe InSeNsItIvE"))

        val result =
            adapter.searchByTitlePaginated(
                query = "case insensitive",
                page = 0,
                size = 20,
            )

        assertEquals(1, result.totalElements)
        assertEquals("CaSe InSeNsItIvE", result.data.first().title)
    }

    @Test
    fun `searchByTitlePaginated with empty query returns all topics paginated`() {
        adapter.save(Topic(title = "Topic Alpha"))
        adapter.save(Topic(title = "Topic Beta"))

        val result =
            adapter.searchByTitlePaginated(
                query = "",
                page = 0,
                size = 1,
            )

        assertEquals(1, result.data.size)
        assertEquals(2, result.totalElements)
        assertEquals(2, result.totalPages)
    }
}
