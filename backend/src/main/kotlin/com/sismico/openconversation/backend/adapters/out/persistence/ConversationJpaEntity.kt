package com.sismico.openconversation.backend.adapters.out.persistence

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Entity
@Table(name = "conversations")
class ConversationJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    var id: UUID? = null

    @Column(name = "topic_id", nullable = false)
    var topicId: UUID? = null

    @Column(name = "audio_storage_ref", nullable = false, length = 500)
    var audioStorageRef: String = ""

    @Column(name = "transcript", nullable = false, columnDefinition = "TEXT")
    var transcript: String = ""

    @Column(name = "analyzed_at")
    var analyzedAt: OffsetDateTime? = null

    @OneToMany(
        mappedBy = "conversation",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.EAGER,
    )
    @OrderBy("display_order ASC")
    var feedbackItems: MutableList<FeedbackItemJpaEntity> = mutableListOf()

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC)

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(ZoneOffset.UTC)
}
