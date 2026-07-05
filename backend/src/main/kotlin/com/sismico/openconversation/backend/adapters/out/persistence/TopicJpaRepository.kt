package com.sismico.openconversation.backend.adapters.out.persistence

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface TopicJpaRepository : JpaRepository<TopicJpaEntity, UUID> {
    @Query(
        "SELECT t FROM TopicJpaEntity t WHERE lower(t.title) LIKE lower(concat('%', :query, '%')) ORDER BY t.createdAt DESC",
    )
    fun searchByTitle(
        @Param("query") query: String,
    ): List<TopicJpaEntity>

    @Query(
        """
        SELECT t FROM TopicJpaEntity t
        WHERE (:query IS NULL OR lower(t.title) LIKE lower(concat('%', :query, '%')))
        ORDER BY t.createdAt DESC
        """,
    )
    fun searchByTitlePaginated(
        @Param("query") query: String?,
        pageable: Pageable,
    ): Page<TopicJpaEntity>
}
