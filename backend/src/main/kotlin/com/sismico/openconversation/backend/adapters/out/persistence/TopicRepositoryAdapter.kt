package com.sismico.openconversation.backend.adapters.out.persistence

import com.sismico.openconversation.backend.adapters.out.persistence.mapper.TopicMapper
import com.sismico.openconversation.backend.application.ports.out.persistence.TopicRepositoryPort
import com.sismico.openconversation.backend.domain.Topic
import com.sismico.openconversation.backend.domain.pagination.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class TopicRepositoryAdapter(
    private val topicJpaRepository: TopicJpaRepository,
) : TopicRepositoryPort {
    override fun save(topic: Topic): Topic {
        val entity = TopicMapper.toEntity(topic)
        val saved = topicJpaRepository.save(entity)
        return TopicMapper.toDomain(saved)
    }

    override fun findById(id: UUID): Topic? =
        topicJpaRepository.findById(id).orElse(null)?.let { TopicMapper.toDomain(it) }

    override fun findByIds(ids: List<UUID>): List<Topic> =
        topicJpaRepository.findAllById(ids).map { TopicMapper.toDomain(it) }

    override fun searchByTitle(query: String): List<Topic> =
        topicJpaRepository.searchByTitle(query).map { TopicMapper.toDomain(it) }

    override fun findAll(): List<Topic> = topicJpaRepository.findAll().map { TopicMapper.toDomain(it) }

    override fun searchByTitlePaginated(
        query: String?,
        page: Int,
        size: Int,
    ): Page<Topic> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val result = topicJpaRepository.searchByTitlePaginated(query, pageable)
        return Page(
            data = result.content.map { TopicMapper.toDomain(it) },
            page = result.number,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages,
        )
    }
}
