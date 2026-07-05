package com.sismico.openconversation.backend.adapters.out.persistence

import com.sismico.openconversation.backend.TestcontainersConfiguration
import com.sismico.openconversation.backend.domain.Conversation
import com.sismico.openconversation.backend.domain.FeedbackItem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
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
class ConversationRepositoryAdapterTest {
    @Autowired
    lateinit var topicJpaRepository: TopicJpaRepository

    @Autowired
    lateinit var conversationJpaRepository: ConversationJpaRepository

    private val conversationAdapter by lazy {
        ConversationRepositoryAdapter(conversationJpaRepository)
    }

    @Test
    fun `save persists conversation with feedback items`() {
        val topic = topicJpaRepository.save(TopicJpaEntity().apply { title = "Conversation Topic" })

        val conversation =
            Conversation(
                topicId = requireNotNull(topic.id),
                audioStorageRef = "audio://placeholder",
                transcript = "Test transcript",
                analyzedAt = nowTruncated(),
                feedbackItems =
                    listOf(
                        FeedbackItem(
                            excerpt = "Excerpt 1",
                            recommendation = "Rec 1",
                            displayOrder = 0,
                            createdAt = nowTruncated(),
                        ),
                    ),
                createdAt = nowTruncated(),
                updatedAt = nowTruncated(),
            )

        val saved = conversationAdapter.save(conversation)

        assertNotNull(saved.id)
        assertEquals(1, saved.feedbackItems.size)
        assertNotNull(saved.feedbackItems.first().id)
    }

    @Test
    fun `findById returns conversation with feedback items`() {
        val topic = topicJpaRepository.save(TopicJpaEntity().apply { title = "Another Topic" })

        val conversation =
            Conversation(
                topicId = requireNotNull(topic.id),
                audioStorageRef = "audio://placeholder",
                transcript = "Test transcript",
                feedbackItems =
                    listOf(
                        FeedbackItem(
                            excerpt = "Excerpt",
                            recommendation = "Rec",
                            displayOrder = 0,
                            createdAt = nowTruncated(),
                        ),
                    ),
                createdAt = nowTruncated(),
                updatedAt = nowTruncated(),
            )

        val saved = conversationAdapter.save(conversation)
        val found = conversationAdapter.findById(requireNotNull(saved.id))

        assertEquals(saved, found)
    }

    @Test
    fun `findByTopicId returns conversations ordered by createdAt desc`() {
        val topic = topicJpaRepository.save(TopicJpaEntity().apply { title = "Ordered Topic" })

        conversationAdapter.save(
            Conversation(
                topicId = requireNotNull(topic.id),
                audioStorageRef = "audio://placeholder",
                transcript = "Older",
                createdAt = nowTruncated().minusDays(1),
                updatedAt = nowTruncated().minusDays(1),
            ),
        )

        conversationAdapter.save(
            Conversation(
                topicId = requireNotNull(topic.id),
                audioStorageRef = "audio://placeholder",
                transcript = "Newer",
                createdAt = nowTruncated(),
                updatedAt = nowTruncated(),
            ),
        )

        val results = conversationAdapter.findByTopicId(requireNotNull(topic.id))

        assertEquals(2, results.size)
        assertTrue(requireNotNull(results[0].createdAt).isAfter(requireNotNull(results[1].createdAt)))
    }

    private fun nowTruncated(): OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MICROS)
}
