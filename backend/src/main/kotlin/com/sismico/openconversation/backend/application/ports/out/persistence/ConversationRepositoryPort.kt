package com.sismico.openconversation.backend.application.ports.out.persistence

import com.sismico.openconversation.backend.domain.Conversation
import com.sismico.openconversation.backend.domain.pagination.Page
import java.util.UUID

interface ConversationRepositoryPort {
    fun save(conversation: Conversation): Conversation

    fun findById(id: UUID): Conversation?

    fun findByTopicId(topicId: UUID): List<Conversation>

    fun findAll(): List<Conversation>

    fun findByFilters(
        filters: ConversationFilters,
        page: Int,
        size: Int,
    ): Page<Conversation>

    fun findByTopicIdPaginated(
        topicId: UUID,
        page: Int,
        size: Int,
    ): Page<Conversation>
}

data class ConversationFilters(
    val topicId: UUID? = null,
)
