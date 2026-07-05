package com.sismico.openconversation.backend.application.service

import com.sismico.openconversation.backend.application.ports.`in`.GetTopicFeedbackUseCase
import com.sismico.openconversation.backend.application.ports.`in`.SearchTopicsUseCase
import com.sismico.openconversation.backend.application.ports.out.persistence.ConversationRepositoryPort
import com.sismico.openconversation.backend.application.ports.out.persistence.TopicRepositoryPort
import com.sismico.openconversation.backend.domain.ConversationFeedbackGroup
import com.sismico.openconversation.backend.domain.Topic
import com.sismico.openconversation.backend.domain.pagination.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class TopicQueryService(
    private val topicRepository: TopicRepositoryPort,
    private val conversationRepository: ConversationRepositoryPort,
) : SearchTopicsUseCase,
    GetTopicFeedbackUseCase {
    override fun search(
        query: String?,
        page: Int,
        size: Int,
    ): Page<Topic> {
        require(page >= 0) { "Page must be greater than or equal to 0" }
        require(size in 1..100) { "Size must be between 1 and 100" }
        if (query != null) {
            require(query.length >= 3) { "Query must be at least 3 characters long" }
        }

        return topicRepository.searchByTitlePaginated(query?.trim()?.takeIf { it.isNotBlank() }, page, size)
    }

    override fun getFeedbackByTopicId(
        topicId: UUID,
        page: Int,
        size: Int,
    ): Page<ConversationFeedbackGroup> {
        require(page >= 0) { "Page must be greater than or equal to 0" }
        require(size in 1..100) { "Size must be between 1 and 100" }

        requireNotNull(topicRepository.findById(topicId)) { "Topic not found" }

        val conversationPage = conversationRepository.findByTopicIdPaginated(topicId, page, size)
        val groups =
            conversationPage.data.map { conversation ->
                ConversationFeedbackGroup(
                    conversationId = requireNotNull(conversation.id),
                    createdAt = conversation.createdAt,
                    feedbackItems = conversation.feedbackItems,
                )
            }

        return Page(
            data = groups,
            page = conversationPage.page,
            size = conversationPage.size,
            totalElements = conversationPage.totalElements,
            totalPages = conversationPage.totalPages,
        )
    }
}
