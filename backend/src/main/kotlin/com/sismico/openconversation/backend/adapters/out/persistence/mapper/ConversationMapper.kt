package com.sismico.openconversation.backend.adapters.out.persistence.mapper

import com.sismico.openconversation.backend.adapters.out.persistence.ConversationJpaEntity
import com.sismico.openconversation.backend.domain.Conversation

object ConversationMapper {
    fun toDomain(entity: ConversationJpaEntity): Conversation =
        Conversation(
            id = entity.id,
            topicId = requireNotNull(entity.topicId) { "topicId must not be null" },
            audioStorageRef = entity.audioStorageRef,
            transcript = entity.transcript,
            analyzedAt = entity.analyzedAt,
            feedbackItems = entity.feedbackItems.map { FeedbackItemMapper.toDomain(it) },
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )

    fun toEntity(domain: Conversation): ConversationJpaEntity {
        val conversationEntity =
            ConversationJpaEntity().apply {
                id = domain.id
                topicId = domain.topicId
                audioStorageRef = domain.audioStorageRef
                transcript = domain.transcript
                analyzedAt = domain.analyzedAt
                createdAt = domain.createdAt.orUtcNow()
                updatedAt = domain.updatedAt.orUtcNow()
            }

        conversationEntity.feedbackItems =
            domain.feedbackItems
                .map { FeedbackItemMapper.toEntity(it, conversationEntity) }
                .toMutableList()

        return conversationEntity
    }
}
