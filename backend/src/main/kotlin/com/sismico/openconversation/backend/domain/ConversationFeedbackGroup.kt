package com.sismico.openconversation.backend.domain

import java.time.OffsetDateTime
import java.util.UUID

data class ConversationFeedbackGroup(
    val conversationId: UUID,
    val createdAt: OffsetDateTime? = null,
    val feedbackItems: List<FeedbackItem> = emptyList(),
)
