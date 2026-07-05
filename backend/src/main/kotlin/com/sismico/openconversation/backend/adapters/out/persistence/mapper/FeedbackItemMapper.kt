package com.sismico.openconversation.backend.adapters.out.persistence.mapper

import com.sismico.openconversation.backend.adapters.out.persistence.ConversationJpaEntity
import com.sismico.openconversation.backend.adapters.out.persistence.FeedbackItemJpaEntity
import com.sismico.openconversation.backend.domain.FeedbackItem

object FeedbackItemMapper {
    fun toDomain(entity: FeedbackItemJpaEntity): FeedbackItem =
        FeedbackItem(
            id = entity.id,
            excerpt = entity.excerpt,
            recommendation = entity.recommendation,
            displayOrder = entity.displayOrder,
            createdAt = entity.createdAt,
        )

    fun toEntity(
        domain: FeedbackItem,
        conversation: ConversationJpaEntity,
    ): FeedbackItemJpaEntity =
        FeedbackItemJpaEntity().apply {
            id = domain.id
            this.conversation = conversation
            excerpt = domain.excerpt
            recommendation = domain.recommendation
            displayOrder = domain.displayOrder
            createdAt = domain.createdAt.orUtcNow()
        }
}
