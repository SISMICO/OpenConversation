package com.sismico.openconversation.backend.adapters.`in`.web.dto

import java.time.OffsetDateTime
import java.util.UUID

data class ConversationResponse(
    val id: UUID,
    val topicId: UUID,
    val topicTitle: String,
    val transcript: String,
    val audioStorageRef: String,
    val analyzedAt: OffsetDateTime?,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?,
    val feedbackItems: List<FeedbackItemResponse>,
)
