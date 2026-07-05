package com.sismico.openconversation.backend.application.service

import com.sismico.openconversation.backend.application.ports.`in`.AnalyzeConversationUseCase
import com.sismico.openconversation.backend.application.ports.out.persistence.ConversationRepositoryPort
import com.sismico.openconversation.backend.domain.Conversation
import com.sismico.openconversation.backend.domain.ConversationWithTopic
import com.sismico.openconversation.backend.domain.FeedbackItem
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Service
@Transactional
class AnalyzeConversationService(
    private val topicService: TopicService,
    private val conversationRepository: ConversationRepositoryPort,
) : AnalyzeConversationUseCase {
    override fun analyze(
        audioStorageRef: String,
        topicTitle: String,
    ): ConversationWithTopic {
        val topic = topicService.ensureTopic(topicTitle)
        val now = OffsetDateTime.now(ZoneOffset.UTC)

        val conversation =
            Conversation(
                topicId = requireNotNull(topic.id) { "Topic id must not be null" },
                audioStorageRef = audioStorageRef,
                transcript = "Simulated transcription for topic '${topic.title}'.",
                analyzedAt = now,
                feedbackItems =
                    listOf(
                        FeedbackItem(
                            excerpt = "Sample excerpt one",
                            recommendation = "Consider expanding your vocabulary.",
                            displayOrder = 0,
                            createdAt = now,
                        ),
                        FeedbackItem(
                            excerpt = "Sample excerpt two",
                            recommendation = "Practice verb tenses more.",
                            displayOrder = 1,
                            createdAt = now,
                        ),
                    ),
                createdAt = now,
                updatedAt = now,
            )

        val saved = conversationRepository.save(conversation)
        return ConversationWithTopic(saved, topic.title)
    }
}
