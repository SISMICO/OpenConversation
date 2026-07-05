package com.sismico.openconversation.backend.adapters.`in`.web.dto

import java.time.OffsetDateTime
import java.util.UUID

data class ConversationFeedbackResponse(
    val conversationId: UUID,
    val createdAt: OffsetDateTime?,
    val feedbackItems: List<FeedbackItemResponse>,
)
