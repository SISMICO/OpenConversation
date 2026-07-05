package com.sismico.openconversation.backend.domain

import java.time.OffsetDateTime
import java.util.UUID

data class ConversationSummary(
    val id: UUID,
    val topicId: UUID,
    val topicTitle: String,
    val audioStorageRef: String,
    val createdAt: OffsetDateTime? = null,
)
