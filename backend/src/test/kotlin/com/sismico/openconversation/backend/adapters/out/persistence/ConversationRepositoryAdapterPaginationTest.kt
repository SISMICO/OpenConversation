package com.sismico.openconversation.backend.adapters.out.persistence

import com.sismico.openconversation.backend.TestcontainersConfiguration
import com.sismico.openconversation.backend.application.ports.out.persistence.ConversationFilters
import com.sismico.openconversation.backend.domain.Conversation
import com.sismico.openconversation.backend.domain.FeedbackItem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

@Import(TestcontainersConfiguration::class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ConversationRepositoryAdapterPaginationTest {
    @Autowired
    lateinit var topicJpaRepository: TopicJpaRepository

    @Autowired
    lateinit var conversationJpaRepository: ConversationJpaRepository

    private val conversationAdapter by lazy {
        ConversationRepositoryAdapter(conversationJpaRepository)
    }

    @Test
    fun `findByFilters returns paginated conversations`() {
        val topic = topicJpaRepository.save(TopicJpaEntity().apply { title = "Pagination Topic" })

        repeat(5) { index ->
            conversationAdapter.save(
                Conversation(
                    topicId = requireNotNull(topic.id),
                    audioStorageRef = "audio://placeholder-$index",
                    transcript = "Transcript $index",
                    createdAt = nowTruncated().minusSeconds(index.toLong()),
                    updatedAt = nowTruncated().minusSeconds(index.toLong()),
                ),
            )
        }

        val result =
            conversationAdapter.findByFilters(
                filters = ConversationFilters(),
                page = 0,
                size = 2,
            )

        assertEquals(2, result.data.size)
        assertEquals(5, result.totalElements)
        assertEquals(3, result.totalPages)
        assertEquals(0, result.page)
        assertEquals(2, result.size)
    }

    @Test
    fun `findByFilters filters by topicId`() {
        val topicA = topicJpaRepository.save(TopicJpaEntity().apply { title = "Topic A" })
        val topicB = topicJpaRepository.save(TopicJpaEntity().apply { title = "Topic B" })

        conversationAdapter.save(
            Conversation(
                topicId = requireNotNull(topicA.id),
                audioStorageRef = "audio://a",
                transcript = "A",
                createdAt = nowTruncated(),
                updatedAt = nowTruncated(),
            ),
        )
        conversationAdapter.save(
            Conversation(
                topicId = requireNotNull(topicB.id),
                audioStorageRef = "audio://b",
                transcript = "B",
                createdAt = nowTruncated(),
                updatedAt = nowTruncated(),
            ),
        )

        val result =
            conversationAdapter.findByFilters(
                filters = ConversationFilters(topicId = topicA.id),
                page = 0,
                size = 20,
            )

        assertEquals(1, result.totalElements)
        assertEquals(topicA.id, result.data.first().topicId)
    }

    @Test
    fun `findByTopicIdPaginated returns conversations ordered by createdAt desc`() {
        val topic = topicJpaRepository.save(TopicJpaEntity().apply { title = "Topic Feedback Pagination" })

        repeat(3) { index ->
            conversationAdapter.save(
                Conversation(
                    topicId = requireNotNull(topic.id),
                    audioStorageRef = "audio://$index",
                    transcript = "Transcript $index",
                    feedbackItems =
                        listOf(
                            FeedbackItem(
                                excerpt = "Excerpt $index",
                                recommendation = "Recommendation $index",
                                displayOrder = index,
                                createdAt = nowTruncated(),
                            ),
                        ),
                    createdAt = nowTruncated().minusSeconds(index.toLong()),
                    updatedAt = nowTruncated().minusSeconds(index.toLong()),
                ),
            )
        }

        val result =
            conversationAdapter.findByTopicIdPaginated(
                topicId = requireNotNull(topic.id),
                page = 0,
                size = 2,
            )

        assertEquals(2, result.data.size)
        assertEquals(3, result.totalElements)
        assertTrue(
            requireNotNull(result.data[0].createdAt).isAfter(requireNotNull(result.data[1].createdAt)),
        )
    }

    private fun nowTruncated(): OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MICROS)
}
