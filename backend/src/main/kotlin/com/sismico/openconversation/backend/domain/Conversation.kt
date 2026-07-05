package com.sismico.openconversation.backend.domain

import java.time.OffsetDateTime
import java.util.UUID

data class Conversation(
    val id: UUID? = null,
    val topicId: UUID,
    val audioStorageRef: String,
    val transcript: String,
    val analyzedAt: OffsetDateTime? = null,
    val feedbackItems: List<FeedbackItem> = emptyList(),
    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null,
)
