package com.sismico.openconversation.backend.application.service

import com.sismico.openconversation.backend.application.ports.`in`.GetConversationUseCase
import com.sismico.openconversation.backend.application.ports.`in`.ListConversationsUseCase
import com.sismico.openconversation.backend.application.ports.out.persistence.ConversationFilters
import com.sismico.openconversation.backend.application.ports.out.persistence.ConversationRepositoryPort
import com.sismico.openconversation.backend.application.ports.out.persistence.TopicRepositoryPort
import com.sismico.openconversation.backend.domain.ConversationSummary
import com.sismico.openconversation.backend.domain.ConversationWithTopic
import com.sismico.openconversation.backend.domain.pagination.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class ConversationQueryService(
    private val conversationRepository: ConversationRepositoryPort,
    private val topicRepository: TopicRepositoryPort,
) : ListConversationsUseCase,
    GetConversationUseCase {
    override fun list(
        filters: ConversationFilters,
        page: Int,
        size: Int,
    ): Page<ConversationSummary> {
        require(page >= 0) { "Page must be greater than or equal to 0" }
        require(size in 1..100) { "Size must be between 1 and 100" }

        val conversationPage = conversationRepository.findByFilters(filters, page, size)
        val topicIds = conversationPage.data.map { it.topicId }.distinct()
        val topicsById = topicRepository.findByIds(topicIds).associateBy { requireNotNull(it.id) }

        val summaries =
            conversationPage.data.map { conversation ->
                val topic =
                    requireNotNull(topicsById[conversation.topicId]) {
                        "Topic ${conversation.topicId} not found for conversation ${conversation.id}"
                    }
                ConversationSummary(
                    id = requireNotNull(conversation.id),
                    topicId = conversation.topicId,
                    topicTitle = topic.title,
                    audioStorageRef = conversation.audioStorageRef,
                    createdAt = conversation.createdAt,
                )
            }

        return Page(
            data = summaries,
            page = conversationPage.page,
            size = conversationPage.size,
            totalElements = conversationPage.totalElements,
            totalPages = conversationPage.totalPages,
        )
    }

    override fun getById(id: UUID): ConversationWithTopic? {
        val conversation = conversationRepository.findById(id) ?: return null
        val topic =
            requireNotNull(topicRepository.findById(conversation.topicId)) {
                "Topic ${conversation.topicId} not found for conversation $id"
            }
        return ConversationWithTopic(conversation, topic.title)
    }
}
