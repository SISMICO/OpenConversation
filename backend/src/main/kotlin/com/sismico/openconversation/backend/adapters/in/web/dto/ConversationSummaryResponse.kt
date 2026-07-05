package com.sismico.openconversation.backend.adapters.`in`.web.dto

import java.time.OffsetDateTime
import java.util.UUID

data class ConversationSummaryResponse(
    val id: UUID,
    val topicId: UUID,
    val topicTitle: String,
    val audioStorageRef: String,
    val createdAt: OffsetDateTime?,
)
