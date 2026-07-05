package com.sismico.openconversation.backend.application.ports.out.persistence

import com.sismico.openconversation.backend.domain.Topic
import com.sismico.openconversation.backend.domain.pagination.Page
import java.util.UUID

interface TopicRepositoryPort {
    fun save(topic: Topic): Topic

    fun findById(id: UUID): Topic?

    fun findByIds(ids: List<UUID>): List<Topic>

    fun searchByTitle(query: String): List<Topic>

    fun findAll(): List<Topic>

    fun searchByTitlePaginated(
        query: String?,
        page: Int,
        size: Int,
    ): Page<Topic>
}
