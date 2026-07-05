package com.sismico.openconversation.backend.adapters.out.persistence.mapper

import com.sismico.openconversation.backend.adapters.out.persistence.TopicJpaEntity
import com.sismico.openconversation.backend.domain.Topic

object TopicMapper {
    fun toDomain(entity: TopicJpaEntity): Topic =
        Topic(
            id = entity.id,
            title = entity.title,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )

    fun toEntity(domain: Topic): TopicJpaEntity =
        TopicJpaEntity().apply {
            id = domain.id
            title = domain.title
            createdAt = domain.createdAt.orUtcNow()
            updatedAt = domain.updatedAt.orUtcNow()
        }
}
