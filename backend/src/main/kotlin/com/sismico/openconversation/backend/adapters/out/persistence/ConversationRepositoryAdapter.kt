package com.sismico.openconversation.backend.adapters.out.persistence

import com.sismico.openconversation.backend.adapters.out.persistence.mapper.ConversationMapper
import com.sismico.openconversation.backend.application.ports.out.persistence.ConversationFilters
import com.sismico.openconversation.backend.application.ports.out.persistence.ConversationRepositoryPort
import com.sismico.openconversation.backend.domain.Conversation
import com.sismico.openconversation.backend.domain.pagination.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ConversationRepositoryAdapter(
    private val conversationJpaRepository: ConversationJpaRepository,
) : ConversationRepositoryPort {
    override fun save(conversation: Conversation): Conversation {
        val entity = ConversationMapper.toEntity(conversation)
        val saved = conversationJpaRepository.save(entity)
        return ConversationMapper.toDomain(saved)
    }

    override fun findById(id: UUID): Conversation? =
        conversationJpaRepository.findByIdOrNull(id)?.let { ConversationMapper.toDomain(it) }

    override fun findByTopicId(topicId: UUID): List<Conversation> =
        conversationJpaRepository
            .findByTopicIdOrderByCreatedAtDesc(topicId)
            .map { ConversationMapper.toDomain(it) }

    override fun findAll(): List<Conversation> =
        conversationJpaRepository.findAll().map { ConversationMapper.toDomain(it) }

    override fun findByFilters(
        filters: ConversationFilters,
        page: Int,
        size: Int,
    ): Page<Conversation> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val result = conversationJpaRepository.findByFilters(filters.topicId, pageable)
        return Page(
            data = result.content.map { ConversationMapper.toDomain(it) },
            page = result.number,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages,
        )
    }

    override fun findByTopicIdPaginated(
        topicId: UUID,
        page: Int,
        size: Int,
    ): Page<Conversation> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val result = conversationJpaRepository.findAllByTopicId(topicId, pageable)
        return Page(
            data = result.content.map { ConversationMapper.toDomain(it) },
            page = result.number,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages,
        )
    }
}
