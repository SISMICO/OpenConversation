package com.sismico.openconversation.backend.adapters.out.persistence

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ConversationJpaRepository : JpaRepository<ConversationJpaEntity, UUID> {
    fun findByTopicIdOrderByCreatedAtDesc(topicId: UUID): List<ConversationJpaEntity>

    @Query(
        """
        SELECT c FROM ConversationJpaEntity c
        WHERE (:topicId IS NULL OR c.topicId = :topicId)
        ORDER BY c.createdAt DESC
        """,
    )
    fun findByFilters(
        @Param("topicId") topicId: UUID?,
        pageable: Pageable,
    ): Page<ConversationJpaEntity>

    fun findAllByTopicId(
        topicId: UUID,
        pageable: Pageable,
    ): Page<ConversationJpaEntity>
}
