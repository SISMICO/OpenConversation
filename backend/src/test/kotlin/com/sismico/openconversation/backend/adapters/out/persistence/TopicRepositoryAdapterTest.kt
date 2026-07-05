package com.sismico.openconversation.backend.adapters.out.persistence

import com.sismico.openconversation.backend.TestcontainersConfiguration
import com.sismico.openconversation.backend.domain.Topic
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import

@Import(TestcontainersConfiguration::class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TopicRepositoryAdapterTest {
    @Autowired
    lateinit var topicJpaRepository: TopicJpaRepository

    private val adapter by lazy { TopicRepositoryAdapter(topicJpaRepository) }

    @Test
    fun `save persists topic and assigns id`() {
        val topic = Topic(title = "Integration Test Topic")

        val saved = adapter.save(topic)

        assertNotNull(saved.id)
        assertEquals("Integration Test Topic", saved.title)
    }

    @Test
    fun `findById returns persisted topic`() {
        val topic = Topic(title = "Find By Id Topic")
        val saved = adapter.save(topic)

        val found = adapter.findById(requireNotNull(saved.id))

        assertEquals(saved, found)
    }

    @Test
    fun `searchByTitle finds topic by partial match`() {
        adapter.save(Topic(title = "Searchable Topic Title"))

        val results = adapter.searchByTitle("Searchable")

        assertEquals(1, results.size)
        assertEquals("Searchable Topic Title", results.first().title)
    }

    @Test
    fun `findAll returns all topics`() {
        adapter.save(Topic(title = "Topic A"))
        adapter.save(Topic(title = "Topic B"))

        val results = adapter.findAll()

        assertTrue(results.size >= 2)
    }
}
